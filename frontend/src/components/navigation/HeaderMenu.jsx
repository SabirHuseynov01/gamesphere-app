import { useEffect, useId, useRef, useState } from "react";

/** Leaving the button for the panel crosses a gap; do not close mid-travel. */
const CLOSE_DELAY_MS = 180;

function pointerHovers() {
    return typeof window.matchMedia === "function"
        && window.matchMedia("(hover: hover) and (pointer: fine)").matches;
}

/**
 * Icon button in the header that opens a panel underneath it, the way the
 * account, wishlist and cart controls behave on a storefront header.
 *
 * A mouse opens it on hover; click still works, and is the only way in on
 * touch, where hover either does not exist or sticks after a tap.
 */
export default function HeaderMenu({ icon, label, badge = 0, children, panelClassName = "" }) {
    const [open, setOpen] = useState(false);
    const containerRef = useRef(null);
    const closeTimer = useRef(null);
    const panelId = useId();

    useEffect(() => () => clearTimeout(closeTimer.current), []);

    useEffect(() => {
        if (!open) return undefined;

        function handlePointerDown(event) {
            if (!containerRef.current?.contains(event.target)) setOpen(false);
        }

        function handleKeyDown(event) {
            if (event.key === "Escape") setOpen(false);
        }

        document.addEventListener("pointerdown", handlePointerDown);
        document.addEventListener("keydown", handleKeyDown);

        return () => {
            document.removeEventListener("pointerdown", handlePointerDown);
            document.removeEventListener("keydown", handleKeyDown);
        };
    }, [open]);

    function cancelClose() {
        clearTimeout(closeTimer.current);
    }

    function handleEnter() {
        if (!pointerHovers()) return;
        cancelClose();
        setOpen(true);
    }

    function handleLeave() {
        if (!pointerHovers()) return;
        cancelClose();
        closeTimer.current = setTimeout(() => setOpen(false), CLOSE_DELAY_MS);
    }

    return (
        <div
            className="header-menu"
            ref={containerRef}
            onMouseEnter={handleEnter}
            onMouseLeave={handleLeave}
        >
            <button
                aria-controls={panelId}
                aria-expanded={open}
                aria-haspopup="true"
                aria-label={label}
                className={`icon-button${open ? " is-open" : ""}`}
                title={label}
                type="button"
                onClick={() => { cancelClose(); setOpen((current) => !current); }}
                onFocus={cancelClose}
            >
                {icon}
                {badge > 0 && <span className="header-menu__badge">{badge > 99 ? "99+" : badge}</span>}
            </button>

            <div className={`header-menu__panel ${panelClassName}`} hidden={!open} id={panelId}>
                {open && children({ close: () => setOpen(false) })}
            </div>
        </div>
    );
}
