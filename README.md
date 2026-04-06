<div align="center">

<img src="https://img.shields.io/badge/StayNest-FF385C?style=for-the-badge&logo=airbnb&logoColor=white" alt="StayNest" />

# 🏠 StayNest — Vacation Rental Platform

**A production-grade, full-stack Airbnb-inspired vacation rental platform built with Java 21, Spring Boot 3, React 18, and PostgreSQL.**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3-61DAFB?style=flat&logo=react&logoColor=black)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![Vite](https://img.shields.io/badge/Vite-6.3-646CFF?style=flat&logo=vite&logoColor=white)](https://vitejs.dev/)
[![TailwindCSS](https://img.shields.io/badge/Tailwind-3.4-38B2AC?style=flat&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat&logo=github-actions&logoColor=white)](https://github.com/features/actions)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<br/>

[📬 Postman Collection](https://sanchitp-dev-6200954.postman.co/workspace/StayNest_Final~b74d8646-e796-44ab-a149-cc7283a0f721/collection/51209613-79856fdd-2a0e-435e-9c39-a2af1e636fad?action=share&source=copy-link&creator=51209613) · [🐛 Report Bug](https://github.com/sanchitpdev/StayNest/issues) · [💡 Request Feature](https://github.com/sanchitpdev/StayNest/issues)

</div>

---

## 📋 Table of Contents

- [About The Project](#-about-the-project)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Development Setup](#local-development-setup)
  - [Docker Setup](#docker-setup)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Environment Variables](#-environment-variables)
- [Contributing](#-contributing)
- [Connect](#-connect)

---

## 🎯 About The Project

StayNest is a **full-stack vacation rental platform** that covers the complete booking lifecycle — from property listing and unit management to payments, messaging, reviews, and analytics.

Built in two phases:

| Version | Focus | Entities |
|---------|-------|----------|
| **V1.0 — MVP** | Core booking flow, auth, properties, payments, reviews | 8 entities |
| **V2.0 — Enhanced** | Messaging, coupons, dynamic pricing, availability calendar, admin role | 17 entities |

> The project follows clean layered architecture with a clear separation between Controller, Service, and Repository layers — designed to reflect real-world production engineering practices.

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 3.3.5 | Application framework |
| Spring Security | 6.x | JWT authentication & authorization |
| Spring Data JPA | 3.x | ORM & repository layer |
| Hibernate | 6.x | JPA implementation |
| PostgreSQL | 16 | Primary database |
| JJWT | 0.12.5 | JWT token generation & validation |
| Lombok | 1.18.30 | Boilerplate reduction |
| Hypersistence Utils | 3.7.3 | JSONB support for PostgreSQL |
| SpringDoc OpenAPI | 2.3.0 | Swagger UI & API documentation |
| Maven | 3.9+ | Build & dependency management |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.3 | UI library |
| Vite | 6.3 | Build tool & dev server |
| Tailwind CSS | 3.4 | Utility-first styling |
| React Router | 6.28 | Client-side routing |
| Axios | 1.7 | HTTP client |
| React Hot Toast | 2.4 | Notifications |
| React DatePicker | 7.5 | Date selection |
| React Icons | 5.4 | Icon library |
| Date-fns | 3.6 | Date utilities |

### DevOps & Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Multi-container orchestration |
| Nginx | Frontend serving & reverse proxy |
| GitHub Actions | CI/CD pipeline |
| Self-hosted Runner | Local deployment automation |

---

## ✨ Features

### Authentication & Authorization
- JWT-based stateless authentication with BCrypt password hashing (strength 12)
- Three role system — **GUEST**, **HOST**, **ADMIN**
- Manual role checks in service layer for consistent 403 responses
- Token stored in localStorage with automatic injection via Axios interceptor
- Auto-logout on 401 responses

### Property Management
- Full CRUD operations for properties and units
- Property status workflow: `DRAFT → ACTIVE → INACTIVE → SUSPENDED`
- JSONB amenities storage for flexible property attributes
- Multi-image support with primary image and display ordering
- Case-insensitive search across city, state, and country
- Advanced search with filters — type, bedrooms, guests, price range, rating
- Pagination and sorting on all list endpoints

### Booking System
- Date conflict prevention using JPQL overlap detection
- Availability Calendar pre-populated 2 years ahead per unit (O(1) lookup)
- Full booking status workflow: `PENDING → CONFIRMED → COMPLETED / CANCELLED`
- Guest capacity validation
- Special requests support

### Dynamic Pricing
- Per-unit pricing rules — `BASE`, `WEEKEND`, `HOLIDAY`, `SEASONAL`
- Priority-based price resolution (SEASONAL > HOLIDAY > WEEKEND > BASE)
- Date-range price breakdown API with daily pricing map
- Cleaning fee support

### Payment System
- Multi-method payment recording — UPI, Credit/Debit Card, Net Banking, Wallet, Cash
- Multi-currency support with INR default
- Overpayment protection
- Refund tracking

### Messaging System
- Guest ↔ Host conversations scoped to a property
- Idempotent conversation creation — no duplicate threads
- Unread message tracking per participant
- Messages ordered chronologically

### Coupon & Discount System
- Two discount types — `FLAT` (₹500 off) and `PERCENTAGE` (20% off)
- Maximum discount cap for percentage coupons
- Minimum booking amount validation
- Per-user usage limits
- Auto-expiry when global usage limit is reached
- Coupon code snapshot on booking for audit trail

### Review System
- Post-checkout reviews only (booking must be COMPLETED)
- Five-category ratings — cleanliness, accuracy, communication, location, value
- Host response to reviews with timestamp
- Average rating calculation per property

### Wishlist
- Save and remove properties
- Collection names and notes per wishlist entry
- Paginated wishlist retrieval

### Dashboard Analytics
- Role-aware dashboard — different stats for GUEST vs HOST
- Total bookings, revenue, average rating, upcoming trips
- Top performing properties for hosts

### Data Integrity
- Soft deletes across all 17 entities using `@SQLDelete` + `@SQLRestriction`
- Hibernate 6 compatible annotations (`@SQLRestriction` replaces deprecated `@Where`)
- Audit fields (`created_at`, `updated_at`, `deleted_at`) on all entities
- `@EntityListeners(AuditingEntityListener.class)` for automatic timestamping

---

## 📁 Project Structure

```
StayNest/
├── .github/
│   └── workflows/
│       ├── ci.yml                    # CI — build & verify on every push
│       └── cd.yml                    # CD — deploy on push to main
│
├── staynest-backend/
│   ├── src/main/java/com/staynest/
│   │   ├── config/                   # Security, CORS, JPA, Swagger config
│   │   ├── controller/               # 14 REST controllers
│   │   ├── dto/
│   │   │   ├── request/              # 12 request DTOs
│   │   │   └── response/             # 16 response DTOs
│   │   ├── entity/                   # 17 JPA entities
│   │   ├── enums/                    # 12 enums
│   │   ├── exception/                # Global exception handler + custom exceptions
│   │   ├── repository/               # 15 JPA repositories with custom JPQL
│   │   ├── security/                 # JWT filter, provider, entry point
│   │   ├── service/                  # 14 business logic services
│   │   └── util/                     # DateUtils, MapperUtils, ValidationUtils
│   ├── src/main/resources/
│   │   ├── application.properties    # Base config
│   │   ├── application-dev.properties    # Local development
│   │   └── application-docker.properties # Docker environment
│   └── Dockerfile
│
├── staynest-frontend/
│   ├── src/
│   │   ├── api/                      # Axios instance + all API service functions
│   │   ├── components/
│   │   │   ├── booking/              # BookingWidget
│   │   │   ├── layout/               # Header, Footer, Layout
│   │   │   ├── property/             # PropertyCard
│   │   │   └── ui/                   # Button, Input, Modal, Badge, Spinner etc.
│   │   ├── context/                  # AuthContext
│   │   ├── pages/
│   │   │   ├── auth/                 # Login, Register
│   │   │   ├── booking/              # MyBookings, BookingDetail, HostBookings
│   │   │   ├── dashboard/            # Dashboard
│   │   │   ├── messages/             # Messages
│   │   │   ├── property/             # PropertyList, PropertyDetail, CreateProperty, HostDashboard
│   │   │   ├── user/                 # Profile
│   │   │   └── wishlist/             # Wishlist
│   │   ├── App.jsx                   # Routes with protected/guest/host guards
│   │   └── main.jsx
│   ├── nginx.conf                    # Nginx config with API proxy
│   └── Dockerfile
│
├── docker-compose.yml                # Orchestrates postgres + backend + frontend
└── .env                              # Environment secrets (not committed)
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- **Java 21+** — [Download](https://adoptium.net/)
- **Maven 3.9+** — [Download](https://maven.apache.org/)
- **Node.js 20+** — [Download](https://nodejs.org/)
- **PostgreSQL 16+** — [Download](https://www.postgresql.org/download/) *(for local setup only)*
- **Docker & Docker Compose** — [Download](https://www.docker.com/get-started/) *(for Docker setup)*

---

### Local Development Setup

#### 1. Clone the repository

```bash
git clone https://github.com/sanchitpdev/StayNest.git
cd StayNest
```

#### 2. Set up the database

```sql
-- Connect to PostgreSQL and create the database
CREATE DATABASE staynestdb;
```

#### 3. Configure backend

Edit `staynest-backend/src/main/resources/application-dev.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/staynestdb
spring.datasource.username=postgres
spring.datasource.password=your_password
```

#### 4. Run the backend

```bash
cd staynest-backend
mvn spring-boot:run
```

Backend starts at `http://localhost:8080`
Swagger UI available at `http://localhost:8080/api/v1/swagger-ui.html`

#### 5. Run the frontend

```bash
cd staynest-frontend
npm install
npm run dev
```

Frontend starts at `http://localhost:3000`

> Make sure the backend is running before starting the frontend. The Vite dev server proxies all `/api` requests to `localhost:8080`.

---

### Docker Setup

The easiest way to run the entire stack.

#### 1. Clone the repository

```bash
git clone https://github.com/sanchitpdev/StayNest.git
cd StayNest
```

#### 2. Create the `.env` file

```bash
# Create .env in the project root
cat > .env << EOF
DB_PASSWORD=1234
JWT_SECRET=49485af85b0e6e215d729bffff25eadd6eeec789c2df5675a3a01052003b6f75
EOF
```

#### 3. Build and start all containers

```bash
docker compose up --build -d
```

This starts three containers:
- `staynest_db` — PostgreSQL on port `5433`
- `staynest_backend` — Spring Boot API on port `9090`
- `staynest_frontend` — React app served via Nginx on port `8088`

#### 4. Access the application

| Service | URL |
|---------|-----|
| Frontend | http://localhost:8088 |
| Backend API | http://localhost:9090/api/v1 |
| Swagger UI | http://localhost:9090/api/v1/swagger-ui.html |
| Health Check | http://localhost:9090/api/v1/health |

#### 5. Useful Docker commands

```bash
# View running containers
docker compose ps

# View logs
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres

# Stop all containers
docker compose down

# Stop and remove all data (fresh start)
docker compose down -v

# Rebuild a specific service
docker compose build backend
docker compose up -d backend
```

---

## 📖 API Documentation

The full API is documented with Swagger UI. Once the app is running, visit:

```
http://localhost:9090/api/v1/swagger-ui.html
```

### Quick API Reference

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/auth/register` | Public | Register new user |
| `POST` | `/auth/login` | Public | Login and get JWT |
| `GET` | `/properties` | Public | Browse all active properties |
| `GET` | `/properties/{id}` | Public | Get property details |
| `POST` | `/properties` | HOST | Create new property |
| `PATCH` | `/properties/{id}/activate` | HOST | Publish property |
| `PATCH` | `/properties/{id}/suspend` | ADMIN | Suspend property |
| `GET` | `/pricing/units/{id}` | Public | Get price breakdown for dates |
| `GET` | `/bookings/availability/{unitId}` | Public | Check unit availability |
| `POST` | `/bookings` | AUTH | Create booking |
| `POST` | `/bookings/{id}/confirm` | HOST | Confirm booking |
| `POST` | `/bookings/{id}/complete` | HOST | Mark booking completed |
| `POST` | `/bookings/{id}/cancel` | GUEST | Cancel booking |
| `POST` | `/payments` | AUTH | Record payment |
| `POST` | `/reviews` | GUEST | Submit review |
| `POST` | `/reviews/{id}/host-response` | HOST | Respond to review |
| `POST` | `/coupons` | ADMIN | Create coupon |
| `POST` | `/coupons/apply` | AUTH | Apply coupon to booking |
| `POST` | `/messages/conversations/property/{id}` | AUTH | Start conversation |
| `POST` | `/messages/conversations/{id}/send` | AUTH | Send message |
| `GET` | `/dashboard/stats` | AUTH | Get dashboard statistics |

### Testing with Postman

A complete Postman collection with **115 automated test cases** across **23 phases** is available:

[![Run in Postman](https://run.pstmn.io/button.svg)](https://sanchitp-dev-6200954.postman.co/workspace/StayNest_Final~b74d8646-e796-44ab-a149-cc7283a0f721/collection/51209613-79856fdd-2a0e-435e-9c39-a2af1e636fad?action=share&source=copy-link&creator=51209613)

The collection:
- Auto-clears all variables on fresh run
- Auto-captures tokens and IDs between requests
- Uses dynamic future dates so tests never expire
- Covers happy paths and edge cases (auth failures, duplicates, invalid inputs)

---

## 🗄 Database Schema

The application uses **17 JPA entities** across two versions:

### V1 Entities
`users` · `properties` · `units` · `bookings` · `payments` · `reviews` · `wishlists` · `property_images`

### V2 Entities (additions)
`addresses` · `unit_pricing` · `availability_calendar` · `conversations` · `messages` · `coupons` · `booking_coupons`

### Key Design Decisions

- **Soft deletes** on all entities — data is never permanently deleted, `deleted_at` timestamp is set instead
- **JSONB** for property amenities — flexible key-value storage without schema changes
- **Availability Calendar** pre-populated 2 years ahead — O(1) availability lookup instead of range queries
- **UUID primary keys** — all entities use UUID for distributed-system compatibility
- **Audit fields** — `created_at`, `updated_at`, `deleted_at` on every entity via `@EntityListeners`

---

## ⚙️ CI/CD Pipeline

The project uses **GitHub Actions** with a **self-hosted runner** for automated build and deployment.

### CI Pipeline (every push)

```
Push to any branch
        ↓
Verify Java 21
        ↓
Build backend JAR (mvn clean package -DskipTests)
        ↓
Build Docker images
        ↓
Verify images created
        ↓
Notify pass / fail
```

### CD Pipeline (push to main only)

```
Push to main branch
        ↓
Build backend JAR
        ↓
Stop existing containers (docker compose down)
        ↓
Build fresh Docker images (--no-cache)
        ↓
Start all containers (docker compose up -d)
        ↓
Health check — polls /api/v1/health every 10s (max 2 min)
        ↓
Verify frontend accessible
        ↓
Clean up old images
        ↓
Notify success / failure
```

---

## 🔐 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `DB_PASSWORD` | PostgreSQL password | `1234` |
| `JWT_SECRET` | JWT signing secret | See `.env` |
| `JWT_EXPIRATION_MS` | Token expiry in milliseconds | `86400000` (24h) |

> ⚠️ Never commit your `.env` file. It is already in `.gitignore`.

---

## 🤝 Contributing

Contributions are welcome! Here's how:

```bash
# 1. Fork the repository
# 2. Create your feature branch
git checkout -b feature/AmazingFeature

# 3. Commit your changes
git commit -m "feat: add AmazingFeature"

# 4. Push to the branch
git push origin feature/AmazingFeature

# 5. Open a Pull Request
```

### Commit Message Convention

```
feat:     new feature
fix:      bug fix
refactor: code refactoring
docs:     documentation update
ci:       CI/CD changes
chore:    maintenance tasks
```

---

## 📞 Connect

Built and maintained by **Sanchit Pawar**

[![GitHub](https://img.shields.io/badge/GitHub-sanchitpdev-181717?style=flat&logo=github&logoColor=white)](https://github.com/sanchitpdev)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-sanchitpawar-0077B5?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/sanchitpawar)

---

<div align="center">

**⭐ If you found this project helpful, please give it a star!**

Made with ❤️ by [Sanchit Pawar](https://github.com/sanchitpdev)

</div>
