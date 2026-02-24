# Server Warehouse

## Table of Contents
- [1. Overview](#1-overview)
- [2. Tech Stack and Locked Versions](#2-tech-stack-and-locked-versions)
- [3. API Response Contract](#3-api-response-contract)
- [4. Project Structure](#4-project-structure)
- [5. Environment Configuration](#5-environment-configuration)
- [6. Database Migrations and Seed Data](#6-database-migrations-and-seed-data)
- [7. Running the Service](#7-running-the-service)
- [8. Swagger and Default API Inputs](#8-swagger-and-default-api-inputs)
- [9. API Endpoints](#9-api-endpoints)
- [10. API Examples](#10-api-examples)
- [11. Validation and Error Behavior](#11-validation-and-error-behavior)
- [12. Design Decisions](#12-design-decisions)
- [13. Assumptions](#13-assumptions)
- [14. Troubleshooting](#14-troubleshooting)

## 1. Overview
This project is a warehouse management REST API for:
- items
- item variants
- inventory stock tracking
- sales transactions with stock deduction

The implementation is modular, transaction-safe for sales, and integrated with Swagger/OpenAPI for direct API testing.

## 2. Tech Stack and Locked Versions
- Java: `17.0.2`
- Spring Boot parent: `3.5.11`
- PostgreSQL JDBC: `42.7.10`
- Springdoc OpenAPI UI: `3.0.1`
- Maven Enforcer Plugin: `3.6.2`
- Required Maven version: `3.9.12` only

Maven version enforcement is implemented in:
- `.tool-versions` with `maven 3.9.12`
- `pom.xml` enforcer rule with exact range `[3.9.12]`
- `scripts/run-migrations.sh` hard check for `3.9.12`

Springdoc `3.0.1` requires transitive exclusion of Spring Boot 4 modules in `pom.xml` so runtime behavior remains aligned with Spring Boot `3.5.11`.

## 3. API Response Contract
All endpoints return:

```json
{
  "code": "00 | 09 | 99",
  "message": "string for 00 or 09, object for 99",
  "data": {}
}
```

Rules:
- `code = "00"` for success
- `code = "09"` for pending
- `code = "99"` for failure
- `message` for `99` is:
  - `{ what, why, how }` for internal and business errors
  - `{ originalCode, originalMessage }` for external API errors
  - `{ fieldName: expectedValue }` for validation failures
- `data` is omitted when there is no payload

## 4. Project Structure

```text
server_warehouse/
  db/
    migrations/
      001_schema/
      002_seed/
  scripts/
    run-migrations.sh
  src/main/java/com/greateastern/warehouse/
    common/
    config/
    item/
    migration/
    sale/
    variant/
  src/main/resources/
    application.yml
```

## 5. Environment Configuration
Create `server_warehouse/.env`:

```bash
SUPABASE_DATABASE_URL=https://project-ref.supabase.co
SUPABASE_DATABASE_USERNAME=postgres
SUPABASE_DATABASE_PASSWORD=change_me
SUPABASE_PROJECT_URL=https://project-ref.supabase.co
SUPABASE_ANON_KEY=change_me
SUPABASE_SERVICE_ROLE_KEY=change_me
SUPABASE_DATABASE_NAME=postgres
SUPABASE_DATABASE_SSLMODE=require
PORT=8080
```

Minimum required values:
- `SUPABASE_DATABASE_URL` or `SUPABASE_PROJECT_URL`
- `SUPABASE_DATABASE_PASSWORD`

Accepted database URL formats:
- `jdbc:postgresql://...`
- `postgresql://...`
- `https://<project-ref>.supabase.co`

## 6. Database Migrations and Seed Data
Migrations are split by purpose and run in deterministic sorted order.

Schema files:
- `db/migrations/001_schema/001_create_items.sql`
- `db/migrations/001_schema/002_create_item_variants.sql`
- `db/migrations/001_schema/003_create_sales.sql`
- `db/migrations/001_schema/004_create_sale_lines.sql`
- `db/migrations/001_schema/005_constraints_and_indexes.sql`

Seed files:
- `db/migrations/002_seed/001_seed_items.sql`
- `db/migrations/002_seed/002_seed_variants.sql`
- `db/migrations/002_seed/003_seed_sales.sql`
- `db/migrations/002_seed/004_seed_sale_lines.sql`
- `db/migrations/002_seed/005_sync_sequences.sql`

Seed identifiers used by Swagger defaults:
- Item IDs: `1001`, `1002`
- Variant IDs: `2001`, `2002`
- Sale ID: `3001`
- Sale line ID: `4001`
- Sale reference: `MIG-SEED-SALE-3001`

Run all migrations:

```bash
./scripts/run-migrations.sh
```

## 7. Running the Service
1. Activate Java `17.0.2`.
2. Activate Maven `3.9.12`.
3. Run migrations:

```bash
./scripts/run-migrations.sh
```

4. Start application:

```bash
mvn -q spring-boot:run
```

5. Open:
- `http://localhost:8080/`
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

## 8. Swagger and Default API Inputs
Swagger is configured with defaults and examples so each API can be executed directly.

Default query parameter values:
- `GET /api/items?activeOnly=true`
- `GET /api/items/{itemId}/variants?activeOnly=true`
- `GET /api/sales?reference=MIG-SEED-SALE-3001`

Default path parameter examples:
- `itemId=1001`
- `variantId=2001`
- `saleId=3001`

Default request body values:
- `POST /api/items`
- `PUT /api/items/{itemId}`
- `POST /api/items/{itemId}/variants`
- `PUT /api/variants/{variantId}`
- `POST /api/sales`

## 9. API Endpoints
Item endpoints:
- `POST /api/items`
- `GET /api/items`
- `GET /api/items/{itemId}`
- `PUT /api/items/{itemId}`
- `DELETE /api/items/{itemId}`

Variant endpoints:
- `POST /api/items/{itemId}/variants`
- `GET /api/items/{itemId}/variants`
- `GET /api/variants/{variantId}`
- `PUT /api/variants/{variantId}`
- `DELETE /api/variants/{variantId}`

Sale endpoints:
- `POST /api/sales`
- `GET /api/sales`
- `GET /api/sales/{saleId}`

## 10. API Examples
Create item:

```bash
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Migration Demo Item",
    "description": "Item created from Swagger using default request body",
    "active": true
  }'
```

List variants:

```bash
curl "http://localhost:8080/api/items/1001/variants?activeOnly=true"
```

Create sale:

```bash
curl -X POST http://localhost:8080/api/sales \
  -H "Content-Type: application/json" \
  -d '{
    "reference": "",
    "lines": [
      { "variantId": 2001, "quantity": 1 }
    ]
  }'
```

## 11. Validation and Error Behavior
Validation failures return:

```json
{
  "code": "99",
  "message": {
    "fieldName": "expected value"
  }
}
```

Business and unexpected failures return:

```json
{
  "code": "99",
  "message": {
    "what": "error category",
    "why": "technical reason",
    "how": "recommended next action"
  }
}
```

## 12. Design Decisions
- Domain modules (`item`, `variant`, `sale`) isolate concerns.
- Services own business logic and transaction boundaries.
- Sale creation uses row locking to prevent overselling.
- Global exception handling keeps response shape consistent.
- Data contracts use explicit types.

## 13. Assumptions
- Empty sale `reference` is allowed and auto-generated.
- Missing `active` input on item and variant is treated as `true`.
- Seed data is intended for immediate Swagger testing.

## 14. Troubleshooting
- If Maven check fails, switch to Maven `3.9.12`.
- If startup fails with datasource errors, verify `SUPABASE_*` values.
- If migrations fail to connect, verify network and Supabase host resolution.
- Rotate credentials before production use if they were ever shared publicly.
