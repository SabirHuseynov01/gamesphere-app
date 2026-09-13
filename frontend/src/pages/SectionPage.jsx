import { useTranslation } from "../i18n/index.jsx";

export default function SectionPage({ title }) {
    const { t } = useTranslation();
    const translatedTitle = title === "Tournaments" ? t("nav.tournaments") : title;

    return (
        <div className="container">
            <section className="browse-header">
                <div>
                    <h1>{translatedTitle}</h1>
                </div>
            </section>
        </div>
    );
}
