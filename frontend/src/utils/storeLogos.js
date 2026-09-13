import steamLogo from "../assets/stores/steam.svg";
import epicLogo from "../assets/stores/epic-games.svg";
import ubisoftLogo from "../assets/stores/ubisoft.svg";
import playstationLogo from "../assets/stores/playstation.svg";
import xboxLogo from "../assets/stores/xbox.svg";
import battleNetLogo from "../assets/stores/battle.net.svg";
import eaGamesLogo from "../assets/stores/eagames.svg";
import gogLogo from "../assets/stores/gog.svg";
import rockstarGamesLogo from "../assets/stores/rockstargames.svg";
import nintendoLogo from "../assets/stores/nintendo.svg";

const STORE_LOGOS = [
    { match: ["steam"], src: steamLogo },
    { match: ["epic"], src: epicLogo },
    { match: ["ubisoft"], src: ubisoftLogo },
    { match: ["playstation", "ps store"], src: playstationLogo },
    { match: ["xbox"], src: xboxLogo },
    { match: ["battle net", "blizzard"], src: battleNetLogo },
    { match: ["ea games", "ea app", "ea store", "electronic arts", "eagames"], src: eaGamesLogo },
    { match: ["gog com", "gog"], src: gogLogo },
    { match: ["rockstar games", "rockstar store", "rockstar"], src: rockstarGamesLogo },
    { match: ["nintendo eshop", "nintendo"], src: nintendoLogo },
];

export function getStoreLogo(storeName) {
    const normalizedName = String(storeName || "")
        .trim()
        .toLowerCase()
        .replace(/[._-]+/g, " ")
        .replace(/\s+/g, " ");

    return STORE_LOGOS.find(({ match }) => match.some((value) => normalizedName.includes(value)))?.src || null;
}
