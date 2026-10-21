const API_ORIGIN = window.location.origin;

const originalFetch = window.fetch.bind(window);

let csrfToken = null;
let csrfPromise = null;

async function getCsrfToken() {
    if (csrfToken) return csrfToken;

    if (!csrfPromise) {
        csrfPromise = originalFetch(`${API_ORIGIN}/api/auth/csrf`, {
            credentials: "include"
        })
            .then(async (response) => {
                if (!response.ok) {
                    throw new Error("Unable to obtain CSRF token");
                }

                const data = await response.json();
                csrfToken = data.token;
                return csrfToken;
            })
            .finally(() => {
                csrfPromise = null;
            });
    }

    return csrfPromise;
}

export function initializeCsrfFetch() {
    window.fetch = async (input, init = {}) => {
        const url = new URL(
            typeof input === "string" ? input : input.url,
            window.location.href
        );

        const isRentFlowApi =
            url.pathname.startsWith("/api/") &&
            (
                url.origin === API_ORIGIN ||
                url.origin === window.location.origin
            );

        if (!isRentFlowApi) {
            return originalFetch(input, init);
        }

        const method = (
            init.method ||
            (input instanceof Request ? input.method : "GET")
        ).toUpperCase();

        const options = {
            ...init,
            credentials: "include"
        };

        if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
            const token = await getCsrfToken();

            const headers = new Headers(
                init.headers || (input instanceof Request ? input.headers : {})
            );

            headers.set("X-XSRF-TOKEN", token);
            options.headers = headers;
        }

        const response = await originalFetch(input, options);

        if (url.pathname === "/api/auth/login" && response.ok) {
            csrfToken = null;
        }

        if (url.pathname === "/api/auth/logout" && response.ok) {
            csrfToken = null;
        }

        return response;
    };
}
