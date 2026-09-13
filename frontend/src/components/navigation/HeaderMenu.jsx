import { useEffect, useId, useRef, useState } from "react";

/**
 * Icon button in the header that opens a panel underneath it, the way the
 * account, wishlist and cart controls behave on a storefront header.
 */
export default function HeaderMenu({ icon, label, badge = 0, children, panelClassName = "" }) {
    const [open, setOpen] = useState(false);
    const containerRef = useRef(null);
    const panelId = useId();

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

    return (
        <div className="header-menu" ref={containerRef}>
            <button
                aria-controls={panelId}
                aria-expanded={open}
                aria-haspopup="true"
                aria-label={label}
                className={`icon-button${open ? " is-open" : ""}`}
                title={label}
                type="button"
                onClick={() => setOpen((current) => !current)}
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
