# OrbitERP backend

Spring Boot modular monolith: `tenant`, `security`, `auth`, `sales`, `inventory`, `finance` and `common`.
MySQL schema managed by Flyway (`src/main/resources/db/migration`), JWT authentication, one shared
schema for all tenants.

## Run it

Everything runs in Docker, so you need neither Java nor Node locally. From the repository root:

```bash
docker compose up --build
```

- App (Angular dev server): http://localhost:4200
- API: http://localhost:8080/api
- MySQL: localhost:3306 (user `orbiterp`, password `orbiterp`)

The local stack starts with `DEMO_DATA_ENABLED=true`, which creates two demo workspaces on first start.

To run the backend from an IDE instead, start only MySQL (`docker compose up mysql`) and run
`OrbitErpApplication` with the environment variable `DEMO_DATA_ENABLED=true`.

## Demo accounts

Every demo user has the password `demo1234`. These are test values for local development only.

| Workspace | Username     | Role            |
|-----------|--------------|-----------------|
| `acme`    | `admin`      | Admin           |
| `acme`    | `sales`      | Sales rep       |
| `acme`    | `warehouse`  | Warehouse clerk |
| `acme`    | `accountant` | Accountant      |
| `globex`  | same four usernames | same roles |

Acme sells laptops, monitors and keyboards; Globex sells pallets and boxes. Both start with opening stock
and two customers.

## How tenancy works

- After login, the tenant always comes from the JWT's `tenant_id` claim (`TenantFilter`), never from
  anything else the client sends. On a tenant subdomain (with `BASE_DOMAIN` set) the subdomain must match.
- Every tenant-owned entity extends `BaseEntity`, whose Hibernate filter is switched on for every session,
  so every JPQL query only sees the current tenant's rows.
- Hibernate filters do not apply to loads by primary key, so every repository extends `TenantScopedRepository`,
  which turns `findById` into a filtered query: another tenant's id returns nothing (404).
- As a last line of defence, `BaseEntity` refuses to load any row whose `tenant_id` is not the current tenant.
- New rows take their `tenant_id` from `TenantContext` when saved.
- `TenantRoutingDataSource` sends every tenant to the shared database today; a tenant with its own database
  later is a new entry in `DataSourceConfig`.
- Do not use native SQL queries in the modules: they bypass the tenant filter.

## How modules talk

Modules never use each other's repositories. They publish events (`common/events`), handled synchronously
in the same transaction, so a failing listener rolls everything back:

| Event | Published by | Handled by |
|---|---|---|
| `TenantCreatedEvent` | tenant (signup) | auth: roles · inventory: default warehouse · finance: chart of accounts |
| `OrderConfirmedEvent` | sales | inventory: reserves stock (or the confirmation fails with 409) |
| `OrderShippedEvent` | sales | inventory: issues the reserved stock |
| `OrderCancelledEvent` | sales | inventory: releases the reservation |
| `InvoicePostedEvent` | sales | finance: journal entry (Dr 1100, Cr 4000 and 2100) and receivable |
| `InvoicePaidEvent` | finance | sales: marks the invoice paid |

## Manual test script

Run this before each milestone demo. It covers the order-to-cash flow on two tenants and checks that
neither tenant can see the other's data. Start from a fresh database (`docker compose down -v`, then
`docker compose up --build`).

### 1. Order to cash in `acme`

1. Sign in to workspace `acme` as `sales`.
2. **Quotes → New quote**: customer *Northwind Retail*, one line of `LAP-14` × 2 at 1200.00, tax 19 %.
   The total shows 2,856.00. Create it.
3. On the quote, **Convert to order**. The order opens in status *Draft*.
4. **Confirm and reserve stock**. Status *Confirmed*. Sign in as `warehouse` and open **Stock**:
   `LAP-14` shows 25 on hand, 2 reserved, 23 available.
5. Still as `warehouse`, open the order and **Ship**. Status *Shipped*; stock shows 23 on hand, 0 reserved,
   and the movements list an *Issue* of -2 with the order number.
6. Sign in as `accountant`. Open the order, **Create invoice** (keep the dates), then **Post invoice**.
7. **Journal** shows the invoice entry: Dr 1100 2,856.00, Cr 4000 2,400.00, Cr 2100 456.00.
   **Trial balance** says *Balanced*.
8. **Receivables** lists the invoice as *Open*. **Record payment** for the full amount.
   It becomes *Paid*, the invoice shows *Paid*, and the journal has a payment entry (Dr 1000, Cr 1100).

### 2. Business rules

1. As `sales` in `acme`, create an order for `MON-27` × 1000 and confirm it: the app refuses with
   *Not enough MON-27 in stock* and the order stays *Draft*.
2. Cancel that order: status *Cancelled*.
3. As `sales`, the Finance menu is not shown, and opening `/finance/journal` directly sends you back to the dashboard.

### 3. Tenant isolation

1. Sign in to `globex` as `admin`. Customers, items, orders, invoices and journal show only Globex data:
   no Northwind, no laptops, no `acme` documents.
2. Note the id of an `acme` order (from its address, e.g. `/sales/orders/1`). In `globex`, open
   `/sales/orders/<that id>`: the app says the order was not found.
3. Optional, with the API: sign in to `globex` and call `GET /api/sales/orders/<acme order id>` with its token.
   The answer is `404 NOT_FOUND`.

```bash
TOKEN=$(curl -s localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"tenantCode":"globex","username":"admin","password":"demo1234"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl -i localhost:8080/api/sales/orders/1 -H "Authorization: Bearer $TOKEN"
```

### 4. Signup

On the sign-in page, choose **Create a workspace**, fill it in and submit. You land on the dashboard of
the new, empty workspace as its admin, with its own chart of accounts and a `MAIN` warehouse.

## API overview

All endpoints are under `/api` and need `Authorization: Bearer <token>`, except login, signup and health.
Errors always have the shape `{ code, message, details, traceId, timestamp }`.

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/login`, `POST /auth/signup`, `GET /auth/me`, `GET /auth/health` |
| Users (admin) | `GET /users`, `POST /users` |
| Customers | `GET/POST /sales/customers`, `GET/PUT /sales/customers/{id}` |
| Quotes | `GET/POST /sales/quotes`, `GET /sales/quotes/{id}`, `POST /sales/quotes/{id}/convert`, `POST /sales/quotes/{id}/reject` |
| Orders | `GET/POST /sales/orders`, `GET /sales/orders/{id}`, `POST /sales/orders/{id}/confirm`, `/ship`, `/cancel`, `/invoice` |
| Invoices | `GET /sales/invoices`, `GET /sales/invoices/{id}`, `POST /sales/invoices/{id}/post` |
| Items | `GET/POST /inventory/items`, `GET/PUT /inventory/items/{id}` |
| Warehouses | `GET/POST /inventory/warehouses` |
| Stock | `GET /inventory/stock`, `GET/POST /inventory/stock-moves` |
| Accounts | `GET/POST /finance/accounts` |
| Journal | `GET/POST /finance/journal-entries`, `GET /finance/journal-entries/{id}`, `GET /finance/trial-balance` |
| Receivables | `GET /finance/receivables?status=OPEN`, `GET /finance/receivables/aging` |
| Payments | `GET/POST /finance/payments` |

## Configuration

| Variable | Default | Meaning |
|---|---|---|
| `SPRING_DATASOURCE_URL`, `_USERNAME`, `_PASSWORD` | local MySQL | Database connection |
| `JWT_SECRET` | a development-only value | Token signing key, at least 32 characters. Always set it in production. |
| `BASE_DOMAIN` | empty | e.g. `orbiterp.app`, to tie `acme.orbiterp.app` to workspace `acme` |
| `DEMO_DATA_ENABLED` | `false` | Creates the demo workspaces above. Never enable it in production. |
