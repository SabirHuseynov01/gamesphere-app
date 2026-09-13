import { useEffect, useMemo, useState } from "react";
import { Star } from "lucide-react";

import { createGameReview, getApprovedGameReviews } from "../../api/reviewApi.js";
import { getApiErrorMessage } from "../../api/httpClient.js";
import { useTranslation } from "../../i18n/index.jsx";

function Stars({ value, size = 16 }) {
    return <span className="review-stars" aria-label={`${value} / 5`}>
        {[1, 2, 3, 4, 5].map((star) => <Star key={star} size={size} fill={star <= Math.round(value) ? "currentColor" : "none"} />)}
    </span>;
}

export default function ReviewSection({ gameId, productId }) {
    const { t } = useTranslation();
    const [reviews, setReviews] = useState([]);
    const [loadedGameId, setLoadedGameId] = useState(null);
    const [error, setError] = useState("");
    const [formOpen, setFormOpen] = useState(false);
    const [rating, setRating] = useState("5");
    const [comment, setComment] = useState("");
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const controller = new AbortController();
        getApprovedGameReviews(gameId, controller.signal).then(setReviews).catch((requestError) => {
            if (requestError.name !== "CanceledError") setError(getApiErrorMessage(requestError));
        }).finally(() => { if (!controller.signal.aborted) setLoadedGameId(gameId); });
        return () => controller.abort();
    }, [gameId]);

    const statistics = useMemo(() => {
        const distribution = [5, 4, 3, 2, 1].map((score) => ({ score, count: reviews.filter((review) => Math.round(review.rating) === score).length }));
        const average = reviews.length ? reviews.reduce((sum, review) => sum + Number(review.rating || 0), 0) / reviews.length : 0;
        return { average, distribution };
    }, [reviews]);

    async function submitReview(event) {
        event.preventDefault();
        setSubmitting(true);
        setError("");
        try {
            const created = await createGameReview({ gameId, productId, rating, comment });
            setReviews((current) => [created, ...current]);
            setComment("");
            setFormOpen(false);
        } catch (requestError) { setError(getApiErrorMessage(requestError)); }
        finally { setSubmitting(false); }
    }

    return <section className="reviews-section" aria-labelledby="reviews-title">
        <div className="reviews-section__header">
            <div><p className="game-details__eyebrow">{t("reviews.eyebrow")}</p><h2 id="reviews-title">{t("reviews.title")}</h2></div>
            <button type="button" className="reviews-section__write" onClick={() => setFormOpen((open) => !open)}>{t("reviews.write")}</button>
        </div>
        {error && <p className="reviews-section__error">{error}</p>}
        <div className="reviews-summary">
            <div className="reviews-summary__average"><strong>{statistics.average ? statistics.average.toFixed(1) : "0.0"}</strong><Stars value={statistics.average} /><span>{t("reviews.count", { count: reviews.length })}</span></div>
            <div className="reviews-summary__distribution">{statistics.distribution.map(({ score, count }) => <div className="review-bar" key={score}><span>{score}</span><Stars value={score} size={13} /><div><span style={{ width: `${reviews.length ? (count / reviews.length) * 100 : 0}%` }} /></div><small>{count}</small></div>)}</div>
        </div>
        {formOpen && <form className="review-form" onSubmit={submitReview}><label>{t("reviews.rating")}<select value={rating} onChange={(event) => setRating(event.target.value)}>{[5, 4.5, 4, 3.5, 3, 2.5, 2, 1.5, 1].map((value) => <option key={value}>{value}</option>)}</select></label><label>{t("reviews.comment")}<textarea required minLength={3} value={comment} onChange={(event) => setComment(event.target.value)} /></label><button type="submit" disabled={submitting}>{submitting ? t("common.loading") : t("reviews.submit")}</button></form>}
        {loadedGameId !== gameId && <p className="reviews-section__muted">{t("common.loading")}</p>}
        {loadedGameId === gameId && reviews.length === 0 && <p className="reviews-section__muted">{t("reviews.empty")}</p>}
        <div className="review-list">{reviews.map((review) => <article className="review-item" key={review.id}><div className="review-item__top"><strong>{review.username}</strong><Stars value={review.rating} /><time>{review.createdAt?.slice(0, 10)}</time></div><p>{review.comment}</p></article>)}</div>
    </section>;
}
