import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { router } from "./app/router.jsx";
import { I18nProvider } from "./i18n/index.jsx";
import { AccountProvider } from "./account/AccountProvider.jsx";
import "./styles/reset.css";
import "./styles/variables.css";
import "./styles/global.css";

createRoot(document.getElementById("root")).render(
    <StrictMode>
        <I18nProvider>
            <AccountProvider>
                <RouterProvider router={router} />
            </AccountProvider>
        </I18nProvider>
    </StrictMode>,
);
