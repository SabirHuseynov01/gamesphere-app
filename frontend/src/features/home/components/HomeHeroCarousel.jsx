import { ArrowRight, CalendarDays, ChevronLeft, ChevronRight } from "lucide-react";
import { Link } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";

import { useTranslation } from "../../../i18n/index.jsx";
import { formatDateTime } from "../../../utils/formatters.js";
import { resolveMediaUrl } from "../../../utils/mediaUrl.js";
import { getGameArtwork } from "../../../utils/gameArtwork.js";

function shortenDescription(description, maxLength = 190) {
    if (!description) return "";
    const text = String(description).trim();
    return text.length > maxLength ? `${text.slice(0, maxLength).trim()}...` : text;
}

function shuffle(items) {
    const shuffled = [...items];

    for (let index = shuffled.length - 1; index > 0; index -= 1) {
        const randomIndex = Math.floor(Math.random() * (index + 1));
        [shuffled[index], shuffled[randomIndex]] = [shuffled[randomIndex], shuffled[index]];
    }

    return shuffled;
}

export default function HomeHeroCarousel({ games }) {
    const { t } = useTranslation();
    const slides = useMemo(() => shuffle(games.filter((game) => game?.slug || game?.id)).slice(0, 6), [games]);
    const [activeIndex, setActiveIndex] = useState(0);
    const [isPaused, setIsPaused] = useState(false);

    useEffect(() => {
        if (slides.length < 2 || isPaused) return undefined;
        const timer = window.setInterval(() => setActiveIndex((current) => (current + 1) % slides.length), 6500);
        return () => window.clearInterval(timer);
    }, [isPaused, slides.length]);

    if (!slides.length) return null;

    const safeIndex = activeIndex % slides.length;
    const game = slides[safeIndex];
    const gamePath = game.slug ? `/games/${game.slug}` : `/games/${game.id}`;
    const artwork = getGameArtwork(game, "hero") || getGameArtwork(game, "background") || getGameArtwork(game, "cover");
    const imageUrl = artwork ? resolveMediaUrl(artwork) : null;

    function goTo(index) {
        setActiveIndex((index + slides.length) % slides.length);
    }

    return (
        <section className="home-hero-carousel" aria-label={t("home.featured")} onMouseEnter={() => setIsPaused(true)} onMouseLeave={() => setIsPaused(false)} onFocus={() => setIsPaused(true)} onBlur={(event) => { if (!event.currentTarget.contains(event.relatedTarget)) setIsPaused(false); }}>
            <div className="home-hero-carousel__artwork" key={game.id || game.slug}>
                {imageUrl ? <img src={imageUrl} alt="" aria-hidden="true" /> : <div className="home-hero-carousel__artwork-fallback" />}
            </div>
            <div className="home-hero-carousel__overlay" />
            <div className="container home-hero-carousel__content">
                <p className="home-hero__eyebrow">{t("home.featuredEyebrow")}</p>
                <h1>{game.title}</h1>
                <p className="home-hero__description">{shortenDescription(game.description) || t("home.description")}</p>
                <div className="home-hero__facts">
                    {(game.developer || game.publisher) && <span>{game.developer || game.publisher}</span>}
                    {game.releaseDate && <span><CalendarDays size={15} />{formatDateTime(game.releaseDate)}</span>}
                </div>
                <div className="home-hero__actions">
                    <Link className="primary-action" to={gamePath}>{t("home.compareOffers")} <ArrowRight size={18} /></Link>
                    <Link className="secondary-action" to={gamePath}>{t("home.browse")}</Link>
                </div>
            </div>
            {slides.length > 1 && <div className="home-hero-carousel__controls" aria-label={t("home.featured")}>
                <button type="button" className="home-hero-carousel__arrow" onClick={() => goTo(safeIndex - 1)} aria-label="Previous slide"><ChevronLeft size={20} /></button>
                <div className="home-hero-carousel__dots">
                    {slides.map((slide, index) => <button type="button" className={`home-hero-carousel__dot ${index === safeIndex ? "is-active" : ""}`} key={slide.id || slide.slug || index} onClick={() => goTo(index)} aria-label={`Slide ${index + 1}`} aria-current={index === safeIndex ? "true" : undefined} />)}
                </div>
                <button type="button" className="home-hero-carousel__arrow" onClick={() => goTo(safeIndex + 1)} aria-label="Next slide"><ChevronRight size={20} /></button>
            </div>}
        </section>
    );
}
