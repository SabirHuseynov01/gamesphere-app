# GameSphere — Backend

Spring Boot REST API behind GameSphere: a game catalogue with price comparison,
a digital marketplace, in-game top-ups, tournaments and the commerce around
them (cart, orders, payments, digital code delivery).

The React client lives in [`frontend/`](frontend/README.md) and talks to this
API over HTTP.

---

## Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 (Web, Data JPA, Security, Validation, Cache, Mail) |
| Database | PostgreSQL 16, schema managed by Liquibase |
| Auth | JWT access + refresh tokens (jjwt 0.12) |
| Mapping | MapStruct 1.6 (Spring component model) |
| Payments | Stripe Java SDK 32 (optional, off by default) |
| Docs | springdoc-openapi (Swagger UI) |
| Build | Gradle wrapper |
| Tests | JUnit 5, Mockito, AssertJ, Testcontainers |

---

## Running it

### 1. Database

`docker-compose.yml` brings up the Postgres this project expects — note the
host port is **5433**, not 5432:

```bash
docker compose up -d
```

| | |
|---|---|
| Database | `gamesphere_db` |
| User / password | `gamesphere` / `gamesphere` |
| Port | `5433` |

### 2. Application

```bash
./gradlew bootRun          # macOS / Linux
gradlew.bat bootRun        # Windows
```

The API comes up on **http://localhost:8080**. Liquibase applies the 21
changelogs in `src/main/resources/db/changelog/` on first start, and JPA runs
with `ddl-auto: validate`, so the schema comes from migrations only — never
from entity scanning.

### 3. Swagger

<http://localhost:8080/swagger-ui.html> — every endpoint, grouped by
controller, with request and response schemas.

---

## Configuration

Everything below has a working default, so the app starts with no environment
set. Override through environment variables:

| Variable | Default | What it does |
|---|---|---|
| `JWT_SECRET` | a dev key in `application.yaml` | HMAC key for signing tokens. **Set your own before deploying.** |
| `JWT_ACCESS_EXPIRATION` | `900000` (15 min) | Access token lifetime, ms |
| `JWT_REFRESH_EXPIRATION` | `2592000000` (30 days) | Refresh token lifetime, ms |
| `APP_STORAGE_UPLOAD_DIR` | `~/gamesphere-uploads` | Where uploaded covers and product images land |
| `APP_FRONTEND_URL` | `http://localhost:3000` | Used in outgoing email links |
| `app.cors.allowed-origins` | `http://localhost:3000,http://localhost:5173` | Browser origins allowed to call the API |
| `MAIL_ENABLED` | `false` | Turn on SMTP delivery |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail SMTP / empty | Mail transport |
| `STRIPE_ENABLED` | `false` | Turn on Stripe checkout |
| `STRIPE_SECRET_KEY` / `STRIPE_WEBHOOK_SECRET` | empty | Stripe credentials |
| `STRIPE_CURRENCY` | `usd` | Checkout currency |

Mail and Stripe are **disabled by default**, so local development needs no
third-party accounts.

---

## API

Every response is wrapped in the same envelope:

```json
{
  "success": true,
  "message": "Games retrieved",
  "data": { },
  "errorCode": null
}
```

Paginated endpoints put Spring's page in `data` as `{ content, totalElements, ... }`.

### Endpoints by area

| Base path | Area |
|---|---|
| `/api/auth` | Register, login, refresh, logout |
| `/api/users` | Own profile: read and update |
| `/api/games` | Game catalogue, search, slug lookup, offers, cover upload |
| `/api/products` | Marketplace and top-up products, images, digital codes |
| `/api/categories` | Category tree |
| `/api/search` | Cross-catalogue search |
| `/api/cart` | Cart read, add, remove, clear |
| `/api/order` | Place an order, from the cart or explicit items; own order history |
| `/api/payments`, `/api/payments/stripe` | Payment intents and the Stripe webhook |
| `/api/wishlist` | Wishlist read, add, remove, clear |
| `/api/library` | Owned entitlements |
| `/api/reviews` | Game and product reviews |
| `/api/gifts` | Gifting digital products |
| `/api/tournaments` | Tournaments and participation |
| `/api/notifications` | Own notifications, unread count, mark read |
| `/api/seller` | Seller profile |
| `/api/top-up-fulfillments` | Top-up fulfilment status |
| `/api/admin`, `/api/admin/email`, `/api/admin/top-up-fulfillments` | Admin tooling |

### Authentication

`POST /api/auth/login` returns an access token and a refresh token. Send the
access token on every protected call:

```
Authorization: Bearer <accessToken>
```

When it expires, `POST /api/auth/refresh` rotates the pair — the old refresh
token is invalidated, so a replayed token is rejected.

Roles seeded by migration: `ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`.

Public without a token: registration and login, the Stripe webhook, Swagger,
uploaded files under `/uploads/**`, and read-only catalogue browsing (games,
search, categories, active products, product reviews). Everything else needs
authentication; admin and seller routes need the matching role.

---

## Domain model

24 entities under `entity/`. The ones worth knowing:

- **User / Role / SellerProfile / RefreshToken** — accounts, authorities, seller
  status and token rotation.
- **Game** — a title. `catalogType` (`GAME`, `TOP_UP`, `BOTH`) decides whether it
  shows in the game catalogue, the top-up catalogue, or both.
- **Product** — something purchasable attached to a game: a key, a DLC, an
  in-game currency package. `catalogSection` splits `MARKETPLACE` from `TOP_UPS`;
  `productType` and `deliveryType` decide how it is fulfilled.
- **Cart / CartItem → Order / OrderItem → Payment** — the purchase path.
- **DigitalCode / TopUpFulfillment / UserEntitlement** — delivery after payment,
  by code, by player-account top-up, or by granting an entitlement.
- **Review, Wishlist, Gift, Notification, Tournament, TournamentParticipant** —
  the surrounding features.

### One rule worth stating

A top-up product may set `requiresPlayerId`. The **cart accepts it without an
id** — a shopper may not have looked it up yet — but **order placement refuses**
to proceed until one is supplied. The id is needed at fulfilment, not while
browsing.

---

## Project layout

```
src/main/java/com/example/gamesphere/
├── config/          Security, CORS, Swagger, cache, scheduling
├── controller/      22 REST controllers
├── dto/             request/ and response/ payloads
├── entity/          JPA entities
├── enums/           Domain enums
├── event/ listener/ Application events (mail, fulfilment)
├── exception/       Domain exceptions + global handler
├── mapper/          MapStruct entity ↔ DTO
├── repository/      Spring Data repositories
├── scheduler/       6 scheduled jobs
├── security/        JWT filter, user details, token service
├── specification/   JPA Specifications for filtered search
├── util/ validation/
└── src/main/resources/db/changelog/   Liquibase migrations
```

### Scheduled jobs

`GiftExpiryScheduler`, `OfferRefreshScheduler`, `ProductStatusScheduler`,
`RefreshTokenCleanupScheduler`, `TournamentReminderScheduler`,
`TournamentStatusScheduler`.

---

## Tests

```bash
./gradlew test
```

34 test classes. Service tests are plain Mockito; the integration tests under
`src/test/java/.../integration/` use **Testcontainers** and are annotated
`@Testcontainers(disabledWithoutDocker = true)`, so they skip cleanly when
Docker is not running instead of failing the build.

Reports land in `build/reports/tests/test/index.html`.

---

## Database notes

Schema changes go in a **new** file under
`src/main/resources/db/changelog/changes/`, numbered in sequence, and get
registered in `db.changelog-master.yaml`. Never edit an applied changeset —
Liquibase checksums them and will refuse to start.

Resetting a local database is covered in
[`docs/liquibase-database-reset.md`](docs/liquibase-database-reset.md).
