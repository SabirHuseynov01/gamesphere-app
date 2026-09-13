import { createBrowserRouter } from "react-router-dom";

import AccountPage from "../pages/AccountPage.jsx";
import MainLayout from "../layouts/MainLayout.jsx";
import GameDetailsPage from "../pages/GameDetailsPage.jsx";
import BrowseGamesPage from "../pages/BrowseGamesPage.jsx";
import HomePage from "../pages/HomePage.jsx";
import NotFoundPage from "../pages/NotFoundPage.jsx";
import SectionPage from "../pages/SectionPage.jsx";
import MarketplacePage from "../pages/MarketplacePage.jsx";
import TopUpDetailsPage from "../pages/TopUpDetailsPage.jsx";
import TopUpsPage from "../pages/TopUpsPage.jsx";

export const router = createBrowserRouter([
    {
        element: <MainLayout />,
        children: [
            {
                path: "/",
                element: <HomePage />,
            },
            {
                path: "/games",
                element: <BrowseGamesPage />,
            },
            {
                path: "/games/:slug",
                element: <GameDetailsPage />,
            },
            {
                path: "/marketplace",
                element: <MarketplacePage />,
            },
            {
                path: "/top-ups",
                element: <TopUpsPage />,
            },
            {
                path: "/top-ups/:slug",
                element: <TopUpDetailsPage />,
            },
            {
                path: "/account",
                element: <AccountPage />,
            },
            {
                path: "/account/:section",
                element: <AccountPage />,
            },
            {
                path: "/tournaments",
                element: <SectionPage title="Tournaments" />,
            },
            {
                path: "*",
                element: <NotFoundPage />,
            },
        ],
    },
]);
