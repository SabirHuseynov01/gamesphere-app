export function getGameArtwork(game, type = "cover") {
    if (!game) return null;

    const artwork = game.artwork || game.images || {};
    const candidates = {
        cover: [artwork.cover, game.coverImageUrl, game.coverUrl, game.thumbnailUrl],
        background: [artwork.background, game.backgroundImageUrl, game.backgroundUrl],
        hero: [artwork.hero, game.heroImageUrl, game.heroArtworkUrl, artwork.background, game.backgroundImageUrl],
        logo: [artwork.logo, game.logoImageUrl, game.logoUrl],
        thumbnail: [artwork.thumbnail, game.thumbnailUrl, game.coverImageUrl],
        screenshot: [artwork.screenshot, game.screenshotUrl],
    };

    return candidates[type]?.find(Boolean) || candidates.cover.find(Boolean) || null;
}
