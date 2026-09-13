import { Outlet, useLocation } from "react-router-dom";
import { useEffect } from "react";

import Footer from "../components/navigation/Footer.jsx";
import Header from "../components/navigation/Header.jsx";

export default function MainLayout() {
    const { pathname } = useLocation();

    useEffect(() => {
        if (pathname === "/") document.title = "GameSphere";
        else if (pathname.startsWith("/games/")) document.title = "Games | GameSphere";
        else if (pathname.startsWith("/games")) document.title = "Games | GameSphere";
        else if (pathname.startsWith("/marketplace")) document.title = "Marketplace | GameSphere";
        else if (pathname.startsWith("/top-ups")) document.title = "Top-ups | GameSphere";
        else if (pathname.startsWith("/tournaments")) document.title = "Tournaments | GameSphere";
        else document.title = "GameSphere";
    }, [pathname]);

    return (
        <div className="app-shell">
            <Header />
            <main className="app-main">
                <Outlet />
            </main>
            <Footer />
        </div>
    );
}
