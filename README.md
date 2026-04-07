# 💼 Job Application & Assessment System

A complete full-stack **Job Portal** with online assessment, built with **Spring Boot + JDBC** backend and **HTML/CSS/JS** frontend.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green?style=flat-square)
![H2](https://img.shields.io/badge/Database-H2-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-purple?style=flat-square)

---

## ✨ Features

- 🔐 **User Module** — Register & Login (Admin / Student roles)
- 💼 **Job Module** — Browse jobs, apply, admin creates postings
- 📝 **Test System** — 10 random questions, 10-minute timer, auto-score
- 🏆 **Result Module** — Score, percentage, grade, history
- 🎨 **Premium UI** — Dark glassmorphism theme, animations, responsive

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, JDBC (no JPA) |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Database | H2 (in-memory, MySQL-compatible SQL) |
| Build | Maven 3.9 |

## 🚀 Quick Start

```bash
cd backend
mvn spring-boot:run
```

Open **http://localhost:8081** in your browser.

### Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@jobportal.com | admin123 |
| Student | john@gmail.com | john123 |

## 📁 Project Structure

```
├── backend/
│   ├── src/main/java/com/jobportal/
│   │   ├── controller/   (REST APIs)
│   │   ├── service/      (Business logic)
│   │   ├── repository/   (JDBC queries)
│   │   ├── model/        (POJOs)
│   │   ├── config/       (CORS)
│   │   └── util/         (ApiResponse)
│   ├── src/main/resources/
│   │   ├── static/       (Frontend files)
│   │   ├── schema.sql    (6 tables)
│   │   └── data.sql      (Seed data + 20 questions)
│   ├── Dockerfile
│   └── pom.xml
├── frontend/             (Standalone copy)
└── render.yaml           (Deployment config)
```

## 🔌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Register user |
| POST | `/api/users/login` | Login |
| GET | `/api/jobs` | List all jobs |
| POST | `/api/jobs` | Create job (Admin) |
| POST | `/api/jobs/apply` | Apply for job |
| POST | `/api/test/start` | Start test (10 random Qs) |
| POST | `/api/test/submit` | Submit answers, get score |
| GET | `/api/results/user/{id}` | Get user results |

## 🐳 Docker

```bash
cd backend
docker build -t job-portal .
docker run -p 8081:8081 job-portal
```

## 📄 License

MIT — feel free to use for learning and interviews.
