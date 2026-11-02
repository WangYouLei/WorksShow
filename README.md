# WorksShow · Visual Portfolio Editor

> Fill in your content once, switch between templates, and export or deploy with one click — build your personal brand site.

🇨🇳 **[中文 README](README.zh-CN.md)**

<p align="center">
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3.4-42b883?logo=vue.js&logoColor=white">
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-5.3-3178c6?logo=typescript&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6db33f?logo=springboot&logoColor=white">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.0-4479a1?logo=mysql&logoColor=white">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-yellow">
</p>

***

## 📖 What is This?

**WorksShow** is a **personal portfolio / resume site generator** for developers and designers.

No coding required: fill in your personal info, works, experiences and skills in the visual editor, pick one of the **10 distinctive templates**, and you get a personal website you can export or put online at any time.

- Want a new look? Just switch templates — **no need to re-enter content**
- Want a local copy? **Export with one click** to a standalone HTML file that opens in any browser
- Want the world to see it? **Deploy with one click** to Tencent EdgeOne Pages and get a public link instantly
- Not sure how to phrase your resume? A built-in **AI assistant** polishes your copy, and can even generate a custom template from a single sentence

***

## ✨ Key Features

| Feature                        | Description                                                                        |
| ------------------------------ | ---------------------------------------------------------------------------------- |
| 🎨 **10 stylish templates**    | From ink-wash oriental to 3D particle planet, there's one for you (see below)      |
| ✏️ **Visual editing**          | Form on the left, live preview on the right — WYSIWYG with auto-save               |
| 📦 **Single-file HTML export** | Export a zero-dependency HTML file, deployable to any static host                  |
| 🚀 **One-click deploy**        | Integrated with Tencent EdgeOne Pages — configure once, publish with a click       |
| 🤖 **AI assistant**            | Chat-based resume copy polishing; generate a custom template from natural language |
| 🖼️ **Image/video upload**     | Works support cover images and video showcases                                     |
| 🔒 **Full account system**     | Email verification sign-up, phone/email login, password recovery                   |

***

## 🖼️ Template Gallery

| Template               | Style                                                                     |
| ---------------------- | ------------------------------------------------------------------------- |
| Mo Yun (墨韵)            | Oriental ink-wash × magazine                                              |
| Pastel (粉彩)            | Soft pastels × light & modern                                             |
| Aurora (极光)            | Deep space × glassmorphism × Bento Grid                                   |
| Dawn (晨曦)              | Warm editorial × ochre orange                                             |
| Brutal (野性)            | Brutalism × hard borders × primary colors                                 |
| Gazette (报章)           | Classic newspaper × print aesthetics × multi-column                       |
| Glass 3D (琉璃 3D)       | Pastel gradient sky × glassmorphism × real 3D floating scene              |
| Cube Dossier (立方档案)    | Interactive 3D info cube, six faces for six sections                      |
| Particle Planet (粒子星球) | 3D planet of glowing particles with shape-shifting narrative              |
| Virus Infection (病毒侵染) | Cinematic 3D virus, resume unfolds across a six-stage infection lifecycle |

> The four 3D templates remain 100% offline-capable after export, with no external dependencies.

***

## 🚀 Quick Start

### Prerequisites

- Node.js ≥ 18, Java 17, Maven 3.8+
- MySQL 8.0+, Redis 6.0+
- An SMTP mailbox (for verification codes)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/WangYouLei/WorksShow.git
cd WorksShow

# 2. Initialize the database
mysql -u root -p < backend/src/main/resources/sql/schema.sql
mysql -u root -p < backend/src/main/resources/sql/refresh_token.sql

# 3. Start the backend (DB, JWT, mail configs: see "Environment Variables" in the technical doc)
cd backend
mvn spring-boot:run

# 4. Start the frontend (new terminal)
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` → sign up → pick a template → start editing → export or deploy.

***

## 📚 More Documentation

- 📄 **[Technical Documentation](docs/TECHNICAL.md)** (in Chinese) — tech stack, architecture, key implementations, database design, API reference, environment variables and deployment details. For developers and code review.

***

## 📄 License

This project is licensed under the [MIT License](LICENSE).
