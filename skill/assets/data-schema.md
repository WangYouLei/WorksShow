# 简历数据字段规范

从简历提取信息时，必须输出符合以下结构的 JSON。所有字段均必填（无内容时填空字符串或空数组，不要删除字段）。

## 顶层结构

```json
{
  "profile": { ... },
  "experiences": [ ... ],
  "works": [ ... ],
  "skills": [ ... ]
}
```

## profile（个人基本信息）

| 字段 | 类型 | 说明 | 简历来源 |
|---|---|---|---|
| name | string | 中文姓名 | 姓名 |
| nameEn | string | 英文名/拼音，无则空串 | 姓名拼音 |
| title | string | 职位头衔，如 "Java 后端工程师" | 求职意向/当前职位 |
| tagline | string | 一句话个人定位（30 字内） | 自我评价提炼，无则空串 |
| bio | string | 个人简介（100~200 字） | 自我评价/个人总结 |
| email | string | 邮箱 | 联系方式 |
| phone | string | 手机号，无则空串 | 联系方式 |
| location | string | 城市，如 "杭州" | 现居地 |
| availability | string | 状态，如 "在职，看新机会" / "随时到岗"，无则空串 | 求职状态 |
| stats | array | 亮点数字，0~4 项，每项 {label, value} | 工作年限/项目数/证书等可量化亮点 |
| socials | array | 链接，每项 {name, url}，name 如 "GitHub"、"博客" | 主页/仓库链接 |

## experiences（经历，含工作与教育）

每项字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| period | string | 时间段，如 "2022.03 - 至今" |
| title | string | 职位或学历，如 "高级后端工程师" / "本科 · 计算机科学与技术" |
| organization | string | 公司或学校名称 |
| description | string | 职责与业绩，多条用换行符 `\n` 分隔，建议保留量化数据 |
| type | string | `work`（工作）或 `education`（教育），决定渲染分区 |

提取规则：按时间倒序排列；教育经历也放入本数组并标记 `type: "education"`；实习经历标记 `type: "work"`。

## works（项目/作品）

每项字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| title | string | 项目名称 |
| category | string | 分类，如 "后端系统"、"开源项目"，无则空串 |
| description | string | 项目描述：做了什么 + 技术方案 + 成果数据 |
| year | string | 年份，如 "2024"，不确定可空串 |
| link | string | 可访问链接（仓库/演示地址），无则空串 |
| tags | string[] | 技术标签，3~6 个，如 ["Java", "MySQL", "Redis"] |

提取规则：优先保留有量化成果的项目；描述控制在 150 字内；项目按重要性排序（highlight 的放前面）。

## skills（技能）

每项字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| category | string | 技能分类，如 "后端"、"数据库"、"工具" |
| items | string[] | 该分类下的技能点 |

提取规则：3~5 个分类；每类 3~8 项；避免同一技能重复出现在多个分类。

## 注意事项

- 不要发明简历中不存在的信息；不确定的字段留空，交给用户在第 2 步确认时补充。
- 手机号、邮箱等敏感信息只进 JSON 数据块，不要写进 SKILL 流程输出或日志。
- 完整示例见 `assets/sample-data.json`。
