import { useCallback, useEffect, useMemo, useState } from "react";
import {
    CalendarDays,
    Check,
    LogOut,
    Plus,
    Trophy,
    UserRound,
    Users,
    X,
} from "lucide-react";

import {
    createTournament,
    getMyParticipation,
    getParticipants,
    getTournaments,
    joinTournament,
    leaveTournament,
} from "../api/tournamentApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { useAccount } from "../account/AccountProvider.jsx";
import { useTranslation } from "../i18n/index.jsx";
import { formatDateTime, formatEnum, formatMoney } from "../utils/formatters.js";
import "../styles/tournaments.css";

const FILTERS = ["ALL", "UPCOMING", "REGISTRATION_OPEN", "IN_PROGRESS", "FINISHED"];

/** Statuses that still accept new participants — the rest render read-only. */
const JOINABLE = new Set(["UPCOMING", "REGISTRATION_OPEN"]);

const emptyDraft = {
    title: "",
    description: "",
    game: "",
    entryFee: "5",
    prizePool: "100",
    maxParticipants: "16",
    startDate: "",
    endDate: "",
};

function statusTone(status) {
    if (status === "IN_PROGRESS") return "is-live";
    if (status === "FINISHED") return "is-done";
    if (status === "CANCELLED") return "is-cancelled";
    return "is-upcoming";
}

/**
 * The join form is a dialog rather than an inline block: the nickname belongs
 * to one tournament, and an inline form on a grid card loses that connection.
 */
function JoinDialog({ busy, error, t, tournament, onCancel, onConfirm }) {
    const [nickname, setNickname] = useState("");
    const [teamName, setTeamName] = useState("");

    return (
        <div
            className="tournament-modal"
            role="presentation"
            onMouseDown={(event) => {
                if (event.target === event.currentTarget) onCancel();
            }}
        >
            <form
                className="tournament-dialog"
                onSubmit={(event) => {
                    event.preventDefault();
                    onConfirm({ inGameNickname: nickname.trim(), teamName: teamName.trim() || null });
                }}
            >
                <button
                    aria-label={t("common.close")}
                    className="tournament-dialog__close"
                    type="button"
                    onClick={onCancel}
                >
                    <X size={20} />
                </button>

                <p className="section-kicker">{t("tournaments.joinEyebrow")}</p>
                <h2>{tournament.title}</h2>

                <label>
                    {t("tournaments.nickname")}
                    <input
                        autoFocus
                        placeholder={t("tournaments.nicknamePlaceholder")}
                        required
                        value={nickname}
                        onChange={(event) => setNickname(event.target.value)}
                    />
                </label>

                <label>
                    {t("tournaments.team")}
                    <input
                        placeholder={t("tournaments.teamPlaceholder")}
                        value={teamName}
                        onChange={(event) => setTeamName(event.target.value)}
                    />
                </label>

                {error && <p className="tournament-dialog__error">{error}</p>}

                <footer>
                    <button type="button" onClick={onCancel}>{t("common.cancel")}</button>
                    <button className="is-primary" disabled={busy || !nickname.trim()} type="submit">
                        {busy ? t("account.working") : t("tournaments.confirmJoin")}
                    </button>
                </footer>
            </form>
        </div>
    );
}

/** Collapsed by default: most visitors want the bracket list, not the roster. */
function ParticipantList({ t, tournamentId }) {
    const [open, setOpen] = useState(false);
    const [participants, setParticipants] = useState(null);
    const [error, setError] = useState("");

    async function toggle() {
        const next = !open;
        setOpen(next);

        if (!next || participants) return;

        try {
            setParticipants(await getParticipants(tournamentId));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        }
    }

    return (
        <div className="tournament-roster">
            <button type="button" onClick={toggle}>
                <Users size={15} />
                {open ? t("tournaments.hideRoster") : t("tournaments.showRoster")}
            </button>

            {open && error && <p className="tournament-roster__error">{error}</p>}

            {open && !error && (
                participants === null
                    ? <p className="tournament-roster__empty">{t("common.loading")}</p>
                    : participants.length === 0
                    ? <p className="tournament-roster__empty">{t("tournaments.noParticipants")}</p>
                    : (
                        <ul>
                            {participants.map((participant) => (
                                <li key={participant.id}>
                                    <UserRound size={14} />
                                    <strong>{participant.inGameNickname || participant.username}</strong>
                                    {participant.teamName && <small>{participant.teamName}</small>}
                                    <span className="tournament-tag">{formatEnum(participant.status)}</span>
                                </li>
                            ))}
                        </ul>
                    )
            )}
        </div>
    );
}

function CreateForm({ t, onCancel, onCreated }) {
    const [draft, setDraft] = useState(emptyDraft);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    function update(field) {
        return (event) => setDraft((current) => ({ ...current, [field]: event.target.value }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setBusy(true);
        setError("");

        try {
            const created = await createTournament({
                title: draft.title.trim(),
                description: draft.description.trim() || null,
                game: draft.game.trim() || null,
                entryFee: Number(draft.entryFee),
                prizePool: draft.prizePool ? Number(draft.prizePool) : null,
                maxParticipants: draft.maxParticipants ? Number(draft.maxParticipants) : null,
                // <input type="datetime-local"> yields "2026-01-01T18:00", which is
                // exactly the LocalDateTime shape the API expects.
                startDate: draft.startDate,
                endDate: draft.endDate,
            });

            setDraft(emptyDraft);
            onCreated(created);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        } finally {
            setBusy(false);
        }
    }

    return (
        <form className="tournament-create" onSubmit={handleSubmit}>
            <header>
                <h2>{t("tournaments.createTitle")}</h2>
                <button aria-label={t("common.close")} type="button" onClick={onCancel}>
                    <X size={18} />
                </button>
            </header>

            <div className="tournament-create__grid">
                <label className="is-wide">
                    {t("tournaments.name")}
                    <input required value={draft.title} onChange={update("title")} />
                </label>
                <label>
                    {t("tournaments.game")}
                    <input value={draft.game} onChange={update("game")} />
                </label>
                <label>
                    {t("tournaments.entryFee")}
                    <input min="0.01" required step="0.01" type="number" value={draft.entryFee} onChange={update("entryFee")} />
                </label>
                <label>
                    {t("tournaments.prize")}
                    <input min="0" step="0.01" type="number" value={draft.prizePool} onChange={update("prizePool")} />
                </label>
                <label>
                    {t("tournaments.slots")}
                    <input min="2" type="number" value={draft.maxParticipants} onChange={update("maxParticipants")} />
                </label>
                <label>
                    {t("tournaments.start")}
                    <input required type="datetime-local" value={draft.startDate} onChange={update("startDate")} />
                </label>
                <label>
                    {t("tournaments.end")}
                    <input required type="datetime-local" value={draft.endDate} onChange={update("endDate")} />
                </label>
                <label className="is-wide">
                    {t("tournaments.descriptionLabel")}
                    <textarea rows={3} value={draft.description} onChange={update("description")} />
                </label>
            </div>

            {error && <p className="tournament-create__error">{error}</p>}

            <footer>
                <button disabled={busy} type="submit">
                    {busy ? t("account.working") : t("tournaments.create")}
                </button>
            </footer>
        </form>
    );
}

export default function TournamentsPage() {
    const { t } = useTranslation();
    const { isAuthenticated } = useAccount();
    const [tournaments, setTournaments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [filter, setFilter] = useState("ALL");
    const [joining, setJoining] = useState(null);
    const [busy, setBusy] = useState(false);
    const [dialogError, setDialogError] = useState("");
    const [notice, setNotice] = useState("");
    const [creating, setCreating] = useState(false);
    // Which tournaments this account is already in. Loaded per card so the
    // buttons say "leave" rather than offering a join that would be refused.
    const [joined, setJoined] = useState({});

    const load = useCallback(async (signal) => {
        setLoading(true);
        setError("");

        try {
            const page = await getTournaments(0, 48, signal);
            if (signal?.aborted) return;
            setTournaments(page?.content || []);
        } catch (requestError) {
            if (!signal?.aborted) setError(getApiErrorMessage(requestError));
        } finally {
            if (!signal?.aborted) setLoading(false);
        }
    }, []);

    useEffect(() => {
        const controller = new AbortController();
        // Deferred so the first setState lands after the effect returns rather
        // than cascading a second render out of this one.
        const pending = Promise.resolve().then(() => load(controller.signal));

        return () => {
            controller.abort();
            pending.catch(() => {});
        };
    }, [load]);

    // Membership is only knowable while signed in, and only worth asking for
    // the tournaments actually on screen.
    useEffect(() => {
        const controller = new AbortController();

        const pending = Promise.resolve().then(async () => {
            if (!isAuthenticated || tournaments.length === 0) {
                setJoined({});
                return;
            }

            const results = await Promise.allSettled(
                tournaments.map((tournament) => getMyParticipation(tournament.id, controller.signal)),
            );

            if (controller.signal.aborted) return;

            const next = {};
            results.forEach((result, index) => {
                if (result.status === "fulfilled" && result.value) {
                    next[tournaments[index].id] = result.value;
                }
            });
            setJoined(next);
        });

        return () => {
            controller.abort();
            pending.catch(() => {});
        };
    }, [isAuthenticated, tournaments]);

    const visible = useMemo(
        () => (filter === "ALL" ? tournaments : tournaments.filter((item) => item.status === filter)),
        [filter, tournaments],
    );

    async function confirmJoin(payload) {
        setBusy(true);
        setDialogError("");

        try {
            const participation = await joinTournament(joining.id, payload);
            setJoined((current) => ({ ...current, [joining.id]: participation }));
            setTournaments((current) => current.map((item) => (
                item.id === joining.id
                    ? { ...item, currentParticipants: (item.currentParticipants || 0) + 1 }
                    : item
            )));
            setNotice(t("tournaments.joined", { title: joining.title }));
            setJoining(null);
        } catch (requestError) {
            setDialogError(getApiErrorMessage(requestError));
        } finally {
            setBusy(false);
        }
    }

    async function handleLeave(tournament) {
        setNotice("");
        setError("");

        try {
            await leaveTournament(tournament.id);
            setJoined((current) => {
                const next = { ...current };
                delete next[tournament.id];
                return next;
            });
            setTournaments((current) => current.map((item) => (
                item.id === tournament.id
                    ? { ...item, currentParticipants: Math.max(0, (item.currentParticipants || 1) - 1) }
                    : item
            )));
            setNotice(t("tournaments.left", { title: tournament.title }));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        }
    }

    return (
        <div className="container tournaments-page">
            <header className="tournaments-page__header">
                <div>
                    <p className="section-kicker">{t("tournaments.eyebrow")}</p>
                    <h1>{t("nav.tournaments")}</h1>
                    <p className="tournaments-page__lead">{t("tournaments.description")}</p>
                </div>

                {isAuthenticated && (
                    <button
                        className="tournaments-page__new"
                        type="button"
                        onClick={() => setCreating((current) => !current)}
                    >
                        <Plus size={16} />
                        {t("tournaments.create")}
                    </button>
                )}
            </header>

            {creating && (
                <CreateForm
                    t={t}
                    onCancel={() => setCreating(false)}
                    onCreated={(created) => {
                        setCreating(false);
                        setNotice(t("tournaments.created", { title: created.title }));
                        setTournaments((current) => [created, ...current]);
                    }}
                />
            )}

            <nav className="tournament-filters" aria-label={t("tournaments.filterLabel")}>
                {FILTERS.map((value) => (
                    <button
                        className={filter === value ? "is-active" : ""}
                        key={value}
                        type="button"
                        onClick={() => setFilter(value)}
                    >
                        {value === "ALL" ? t("tournaments.all") : formatEnum(value)}
                    </button>
                ))}
            </nav>

            {notice && <div className="status-panel status-panel--inline status-panel--success">{notice}</div>}
            {error && <div className="status-panel status-panel--inline status-panel--error">{error}</div>}

            {loading ? (
                <div className="tournament-empty">{t("common.loading")}</div>
            ) : visible.length === 0 ? (
                <div className="tournament-empty">
                    <Trophy size={28} />
                    <h2>{t("tournaments.emptyTitle")}</h2>
                    <p>{t("tournaments.emptyText")}</p>
                </div>
            ) : (
                <ul className="tournament-grid">
                    {visible.map((tournament) => {
                        const participation = joined[tournament.id];
                        const full = tournament.maxParticipants > 0
                            && tournament.currentParticipants >= tournament.maxParticipants;
                        const open = JOINABLE.has(tournament.status);

                        return (
                            <li className="tournament-card" key={tournament.id}>
                                <header>
                                    <span className={`tournament-status ${statusTone(tournament.status)}`}>
                                        {formatEnum(tournament.status)}
                                    </span>
                                    {tournament.game && <small>{tournament.game}</small>}
                                </header>

                                <h2>{tournament.title}</h2>
                                {tournament.description && <p>{tournament.description}</p>}

                                <dl className="tournament-facts">
                                    <div>
                                        <dt><CalendarDays size={14} /> {t("tournaments.start")}</dt>
                                        <dd>{formatDateTime(tournament.startDate)}</dd>
                                    </div>
                                    <div>
                                        <dt><Trophy size={14} /> {t("tournaments.prize")}</dt>
                                        <dd>{formatMoney(tournament.prizePool, null)}</dd>
                                    </div>
                                    <div>
                                        <dt><Users size={14} /> {t("tournaments.slots")}</dt>
                                        <dd>
                                            {tournament.currentParticipants || 0}
                                            {tournament.maxParticipants > 0 && ` / ${tournament.maxParticipants}`}
                                        </dd>
                                    </div>
                                    <div>
                                        <dt>{t("tournaments.entryFee")}</dt>
                                        <dd>{formatMoney(tournament.entryFee, null)}</dd>
                                    </div>
                                </dl>

                                {/* Keyed on the head count: a join or leave remounts the roster,
                                    so an open list refetches instead of showing who left. */}
                                <ParticipantList
                                    key={`${tournament.id}-${tournament.currentParticipants}`}
                                    t={t}
                                    tournamentId={tournament.id}
                                />

                                <footer>
                                    {!isAuthenticated ? (
                                        <span className="tournament-card__hint">{t("tournaments.signInFirst")}</span>
                                    ) : participation ? (
                                        <>
                                            <span className="tournament-card__joined">
                                                <Check size={15} />
                                                {participation.inGameNickname || t("tournaments.registered")}
                                            </span>
                                            <button type="button" onClick={() => handleLeave(tournament)}>
                                                <LogOut size={15} />
                                                {t("tournaments.leave")}
                                            </button>
                                        </>
                                    ) : !open ? (
                                        <span className="tournament-card__hint">{t("tournaments.closed")}</span>
                                    ) : full ? (
                                        <span className="tournament-card__hint">{t("tournaments.full")}</span>
                                    ) : (
                                        <button
                                            className="is-primary"
                                            type="button"
                                            onClick={() => {
                                                setDialogError("");
                                                setNotice("");
                                                setJoining(tournament);
                                            }}
                                        >
                                            {t("tournaments.join")}
                                        </button>
                                    )}
                                </footer>
                            </li>
                        );
                    })}
                </ul>
            )}

            {joining && (
                <JoinDialog
                    busy={busy}
                    error={dialogError}
                    t={t}
                    tournament={joining}
                    onCancel={() => setJoining(null)}
                    onConfirm={confirmJoin}
                />
            )}
        </div>
    );
}
