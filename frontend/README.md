# GameSphere — Frontend

React storefront for GameSphere: browse games and compare store offers, buy
marketplace products, top up in-game currency, and manage a cart, wishlist and
account.

It is a pure client — every piece of data comes from the
[Spring Boot API](../README.md), which must be running for the app to show
anything.

---

## Stack

| | |
|---|---|
| Framework | React 19 |
| Build tool | Vite 8 |
| Routing | React Router 7 (`createBrowserRouter`) |
| HTTP | axios, with a token interceptor |
| Icons | lucide-react |
| Styling | Plain CSS with custom properties — no framework |
| Linting | ESLint 10 (flat config) |

No state library: three React contexts cover everything the app shares.

---

## Running it

```bash
npm install
npm run dev
```

Opens on **http://localhost:5173**. Start the backend on port 8080 first —
`http://localhost:5173` is one of the origins its CORS policy allows.

| Script | What it does |
|---|---|
| `npm run dev` | Dev server with hot reload |
| `npm run build` | Production bundle into `dist/` |
| `npm run preview` | Serve the built bundle locally |
| `npm run lint` | ESLint over the whole project |

### Environment

Copy `.env.example` to `.env`:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_BACKEND_URL=http://localhost:8080
```

- **`VITE_API_BASE_URL`** — base for every API call. **It must include `/api`**:
  the client calls paths like `/games/search` and `/cart/items`, so the prefix
  lives here.
- **`VITE_BACKEND_URL`** — origin used to resolve uploaded images served from
  `/uploads`. Falls back to `http://localhost:8080`.
- **`VITE_AZN_PER_USD`** — rate the AZN display switch converts at. Defaults to
  `1.7`.

`.env` is gitignored; `.env.example` is the committed template.

---

## Layout

```
src/
├── account/        AccountProvider — session, cart and wishlist
├── api/            One module per API area (axios calls only)
├── app/            App shell and router
├── assets/         Store logos
├── components/     Shared UI: header, footer, menus, MediaImage
├── features/       Feature-scoped components (games, offers, top-ups, reviews)
├── i18n/           Translations and the useTranslation hook
├── layouts/        MainLayout (header + outlet + footer)
├── money/          CurrencyProvider — display currency and conversion
├── pages/          One component per route
├── styles/         Global tokens and per-area stylesheets
└── utils/          Formatting, media URLs, top-up helpers
```

The split that matters: **`api/` never renders and components never call axios
directly.** A page asks a module in `api/`, or reads from a context.

---

## Routes

| Path | Page |
|---|---|
| `/` | Home |
| `/games` | Game catalogue with filters |
| `/games/:slug` | Game detail, store offers, reviews |
| `/marketplace` | Marketplace products |
| `/top-ups` | Top-up catalogue, grouped by game |
| `/top-ups/:slug` | Packages for one game |
| `/checkout` | Payment method and cart summary |
| `/payment/success`, `/payment/cancel` | Where Stripe returns to |
| `/account`, `/account/:section` | Dashboard — `profile`, `orders`, `wishlist`, `notifications` |
| `/tournaments` | Placeholder section |
| `*` | Not found |

---

## The three contexts

### `I18nProvider` (`src/i18n/`)

Four languages — **EN, AZ, TR, RU**. EN and AZ are written out in full; TR and
RU are merged over EN, so any key they do not translate falls back to English
rather than rendering blank. The chosen language persists in `localStorage`.

```jsx
const { t, language, setLanguage } = useTranslation();
t("topUps.packageCount", { count: 7 });   // "7 packages"
```

A missing key renders its own path (`topUps.badge`), which makes an out-of-date
translation file obvious on screen instead of silent.

### `CurrencyProvider` (`src/money/`)

The display currency, covered under [Money and currency](#money-and-currency).

### `AccountProvider` (`src/account/`)

Holds the session and both baskets, so the header badges and every page agree
without prop drilling:

```jsx
const {
  user, isAuthenticated,
  cart, cartCount, wishlist, wishlistCount,
  signIn, createAccount, signOut,
  addToCart, removeFromCart, toggleWishlist,
} = useAccount();
```

`basketsLoading` stays true until the first cart and wishlist read settles, so
a page can tell "not fetched yet" from "really empty" — without it `/checkout`
greeted a full cart with "Your cart is empty" on a direct load.

Tokens live in `localStorage` and `api/httpClient.js` attaches the access token
to every request. If the backend answers 401 to a cart or wishlist read, the
stored session is dropped rather than leaving a signed-in shell that cannot
load.

---

## Header menus

The three header icons each open a panel: **account** (sign in / register, or
identity and links), **wishlist** and **cart** (contents, per-row removal,
total). Each carries a count badge.

A mouse opens them on hover; a short close delay plus a CSS bridge over the
gap lets the pointer travel from the button into the panel. Clicking still
toggles, and is the only way in where `(hover: hover) and (pointer: fine)`
does not match — on touch, hover either does not exist or sticks after a tap.
They close on outside click and on `Escape`.

There is no "sign in with Google / Facebook" — the backend exposes no OAuth
flow, and a button that cannot do anything is worse than no button.

---

## Money and currency

A product is **stored and charged** in its own currency. On top of that,
`CurrencyProvider` adds a **display currency** the shopper picks in the header
(AZN by default, USD the alternative), converted at `VITE_AZN_PER_USD`:

```jsx
const { formatPrice, isConverted, displayCurrency } = useMoney();
formatPrice(product.finalPrice, product.currency);   // "₼37.37"
```

Conversion is presentation only. Checkout states the real charge — *"Shown in
AZN. The payment is charged in USD — 21.98 USD."* — so the number on the card
statement is never a surprise. A currency with no configured rate is printed as
it is rather than converted at a guess.

---

## Checkout

The cart's **Checkout** goes to `/checkout`, which lists the payment methods
the backend can really take and summarises the cart. Paying runs:

```
POST /api/order/from-cart          → { id }
POST /api/payments/stripe/checkout → { checkoutUrl }
window.location.assign(checkoutUrl)
```

**There is no card form in this codebase, and there should not be one.** Card
number and CVV fields on our own page would put the project in PCI-DSS SAQ D
scope. Stripe Checkout is hosted on Stripe's domain, which keeps it at SAQ A.
If the form ever needs to sit inside our own page, the answer is Stripe
Elements — the inputs are still Stripe iframes — not hand-written inputs.

Stripe returns to `/payment/success` or `/payment/cancel`. Those must match
`STRIPE_SUCCESS_URL` and `STRIPE_CANCEL_URL` on the backend, which default to
port **3000** while this app runs on **5173** — set them.

---

## Conventions

**Money.** Two helpers in `utils/formatters.js`:

- `formatCurrency(amount, currency)` when the currency is known.
- `formatMoney(amount, currency)` when it might not be — it prints a bare
  number rather than guessing a symbol.

Use the second wherever the API may omit a currency. Printing `$16.99` for an
AZN price is a real bug, not a cosmetic one.

**Styling.** Colours, spacing, radii and typography come from
`styles/variables.css`. Reach for a token before writing a literal. Global
rules live in `styles/global.css`; anything feature-specific gets its own file
imported by the component that needs it.

`styles/global.css` is loaded last, so a page stylesheet that overrides a
global rule needs matching specificity — see `.status-panel.status-panel--inline`
in `styles/top-ups.css`.

**Requests.** Every fetch takes an `AbortSignal` and is cancelled on unmount.
Where two requests are independent, use `Promise.allSettled` so one failure
degrades that piece only — the top-up catalogue still renders if the cover-art
request fails.

**Responsive.** Every page works down to 400px. Grids collapse through 1000px,
800px and 560px breakpoints.

---

## Data the UI depends on

Two fields are worth checking in the admin data if the catalogue looks wrong:

- **`game.catalogType`** must be `TOP_UP` for a game to appear in the top-up
  catalogue with its cover art.
- **`product.inGameCurrencyName`** should hold the currency (`CP`, `UC`), not
  the game title. When it merely repeats the game name the client drops it, so
  a package reads `2400` instead of `2400 Call of Duty Warzone`.

Packages with no artwork are not broken: the denomination is rendered as a gold
plate whose weight grows with the tier. Upload a product image and it replaces
the plate.
