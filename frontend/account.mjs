import { chromium } from "playwright";

const OUT = "/tmp/claude-0/-home-user-gamesphere-app/5ac501d1-e48c-522a-88fe-b8f0766d520c/scratchpad";
const wrap = (data) => ({ success: true, message: "ok", data, errorCode: null });

const profile = {
    id: 1, username: "SabirHuseynov", email: "huseynovsabir8569@gmail.com",
    firstName: "Sabir", lastName: "Hüseynov", avatarUrl: null,
    phoneNumber: "+994 50 123 45 67", address: "Baku", balance: 42.5, isSeller: true,
};

const orders = [
    { id: 1, orderNumber: "GS-2026-0041", status: "COMPLETED", totalAmount: 33.98, createdAt: "2026-09-10T18:22:00" },
    { id: 2, orderNumber: "GS-2026-0038", status: "PENDING", totalAmount: 16.98, createdAt: "2026-09-08T11:05:00" },
];

const notifications = [
    { id: 1, type: "ORDER", status: "UNREAD", title: "Order delivered", message: "GS-2026-0041 has been fulfilled.", createdAt: "2026-09-10T18:40:00" },
    { id: 2, type: "SYSTEM", status: "READ", title: "Welcome to GameSphere", message: "Your account is ready.", createdAt: "2026-09-01T09:00:00" },
];

const wishlist = {
    wishlistId: 1,
    products: [{ id: 21, name: "60 PUBG Mobile", gameTitle: "PUBG Mobile", finalPrice: 16.99, currency: "AZN", productType: "IN_GAME_CURRENCY" }],
};

const cart = { cartId: 1, items: [], totalAmount: 0, currency: null };

const browser = await chromium.launch({ executablePath: "/opt/pw-browsers/chromium-1194/chrome-linux/chrome" });
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });

await page.addInitScript(() => {
    localStorage.setItem("accessToken", "test-token");
    localStorage.setItem("gamesphere-user", JSON.stringify({ username: "SabirHuseynov", email: "huseynovsabir8569@gmail.com" }));
});

await page.route("**/users/profile", (r) => r.fulfill({
    json: wrap(r.request().method() === "PUT" ? { ...profile, ...r.request().postDataJSON() } : profile),
}));
await page.route("**/order/my**", (r) => r.fulfill({ json: wrap({ content: orders }) }));
await page.route("**/notifications**", (r) => r.fulfill({ json: wrap(notifications) }));
await page.route("**/wishlist**", (r) => r.fulfill({ json: wrap(wishlist) }));
await page.route("**/cart**", (r) => r.fulfill({ json: wrap(cart) }));
await page.route("**/games/search**", (r) => r.fulfill({ json: wrap({ content: [] }) }));
await page.route("**/products**", (r) => r.fulfill({ json: wrap({ content: [] }) }));

const errors = [];
page.on("console", (m) => m.type() === "error" && errors.push(m.text()));
page.on("pageerror", (e) => errors.push(String(e)));

await page.goto("http://localhost:5173/account", { waitUntil: "networkidle" });
await page.waitForSelector(".account-form");
await page.screenshot({ path: `${OUT}/40-dashboard.png`, fullPage: true });

await page.click('a[href="/account/orders"]');
await page.waitForSelector(".account-rows");
await page.screenshot({ path: `${OUT}/41-orders.png`, fullPage: true });

await page.click('a[href="/account/notifications"]');
await page.waitForSelector(".account-rows li.is-unread");
await page.screenshot({ path: `${OUT}/42-notifications.png`, fullPage: true });

// Save the profile form and confirm the success note appears.
await page.click('a[href="/account"]');
await page.waitForSelector(".account-form");
await page.fill('.account-form__wide input', "Baku, Azerbaijan");
await page.click('.account-form__actions button');
await page.waitForSelector(".account-form__note.is-success");
console.log("save note:", await page.locator(".account-form__note.is-success").innerText());

await page.setViewportSize({ width: 420, height: 900 });
await page.waitForTimeout(300);
await page.screenshot({ path: `${OUT}/43-dashboard-mobile.png`, fullPage: true });

console.log(errors.length ? `CONSOLE ERRORS:\n${errors.join("\n")}` : "no console errors");
await browser.close();