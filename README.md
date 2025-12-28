# QR Menu Backend

A modern, scalable backend application for creating and managing QR-based digital menus. Built with **Spring Boot 3.5** and **Java 21**, this project implements a **Hexagonal (Ports and Adapters) Architecture** with **Domain-Driven Design** principles.

> **Note**: This project is developed as part of a Bachelor's Thesis.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
  - [Hexagonal Architecture](#hexagonal-architecture)
  - [Module Structure](#module-structure)
- [Technology Stack](#technology-stack)
- [Modules](#modules)
  - [Auth Module](#auth-module)
  - [Theme Module](#theme-module)
  - [Menu Module](#menu-module)
  - [QR Module](#qr-module)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Development](#local-development)
  - [Environment Variables](#environment-variables)
- [Development](#development)
  - [Code Formatting](#code-formatting)
  - [Running Tests](#running-tests)
  - [Database Migrations](#database-migrations)
- [Project Structure](#project-structure)
- [License](#license)

---

## Overview

QR Menu is a digital menu management system that allows restaurant owners and businesses to:

- **Create customizable digital menus** with dynamic content
- **Apply themes** to customize the look and feel of menus
- **Generate QR codes** for easy customer access
- **Manage menu content** with flexible JSON-based data structures
- **Build and deploy menus** asynchronously

The system uses a **theme-based approach** where themes define the structure (via JSON Schema) and appearance of menus, allowing for complete flexibility in content types and relationships.

---

## Architecture

### Hexagonal Architecture

This project follows the **Hexagonal Architecture** (also known as Ports and Adapters), which provides:

- **Separation of concerns** between business logic and external dependencies
- **Testability** through dependency inversion
- **Flexibility** to swap implementations (databases, APIs, etc.)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              ADAPTERS (Infrastructure)                       │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                           INBOUND ADAPTERS                              ││
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────┐ ││
│  │  │  REST Controllers│  │  WebSocket      │  │  Queue Listeners        │ ││
│  │  │  (API Layer)     │  │  Handlers       │  │  (Async Processing)     │ ││
│  │  └────────┬─────────┘  └────────┬────────┘  └────────────┬────────────┘ ││
│  └───────────┼─────────────────────┼────────────────────────┼──────────────┘│
│              │                     │                        │               │
│              ▼                     ▼                        ▼               │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                              INPUT PORTS                                ││
│  │                           (Use Case Interfaces)                         ││
│  │      AuthenticationUseCase  │  MenuUseCase  │  ThemeRegisterUseCase    ││
│  └─────────────────────────────────────────────────────────────────────────┘│
│                                      │                                      │
│                                      ▼                                      │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                           APPLICATION CORE                              ││
│  │  ┌──────────────────────────────────────────────────────────────────┐  ││
│  │  │                        DOMAIN LAYER                               │  ││
│  │  │    User  │  Menu  │  MenuContentItem  │  Theme  │  ThemeManifest │  ││
│  │  └──────────────────────────────────────────────────────────────────┘  ││
│  │  ┌──────────────────────────────────────────────────────────────────┐  ││
│  │  │                      APPLICATION SERVICES                         │  ││
│  │  │  AuthenticationService │ MenuService │ MenuContentService │ ...  │  ││
│  │  └──────────────────────────────────────────────────────────────────┘  ││
│  └─────────────────────────────────────────────────────────────────────────┘│
│                                      │                                      │
│                                      ▼                                      │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                             OUTPUT PORTS                                ││
│  │                        (Repository Interfaces)                          ││
│  │   UserRepositoryPort  │  MenuRepositoryPort  │  ThemeStoragePort       ││
│  └─────────────────────────────────────────────────────────────────────────┘│
│              │                     │                        │               │
│              ▼                     ▼                        ▼               │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                          OUTBOUND ADAPTERS                              ││
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────┐ ││
│  │  │  JPA Repositories│  │  Redis Adapter  │  │  S3 Storage Adapter    │ ││
│  │  │  (PostgreSQL)    │  │  (Cache/Session)│  │  (AWS S3)              │ ││
│  │  └─────────────────┘  └─────────────────┘  └─────────────────────────┘ ││
│  └─────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
```

### Module Structure

Each feature module follows a consistent internal structure:

```
module_name/
├── adapter/                    # Infrastructure layer
│   ├── api/                    # Inbound adapters
│   │   ├── controller/         # REST endpoints
│   │   ├── dto/                # Request/Response DTOs
│   │   │   ├── mapper/         # DTO ↔ Domain mappers
│   │   │   └── payload/        # Request/Response objects
│   │   └── filter/             # HTTP filters
│   ├── persistence/            # Outbound adapters
│   │   ├── entity/             # JPA entities
│   │   ├── mapper/             # Entity ↔ Domain mappers
│   │   └── repository/         # JPA repositories & adapters
│   └── queue/                  # Message queue adapters
├── application/                # Application layer
│   ├── port/
│   │   ├── in/                 # Input ports (use cases)
│   │   │   ├── dto/            # Application DTOs
│   │   │   └── mapper/         # Application mappers
│   │   └── out/                # Output ports (repository interfaces)
│   └── service/                # Use case implementations
├── domain/                     # Domain layer (entities, value objects)
└── util/                       # Module-specific utilities
```

---

## Technology Stack

| Category | Technology | Version | Purpose |
|----------|------------|---------|---------|
| **Language** | Java | 21 | Primary language with virtual threads support |
| **Framework** | Spring Boot | 3.5.4 | Application framework |
| **Database** | PostgreSQL | Latest | Primary data store |
| **Cache/Session** | Redis | Latest | Session management & caching |
| **ORM** | Spring Data JPA + Hibernate | - | Database access |
| **Migrations** | Liquibase | - | Database schema versioning |
| **Object Storage** | AWS S3 | - | Theme file storage |
| **Authentication** | JWT (jjwt) | 0.12.6 | Stateless authentication |
| **Validation** | JSON Schema (networknt) | 1.5.7 | Content validation against theme schemas |
| **Image Processing** | Thumbnailator | 0.4.20 | Thumbnail generation |
| **Build Tool** | Gradle | - | Build automation |
| **Testing** | JUnit 5, Testcontainers | - | Unit & integration testing |
| **Code Style** | Google Java Format | - | Code formatting |

### Key Features

- **Virtual Threads** (Project Loom) enabled for improved concurrency
- **Stateless JWT Authentication** with access/refresh token pattern
- **CSRF Protection** for sensitive endpoints
- **JSONB Storage** for flexible content data in PostgreSQL
- **Async Menu Building** with job status tracking

---

## Modules

### Auth Module

Handles user authentication, authorization, and session management.

**Features:**

- User registration with role assignment (USER, ADMIN)
- JWT-based authentication (access + refresh tokens)
- Secure refresh token handling via HttpOnly cookies
- Session metadata tracking (IP, User-Agent)
- BCrypt password encryption
- CSRF token management

**Key Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Authenticate and receive tokens |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | Invalidate session |
| GET | `/api/v1/auth/csrf` | Get CSRF token |

**Domain Entities:**

- `User` - User account with credentials and status flags
- `Role` - Authorization roles (ADMIN, USER)
- `SessionMetadata` - Session tracking information

---

### Theme Module

Manages theme registration, storage, and schema retrieval.

**Features:**

- Theme upload as ZIP files containing templates and assets
- Thumbnail image upload and processing
- Theme manifest with metadata (name, version, author, content types)
- JSON Schema storage for content validation
- UI Schema storage for form generation
- S3-based theme file storage
- Owner-based access control

**Key Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/theme` | List all themes (paginated) |
| GET | `/api/v1/theme/{themeId}` | Get theme manifest |
| GET | `/api/v1/theme/{themeId}/schemas` | Get theme schemas |
| POST | `/api/v1/theme/register` | Upload and register a new theme |
| POST | `/api/v1/theme/unregister` | Remove a theme |

**Domain Entities:**

- `Theme` - Theme with location, manifest, and schemas
- `ThemeManifest` - Metadata including name, version, author, content types

**Theme Package Structure:**

```
theme.zip
├── manifest.json           # Theme metadata
├── schemas/                # JSON Schemas for content validation
│   ├── category.json
│   ├── product.json
│   └── ...
├── ui-schemas/             # UI Schemas for form generation
│   ├── category.json
│   └── ...
└── templates/              # Theme templates and assets
    └── ...
```

---

### Menu Module

Core module for menu creation and content management.

**Features:**

- Menu CRUD operations with theme association
- Flexible content management using JSON data
- Content validation against theme JSON Schemas
- Relational content items (e.g., categories → products)
- Ordered relationships with position tracking
- Async menu building with job status tracking
- Bulk operations for content management

**Key Endpoints:**

**Menu Management:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/menu/create` | Create a new menu |
| GET | `/api/v1/menu/all` | Get all user's menus |
| GET | `/api/v1/menu/{menuId}` | Get menu details |
| DELETE | `/api/v1/menu/delete/{menuId}` | Delete a menu |
| POST | `/api/v1/menu/build` | Trigger async menu build |

**Content Management:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/menu/{menuId}/content` | Create content item |
| GET | `/api/v1/menu/{menuId}/content/{collection}` | Get collection items |
| GET | `/api/v1/menu/{menuId}/content/{collection}/{itemId}` | Get single item |
| PUT | `/api/v1/menu/{menuId}/content/{collection}/{itemId}` | Update item |
| DELETE | `/api/v1/menu/{menuId}/content/{collection}/{itemId}` | Delete item |
| DELETE | `/api/v1/menu/{menuId}/content/{collection}` | Bulk delete items |

**Job Status:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/menu/job/{jobId}` | Get build job status |

**Domain Entities:**

- `Menu` - Menu with name, owner, and selected theme
- `MenuContentItem` - Content item with collection name and JSON data
- `MenuContentRelation` - Ordered relationship between content items
- `MenuJob` - Async build job with status tracking
- `MenuJobStatus` - Enum (PENDING, PROCESSING, COMPLETED, FAILED)

**Content Data Model:**

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Menu Content System                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────────┐         ┌──────────────────┐                  │
│  │ MenuContentItem  │         │ MenuContentItem  │                  │
│  │ (Category)       │         │ (Product)        │                  │
│  ├──────────────────┤         ├──────────────────┤                  │
│  │ id: UUID         │         │ id: UUID         │                  │
│  │ collection:      │         │ collection:      │                  │
│  │   "categories"   │         │   "products"     │                  │
│  │ data: {          │◄───────►│ data: {          │                  │
│  │   "name": "..."  │ Relation│   "name": "..."  │                  │
│  │   "icon": "..."  │ (1:N)   │   "price": 9.99  │                  │
│  │ }                │         │   "image": "..." │                  │
│  └──────────────────┘         │ }                │                  │
│          │                    └──────────────────┘                  │
│          │                                                          │
│          ▼                                                          │
│  ┌──────────────────────────────────────┐                           │
│  │ MenuContentRelation                  │                           │
│  ├──────────────────────────────────────┤                           │
│  │ sourceItem: Category.id              │                           │
│  │ fieldName: "products"                │                           │
│  │ targetItem: Product.id               │                           │
│  │ position: 0, 1, 2...                 │                           │
│  └──────────────────────────────────────┘                           │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

### QR Module

Handles QR code generation for menus.

> **Status**: In Development

**Planned Features:**

- QR code generation with customizable sizes
- Multiple output formats (PNG, SVG)
- Embedded menu URL encoding

---

## Database Schema

The application uses PostgreSQL with Liquibase for schema management.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Database Schema                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  AUTH MODULE                                                                 │
│  ═══════════                                                                 │
│  ┌─────────────┐      ┌─────────────────┐      ┌─────────────┐              │
│  │    users    │      │   user_roles    │      │    roles    │              │
│  ├─────────────┤      ├─────────────────┤      ├─────────────┤              │
│  │ id (PK)     │◄────►│ user_id (FK)    │      │ id (PK)     │              │
│  │ username    │      │ role_id (FK)    │◄────►│ name        │              │
│  │ password    │      └─────────────────┘      └─────────────┘              │
│  │ email       │                                                             │
│  │ enabled     │                                                             │
│  │ ...         │                                                             │
│  └─────────────┘                                                             │
│         │                                                                    │
│         │ (owner_id)                                                         │
│         ▼                                                                    │
│  THEME MODULE                        MENU MODULE                             │
│  ════════════                        ═══════════                             │
│  ┌─────────────────────┐            ┌─────────────────────┐                 │
│  │       theme         │            │        menu         │                 │
│  ├─────────────────────┤            ├─────────────────────┤                 │
│  │ id (PK)             │◄───────────│ selected_theme_id   │                 │
│  │ owner_id (FK)       │            │ id (PK)             │                 │
│  │ name                │            │ menu_name           │                 │
│  │ thumbnail_url       │            │ owner_id (FK)       │                 │
│  │ theme_location_url  │            └─────────────────────┘                 │
│  │ is_free             │                      │                             │
│  │ manifest (JSONB)    │                      │ (menu_id)                   │
│  │ schemas (JSONB)     │                      ▼                             │
│  │ ui_schemas (JSONB)  │            ┌─────────────────────┐                 │
│  └─────────────────────┘            │  menu_content_item  │                 │
│            │                        ├─────────────────────┤                 │
│            │ (theme_id)             │ id (PK, UUID)       │                 │
│            └───────────────────────►│ menu_id (FK)        │                 │
│                                     │ owner_id (FK)       │                 │
│                                     │ theme_id (FK)       │                 │
│                                     │ collection_name     │                 │
│                                     │ data (JSONB)        │◄──┐             │
│                                     │ created_at          │   │             │
│                                     │ updated_at          │   │             │
│                                     └─────────────────────┘   │             │
│                                               │                │             │
│                                               │                │             │
│                                               ▼                │             │
│                                     ┌─────────────────────────┐│             │
│                                     │ menu_content_relation   ││             │
│                                     ├─────────────────────────┤│             │
│                                     │ id (PK)                 ││             │
│                                     │ source_item_id (FK) ────┘│             │
│                                     │ field_name              │             │
│                                     │ target_item_id (FK) ────┘             │
│                                     │ position                │             │
│                                     │ created_at              │             │
│                                     └─────────────────────────┘             │
│                                                                              │
│  JOB TRACKING                                                                │
│  ════════════                                                                │
│  ┌─────────────────────┐                                                    │
│  │     menu_job        │                                                    │
│  ├─────────────────────┤                                                    │
│  │ id (PK, VARCHAR)    │                                                    │
│  │ status              │                                                    │
│  └─────────────────────┘                                                    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Key Database Features

- **JSONB columns** for flexible content storage with GIN indexing
- **Referential integrity** with foreign key constraints
- **Cascade deletes** for content relations
- **Unique constraints** on usernames, theme names, and relation combinations
- **Partial unique indexes** for ordered positions in relations

---

## API Reference

### Response Format

All API responses follow a consistent format:

**Success Response:**

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

**Error Response:**

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### Authentication

The API uses JWT Bearer tokens for authentication:

```
Authorization: Bearer <access_token>
```

**Token Lifecycle:**

1. Login → Receive access token (body) + refresh token (HttpOnly cookie)
2. Use access token for API requests
3. When access token expires, call `/refresh` to get new tokens
4. Logout invalidates the session

### CSRF Protection

The `/api/v1/auth/refresh` endpoint requires CSRF token:

```
X-XSRF-TOKEN: <csrf_token>
```

---

## Getting Started

### Prerequisites

- **Java 21** or later
- **Docker** and **Docker Compose**
- **AWS Account** (for S3 storage)
- **Gradle** (or use the included wrapper)

### Local Development

1. **Clone the repository:**

   ```bash
   git clone <repository-url>
   cd qr-menu
   ```

2. **Start infrastructure services:**

   ```bash
   docker compose up -d
   ```

   This starts PostgreSQL and Redis containers.

3. **Set environment variables:**

   ```bash
   export JWT_SECRET_KEY="your-256-bit-secret-key"
   export AWS_ACCESS_KEY_ID="your-aws-access-key"
   export AWS_SECRET_ACCESS_KEY="your-aws-secret-key"
   export AWS_REGION="your-aws-region"
   ```

4. **Run the application:**

   ```bash
   ./gradlew bootRun
   ```

   The application starts at `http://localhost:8080`

5. **Install Git hooks (recommended):**

   ```bash
   ./gradlew installGitHooks
   ```

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET_KEY` | Secret key for JWT signing (min 256 bits) | Yes |
| `AWS_ACCESS_KEY_ID` | AWS access key for S3 | Yes |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key for S3 | Yes |
| `AWS_REGION` | AWS region for S3 bucket | Yes |

### Configuration Files

- `application.yaml` - Development configuration
- `application-prod.yaml` - Production configuration (SSL enabled, secure cookies)

---

## Development

### Code Formatting

This project uses **Google Java Format** enforced via Spotless.

**Format code before committing:**

```bash
./gradlew spotlessApply
```

**Check formatting:**

```bash
./gradlew spotlessCheck
```

**Install pre-commit hook:**

```bash
./gradlew installGitHooks
```

The pre-commit hook will prevent commits with formatting issues.

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with test report
./gradlew test --info
```

Tests use **Testcontainers** for PostgreSQL and Redis, ensuring realistic integration testing.

### Database Migrations

Migrations are managed by **Liquibase** and located in:

```
src/main/resources/db/changelog/changes/
```

Migrations run automatically on application startup.

**Create a new migration:**

1. Create a new YAML file: `XXX-description.yaml`
2. Add the changeset with a unique ID and author
3. The migration will apply on next startup

---

## Project Structure

```
qr-menu/
├── src/
│   ├── main/
│   │   ├── java/com/furkancavdar/qrmenu/
│   │   │   ├── auth/                    # Authentication module
│   │   │   ├── common/                  # Shared utilities
│   │   │   │   ├── config/              # Global configurations
│   │   │   │   └── exception/           # Global exception handlers
│   │   │   ├── menu_module/             # Menu management module
│   │   │   ├── qr_module/               # QR code generation module
│   │   │   ├── theme_module/            # Theme management module
│   │   │   └── QrMenuApplication.java   # Application entry point
│   │   └── resources/
│   │       ├── application.yaml         # Development config
│   │       ├── application-prod.yaml    # Production config
│   │       └── db/changelog/            # Liquibase migrations
│   └── test/                            # Test sources
├── scripts/
│   └── pre-commit                       # Git pre-commit hook
├── build.gradle                         # Gradle build configuration
├── compose.yaml                         # Docker Compose for local dev
└── README.md                            # This file
```

---

## Author

Furkan Çavdar
