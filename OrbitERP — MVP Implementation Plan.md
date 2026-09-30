# OrbitERP — MVP Implementation Plan

Companion to the [project plan](/mnt/user-data/outputs/orbiterp-project-plan.md): this document covers the technical architecture and the components needed to build it.

## Confirmed decisions

| Area | Choice |
| --- | --- |
| Tenancy | Shared schema + tenant-routing abstraction (ready for dedicated-DB tenants later) |
| Timeline | Weekly sprints, one semester, solo |
| Hosting | Live VPS/cloud instance |
| Deployment | Automated CI/CD (GitHub Actions) |
| Testing | Minimal — manual testing of the core flow |
| Backend data access | Spring Data JPA / Hibernate, with a `@Filter` for automatic tenant scoping |
| DB migrations | Flyway |
| Frontend UI | Plain CSS / Tailwind, custom design |
| Repository structure | Monorepo (frontend + backend together) |
| Frontend state | Angular services + RxJS, no store library |
| API docs | Skipped for the MVP |

## 1. Tech stack

- **Frontend:** Angular, Tailwind CSS, RxJS
- **Backend:** Spring Boot, Spring Data JPA / Hibernate, Spring Security (JWT)
- **Database:** MySQL, Flyway for migrations
- **Infra:** Docker, Docker Compose, Nginx, a VPS
- **CI/CD:** GitHub Actions

## 2. Architecture overview

```
Browser (Angular + Tailwind)
        │  JWT in Authorization header
   Nginx  ── TLS, reverse proxy, serves the Angular build
        │
 Spring Boot (modular monolith)
   ├─ security/      JWT filter, tenant-resolution filter
   ├─ tenant/         TenantContext, TenantRoutingDataSource
   ├─ auth/           login, users, roles
   ├─ sales/          customers, quotes, orders, invoices
   ├─ inventory/       items, warehouses, stock moves
   ├─ finance/        chart of accounts, journal entries, AR
   └─ common/         base entity, error handling, events
        │
      MySQL (Flyway-managed schema)
```

Modules only talk to each other through **application events** (e.g. `OrderConfirmedEvent`), never by reaching into another module's repositories directly.

## 3. Repository layout (monorepo)

```
orbiterp/
├── backend/
│   ├── src/main/java/com/orbiterp/
│   │   ├── tenant/          TenantContext, TenantFilter, TenantRoutingDataSource
│   │   ├── security/        JwtAuthFilter, SecurityConfig
│   │   ├── auth/            User, Role, AuthController
│   │   ├── sales/           Customer, Quote, Order, Invoice
│   │   ├── inventory/       Item, Warehouse, StockMove
│   │   ├── finance/         Account, JournalEntry
│   │   └── common/          BaseEntity, ApiError, DomainEvent
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/    Flyway scripts: V1__init.sql, V2__..., etc.
│   └── build.gradle (or pom.xml)
├── frontend/
│   ├── src/app/
│   │   ├── core/            auth service, http interceptor (JWT), tenant service
│   │   ├── sales/
│   │   ├── inventory/
│   │   ├── finance/
│   │   └── shared/          Tailwind-based UI components
│   └── package.json
├── docker-compose.yml         local dev: backend + mysql + frontend
├── docker-compose.prod.yml    VPS: backend + mysql + nginx
└── .github/workflows/deploy.yml
```

## 4. Backend components

**Tenant resolution**

- `TenantFilter` (servlet filter): reads the subdomain (or `tenant_id` JWT claim after login), sets `TenantContext.set(tenantId)`, clears it at the end of the request.
- `@FilterDef`/`@Filter` on tenant-owned entities, enabled per-session from `TenantContext`, so every JPA query is automatically scoped — no `WHERE tenant_id = ?` written by hand.
- `TenantRoutingDataSource` extends `AbstractRoutingDataSource`: for the MVP it always returns the one shared datasource, but the lookup key is already `tenant_id`, so adding a dedicated datasource for a tenant later is a config change, not a rewrite.

**Base entity** (`common.BaseEntity`): `id`, `tenantId`, `createdAt`, `createdBy`, `updatedAt`, `updatedBy`, `version` (optimistic locking) — every domain entity extends it.

**Security:** Spring Security with a stateless JWT filter. Token carries `tenant_id`, `user_id`, `roles`. Roles are tenant-scoped (`SALES_REP`, `WAREHOUSE_CLERK`, `ACCOUNTANT`, `ADMIN`).

**Events:** Spring's `ApplicationEventPublisher` for in-process domain events (`OrderConfirmedEvent` → Inventory reserves stock; `InvoicePostedEvent` → Finance writes the journal entry). Keeps modules decoupled without needing a message broker for the MVP.

**Error format:** consistent JSON body `{ code, message, details, traceId }` from a single `@ControllerAdvice`.

## 5. Frontend components

- **`core/`**: `AuthService` (login, token storage), `HttpInterceptor` (attaches JWT, handles 401 → redirect to login), `TenantService` (reads tenant from the resolved subdomain).
- **Per-module folders** (`sales`, `inventory`, `finance`): each with its own services (RxJS `Observable`-based HTTP calls) and components — no global store, state lives in the services and in component state.
- **`shared/`**: Tailwind-based building blocks (buttons, tables, form fields, modals) reused across modules.

## 6. Database & migrations

- Flyway scripts in `backend/src/main/resources/db/migration`, one file per change, never edited after being applied.
- `V1__init.sql`: tenants, users, roles.
- `V2__sales.sql`, `V3__inventory.sql`, `V4__finance.sql`, following the sprint order.
- Every tenant-owned table: `tenant_id BIGINT NOT NULL`, tenant-scoped unique constraints (e.g. `UNIQUE(tenant_id, order_number)`), composite indexes leading with `tenant_id`.

## 7. CI/CD pipeline (GitHub Actions)

`.github/workflows/deploy.yml`, triggered on push to `main`:

1. Checkout
2. Build & test backend (Gradle/Maven)
3. Build frontend (Angular production build)
4. Build and push Docker images (backend, frontend/nginx)
5. SSH into the VPS, `docker compose pull && docker compose up -d`, Flyway migrations run automatically on backend startup

Secrets (VPS host/key, DB password, JWT signing key) stored in GitHub Actions secrets — never committed.

## 8. Deployment topology (VPS)

```
docker-compose.prod.yml
├── nginx      → serves Angular build, reverse-proxies /api to backend, TLS
├── backend    → Spring Boot app (runs Flyway on startup)
└── mysql      → single shared-schema database for all MVP tenants
```

## 9. Testing approach (minimal)

- No automated test suite for the MVP.
- A short **manual test script**, run before each milestone demo and before the final demo: log in as two different tenants, run the order-to-cash flow on each, confirm one tenant never sees the other's data.
- Keep that script in `backend/README.md` so it's repeatable from week to week.

## 10. Mapping to the sprint plan

This implementation plan slots directly into the [project plan](/mnt/user-data/outputs/orbiterp-project-plan.md)'s weekly sprints: Week 1 sets up this repo layout and the CI/CD skeleton, Week 2 builds the tenant/routing package, and each following module (Sales, Inventory, Finance) is built as its own package with its own Flyway scripts and Angular folder.