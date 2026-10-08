import { useEffect } from "react";

// =========================================================
// MODULE 4 - small shared building blocks for the Module 4 pages
// =========================================================

export function money(value) {
    const n = Number(value || 0);
    return "Rs. " + n.toLocaleString("en-LK", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

/** "2026-10-07 15:30:00" / "2026-10-07T15:30" -> "07 Oct 2026, 15:30" */
export function dateTime(value) {
    if (!value) return "-";
    const d = new Date(String(value).replace(" ", "T"));
    if (Number.isNaN(d.getTime())) return value;
    return d.toLocaleString("en-GB", {
        day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit"
    });
}

export function date(value) {
    if (!value) return "-";
    const d = new Date(String(value).slice(0, 10) + "T00:00");
    if (Number.isNaN(d.getTime())) return value;
    return d.toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
}

/** Local "now" for <input type="datetime-local"> */
export function nowLocal() {
    const d = new Date();
    d.setSeconds(0, 0);
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

const TONES = {
    ACTIVE: "blue", ISSUED: "blue", OVERDUE: "red", PARTIALLY_RETURNED: "amber",
    RETURNED: "green", CLOSED: "gray", CANCELLED: "gray",
    GOOD: "green", DAMAGED: "red", "MISSING PARTS": "amber", "NEEDS MAINTENANCE": "amber",
    ASSESSED: "amber", CHARGED: "red", WAIVED: "gray", REPAIRED: "green",
    PENDING: "amber", RECOVERED: "green", SETTLED: "green",
    HELD: "blue", PARTIALLY_REFUNDED: "green", REFUNDED: "green", FORFEITED: "red",
    LOST: "red", STOLEN: "red", NON_RETURNED: "amber",
    MINOR: "amber", MODERATE: "red", SEVERE: "red",
    FULL: "green", PARTIAL: "amber",
    RENTAL: "blue", EXTENSION: "blue", LATE: "amber", DAMAGE: "red", LOST_ITEM: "red", OTHER: "gray"
};

export function label(value) {
    if (!value) return "-";
    const s = String(value).replaceAll("_", " ").toLowerCase();
    return s.charAt(0).toUpperCase() + s.slice(1);
}

export function Badge({ value, children }) {
    const tone = TONES[value] || "gray";
    return <span className={`m4-badge m4-badge-${tone}`}>{children || label(value)}</span>;
}

export function Alert({ type = "error", children, onClose }) {
    if (!children) return null;
    return (
        <div className={`m4-alert m4-alert-${type}`}>
            <span>{children}</span>
            {onClose && <button className="m4-alert-close" onClick={onClose} aria-label="Close">×</button>}
        </div>
    );
}

export function Card({ title, actions, children, className = "", flush = false }) {
    return (
        <section className={`m4-card ${className}`}>
            {(title || actions) && (
                <div className="m4-card-head">
                    {title && <h2>{title}</h2>}
                    {actions && <div className="m4-card-actions">{actions}</div>}
                </div>
            )}
            <div className={flush ? "m4-card-flush" : "m4-card-body"}>{children}</div>
        </section>
    );
}

export function Stat({ label: text, value, hint, tone, onClick }) {
    return (
        <button type="button" className={`m4-stat ${tone ? "m4-stat-" + tone : ""}`} onClick={onClick} disabled={!onClick}>
            <span className="m4-stat-label">{text}</span>
            <strong className="m4-stat-value">{value}</strong>
            {hint && <span className="m4-stat-hint">{hint}</span>}
        </button>
    );
}

export function Empty({ children }) {
    return <div className="m4-empty">{children}</div>;
}

export function Loading() {
    return <div className="m4-empty">Loading...</div>;
}

export function Modal({ title, onClose, children, footer, wide = false }) {
    useEffect(() => {
        const onKey = (e) => e.key === "Escape" && onClose();
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [onClose]);

    return (
        <div className="m4-modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
            <div className={`m4-modal ${wide ? "m4-modal-wide" : ""}`} role="dialog" aria-modal="true">
                <div className="m4-modal-head">
                    <h3>{title}</h3>
                    <button className="m4-icon-btn" onClick={onClose} aria-label="Close">×</button>
                </div>
                <div className="m4-modal-body">{children}</div>
                {footer && <div className="m4-modal-foot">{footer}</div>}
            </div>
        </div>
    );
}

/** Wizard progress bar: steps = ["Find rental", ...], current = index */
export function Stepper({ steps, current, onStep }) {
    return (
        <ol className="m4-stepper">
            {steps.map((step, i) => {
                const state = i < current ? "done" : i === current ? "current" : "todo";
                const clickable = onStep && i < current;
                return (
                    <li key={step} className={`m4-step m4-step-${state}`}>
                        <button type="button" disabled={!clickable} onClick={() => clickable && onStep(i)}>
                            <span className="m4-step-dot">{state === "done" ? "✓" : i + 1}</span>
                            <span className="m4-step-text">{step}</span>
                        </button>
                    </li>
                );
            })}
        </ol>
    );
}

/** Rental lifecycle pills: Issued > Returned > Settled > Closed */
export function RentalFlow({ status, settlementStatus }) {
    const returned = ["RETURNED", "CLOSED"].includes(status);
    const steps = [
        { name: "Issued", done: true },
        { name: status === "PARTIALLY_RETURNED" ? "Partly returned" : "Returned", done: returned, current: !returned },
        { name: "Settled", done: settlementStatus === "SETTLED", current: returned && settlementStatus !== "SETTLED" },
        { name: "Closed", done: status === "CLOSED" }
    ];
    return (
        <div className="m4-flow">
            {steps.map((s) => (
                <span key={s.name} className={`m4-flow-pill ${s.done ? "done" : s.current ? "current" : ""}`}>
                    {s.done ? "✓ " : ""}{s.name}
                </span>
            ))}
        </div>
    );
}

export function Field({ label: text, hint, children, className = "" }) {
    return (
        <label className={`m4-field ${className}`}>
            <span className="m4-field-label">{text}</span>
            {children}
            {hint && <span className="m4-field-hint">{hint}</span>}
        </label>
    );
}
