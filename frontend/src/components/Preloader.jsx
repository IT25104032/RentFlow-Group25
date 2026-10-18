import "./Preloader.css";

function Preloader({ isExiting = false }) {
    return (
        <div
            className={`rentflow-preloader ${
                isExiting ? "preloader-exit" : ""
            }`}
            role="status"
            aria-label="Loading RentFlow"
        >
            <div className="preloader-background-circle circle-one" />
            <div className="preloader-background-circle circle-two" />

            <div className="preloader-content">

                <img
                    src="/rentflow-logo.png"
                    alt="RentFlow Logo"
                    className="preloader-logo"
                />

                <h1 className="preloader-title">
                    Rent<span>Flow</span>
                </h1>

                <p className="preloader-tagline">
                    RENT. WORK. GROW.
                </p>

                <div className="preloader-dots" aria-hidden="true">
                    <span />
                    <span />
                    <span />
                </div>

            </div>
        </div>
    );
}

export default Preloader;
