# Mo Yun Portfolio

一个基于 Vue 3 + TypeScript + Vite 构建的作品集/简历编辑器应用。

## 功能特性

- 可视化编辑器：实时预览作品集效果
- 多模板支持：支持多种主题模板切换
- 数据管理：管理个人资料、技能、工作经历和作品集
- 导出功能：将作品集导出为静态文件

## 技术栈

- Vue 3 (Composition API)
- TypeScript
- Vite
- Vue Router
- Lucide Vue Next (图标库)

## 项目结构

```
src/
├── components/          # 页面组件
├── composables/         # 组合式函数
├── data/               # 数据模型
├── editor/             # 编辑器模块
├── templates/          # 主题模板
├── App.vue             # 根组件
├── main.ts             # 入口文件
└── router.ts           # 路由配置
```

## 开发命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 类型检查
npm run check
```

## 模板系统

项目支持模板扩展，模板位于 `src/templates/` 目录下。当前包含 `mo-yun` 模板。

## License

MIT
