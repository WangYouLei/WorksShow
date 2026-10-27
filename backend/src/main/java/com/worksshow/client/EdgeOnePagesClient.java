package com.worksshow.client;

import com.worksshow.dto.EdgeOneDeployResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * EdgeOne Pages 部署客户端
 * <p>
 * 通过 ProcessBuilder 调用 EdgeOne CLI(edgeone pages deploy)完成部署。
 * 要求后端服务器已安装 Node.js 18+ 和 edgeone CLI(≥1.2.30)。
 * <p>
 * 部署流程:
 * <ol>
 *   <li>创建临时目录,将 HTML 写入 index.html</li>
 *   <li>执行 edgeone pages deploy &lt;dir&gt; -t &lt;token&gt; -n &lt;projectName&gt;</li>
 *   <li>解析 stdout 提取 EDGEONE_DEPLOY_URL 和 EDGEONE_PROJECT_ID</li>
 *   <li>清理临时目录</li>
 * </ol>
 *
 * @author WorksShow
 */
@Slf4j
@Component
public class EdgeOnePagesClient {

    /** CLI 可执行文件名(Windows 下 npm 全局安装的为 .cmd) */
    private static final String CLI_BIN = System.getProperty("os.name", "").toLowerCase().contains("win")
            ? "edgeone.cmd" : "edgeone";

    /** 部署成功输出中匹配 DEPLOY_URL 的正则 */
    private static final Pattern DEPLOY_URL_PATTERN =
            Pattern.compile("EDGEONE_DEPLOY_URL=(\\S+)");

    /** 部署成功输出中匹配 PROJECT_ID 的正则 */
    private static final Pattern PROJECT_ID_PATTERN =
            Pattern.compile("EDGEONE_PROJECT_ID=(\\S+)");

    /** CLI 输出累积上限(字符),防止异常 CLI 无限输出撑爆内存 */
    private static final int MAX_OUTPUT_LENGTH = 64 * 1024;

    /** 项目名合法格式:字母/数字开头,仅字母/数字/下划线/连字符。
     *  兜底校验:防止历史数据或绕过 DTO 校验的值以 "-" 开头被 CLI 解析为其他选项(参数注入) */
    private static final Pattern PROJECT_NAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-_]{0,99}$");

    /** 部署超时(秒),CLI 构建上传可能耗时 */
    @Value("${edgeone.deploy-timeout-seconds:180}")
    private long deployTimeoutSeconds;

    /**
     * 部署 HTML 到 EdgeOne Pages
     *
     * @param html        完整 HTML 内容
     * @param apiToken    明文 API Token(CLI -t 参数)
     * @param projectName EdgeOne 项目名(CLI -n 参数)
     * @return 部署结果
     */
    public EdgeOneDeployResult deploy(String html, String apiToken, String projectName) {
        // 项目名兜底校验(防参数注入),历史配置数据可能未经过 DTO 校验
        if (projectName == null || !PROJECT_NAME_PATTERN.matcher(projectName).matches()) {
            return new EdgeOneDeployResult(false, null, null,
                    "项目名不合法:须以字母或数字开头,仅可包含字母/数字/下划线/连字符");
        }
        Path tempDir = null;
        try {
            // 1. 创建临时目录并写入 index.html
            tempDir = Files.createTempDirectory("edgeone-deploy-");
            Path indexFile = tempDir.resolve("index.html");
            Files.writeString(indexFile, html, StandardCharsets.UTF_8);
            log.info("部署临时目录: {}", tempDir);

            // 2. 构建 CLI 命令
            ProcessBuilder pb = new ProcessBuilder(
                    CLI_BIN, "pages", "deploy",
                    tempDir.toString(),
                    "-t", apiToken,
                    "-n", projectName
            );
            pb.redirectErrorStream(true);

            // 3. 启动进程,由独立守护线程读取输出
            //    (若在当前线程读到 EOF 再 waitFor,进程挂起不退出时 readLine 会永久阻塞,
            //     超时与 destroyForcibly 永远执行不到,部署请求将无限期占用容器线程)
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized (output) {
                            if (output.length() < MAX_OUTPUT_LENGTH) {
                                output.append(line).append('\n');
                            }
                        }
                    }
                } catch (IOException ignored) {
                    // 进程被强制销毁时输出流关闭,属预期情况
                }
            });
            outputReader.setDaemon(true);
            outputReader.start();

            // 4. 等待完成(带超时)
            boolean finished = process.waitFor(deployTimeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                // 等待进程真正终止,避免遗留僵尸进程占用资源
                try {
                    if (!process.waitFor(5, TimeUnit.SECONDS)) {
                        log.warn("EdgeOne 进程强制销毁后仍未终止");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new EdgeOneDeployResult(false, null, null,
                        "部署超时(" + deployTimeoutSeconds + "秒)");
            }

            // 进程已退出,等待输出线程收尾(最多5秒),保证 output 完整
            outputReader.join(5000);

            int exitCode = process.exitValue();
            String stdout = output.toString();
            // 过滤可能包含 API Token 的行(CLI 失败时可能回显命令行,含 -t <token>)
            String safeLog = sanitizeLog(stdout);
            log.info("EdgeOne CLI 输出(exitCode={}):\n{}", exitCode, safeLog);

            if (exitCode != 0) {
                return new EdgeOneDeployResult(false, null, null,
                        "CLI 退出码 " + exitCode + ": " + extractError(stdout));
            }

            // 5. 解析输出
            String deployUrl = extract(stdout, DEPLOY_URL_PATTERN);
            String projectId = extract(stdout, PROJECT_ID_PATTERN);
            if (deployUrl == null) {
                return new EdgeOneDeployResult(false, null, null,
                        "未在 CLI 输出中找到部署URL");
            }
            return new EdgeOneDeployResult(true, deployUrl, projectId, null);

        } catch (IOException e) {
            log.error("调用 EdgeOne CLI 失败", e);
            return new EdgeOneDeployResult(false, null, null,
                    "调用部署命令失败: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new EdgeOneDeployResult(false, null, null, "部署被中断");
        } finally {
            cleanup(tempDir);
        }
    }

    /** 从输出中提取正则第一个分组 */
    private String extract(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    /**
     * 过滤日志中可能包含 API Token 的行。
     * CLI 失败时可能回显完整命令行(含 -t &lt;token&gt;),记录前需脱敏。
     */
    private String sanitizeLog(String stdout) {
        return Arrays.stream(stdout.split("\n"))
                .map(line -> {
                    // 含 "-t " 参数的行(命令回显),将 token 部分替换为 ****
                    if (line.contains(" -t ")) {
                        return line.replaceAll("-t\\s+\\S+", "-t ****");
                    }
                    return line;
                })
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    /** 提取错误信息(取输出的末尾若干行,避免过长) */
    private String extractError(String stdout) {
        String[] lines = stdout.split("\n");
        int start = Math.max(0, lines.length - 5);
        return String.join(" | ", Arrays.copyOfRange(lines, start, lines.length));
    }

    /** 递归清理临时目录 */
    private void cleanup(Path dir) {
        if (dir == null) {
            return;
        }
        try {
            if (Files.exists(dir)) {
                try (Stream<Path> paths = Files.walk(dir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException ignored) {
                                }
                            });
                }
            }
        } catch (IOException e) {
            log.warn("清理临时目录失败: {}", dir, e);
        }
    }
}
