import { CalendarDays } from "lucide-react";
import { Link } from "react-router-dom";

import { formatEnum } from "../../../utils/formatters.js";
import { resolveMediaUrl } from "../../../utils/mediaUrl.js";
import { useTranslation } from "../../../i18n/index.jsx";
import MediaImage from "../../../components/MediaImage.jsx";
import { getGameArtwork } from "../../../utils/gameArtwork.js";

export default function GameCard({ game }) {
    const { t } = useTranslation();
    const coverUrl = resolveMediaUrl(getGameArtwork(game, "cover"));

    const releaseYear = game.releaseDate
        ? new Date(game.releaseDate).getFullYear()
        : null;

    const genres = game.genres || [];

    return (
        <article className="game-card">
            <Link className="game-card__cover" to={`/games/${game.slug}`}>
                <MediaImage src={coverUrl} alt={`${game.title} cover`} fallbackLabel={game.title} className="game-artwork" />
            </Link>

            <div className="game-card__content">
                <div className="game-card__badges">
                    {game.accessType && (
                        <span className="game-card__type">
              {formatEnum(game.accessType)}
            </span>
                    )}

                    {genres.slice(0, 2).map((genre) => (
                        <span className="game-card__genre" key={genre}>
              {formatEnum(genre)}
            </span>
                    ))}
                </div>

                <div>
                    <p className="game-card__developer">
                        {game.developer || t("games.unknownDeveloper")}
                    </p>

                    <h2 className="game-card__title">
                        <Link to={`/games/${game.slug}`}>{game.title}</Link>
                    </h2>
                </div>

                <div className="game-card__meta">
          <span>
            <CalendarDays size={15} />
              {releaseYear || t("games.tba")}
          </span>

                    <span>{game.publisher || t("games.unknownPublisher")}</span>
                </div>

                <Link className="game-card__action" to={`/games/${game.slug}`}>
                    {t("games.viewOffers")}
                </Link>
            </div>
        </article>
    );
}
