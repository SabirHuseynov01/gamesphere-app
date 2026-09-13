import { useTranslation } from "../../i18n/index.jsx";

export default function Footer() {
    const { t } = useTranslation();
    return (
        <footer className="site-footer">
            <div className="container footer-inner">
                <strong>GameSphere</strong>
                <span>© {new Date().getFullYear()} {t("footer.comparison")}</span>
            </div>
        </footer>
    );
}
