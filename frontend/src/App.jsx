import { useEffect, useState } from "react";
import Preloader from "./components/Preloader";
import AppRoutes from "./routes/AppRoutes";

function getStoredUser() {
    try {
        const savedUser = sessionStorage.getItem("rentflowUser");
        return savedUser ? JSON.parse(savedUser) : null;
    } catch {
        return null;
    }
}

function App() {
    const [user, setUser] = useState(getStoredUser);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    const [showPreloader, setShowPreloader] = useState(true);
    const [preloaderExiting, setPreloaderExiting] = useState(false);

    useEffect(() => {
        const exitTimer = setTimeout(() => {
            setPreloaderExiting(true);
        }, 1800);

        const removeTimer = setTimeout(() => {
            setShowPreloader(false);
        }, 2300);

        return () => {
            clearTimeout(exitTimer);
            clearTimeout(removeTimer);
        };
    }, []);

    const handleLogin = async (event) => {
        event.preventDefault();
        setMessage("");
        setLoading(true);

        try {
            const response = await fetch(
                "/api/auth/login",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    credentials: "include",
                    body: JSON.stringify({
                        email,
                        password
                    })
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Login failed");
            }

            sessionStorage.setItem(
                "rentflowUser",
                JSON.stringify(data)
            );

            setUser(data);
            setPassword("");

        } catch (error) {
            setMessage(error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = async () => {
        try {
            await fetch(
                "/api/auth/logout",
                {
                    method: "POST",
                    credentials: "include"
                }
            );
        } catch (error) {
            console.error("Logout request failed:", error);
        }

        sessionStorage.removeItem("rentflowUser");
        setUser(null);
        setEmail("");
        setPassword("");
        setMessage("");
    };

    if (showPreloader) {
        return (
            <Preloader isExiting={preloaderExiting} />
        );
    }

    // Logged-in user: show the integrated application
    if (user) {
        return (
            <AppRoutes
                user={user}
                onLogout={handleLogout}
            />
        );
    }

    // Not logged in: show the existing login functionality
    return (
        <div style={styles.container}>
            <form onSubmit={handleLogin} style={styles.form}>
                <h1>RentFlow Login</h1>

                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(event) =>
                        setEmail(event.target.value)
                    }
                    style={styles.input}
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(event) =>
                        setPassword(event.target.value)
                    }
                    style={styles.input}
                    required
                />

                <button
                    type="submit"
                    style={styles.button}
                    disabled={loading}
                >
                    {loading ? "Logging in..." : "Login"}
                </button>

                {message && (
                    <p style={styles.message}>{message}</p>
                )}
            </form>
        </div>
    );
}

const styles = {
    container: {
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "linear-gradient(135deg, #D6EAFE 0%, #E6F2FF 45%, #D3E8FC 100%)"
    },
    form: {
        width: "400px",
        padding: "40px 36px",
        background: "rgba(255, 255, 255, 0.92)",
        borderRadius: "24px",
        boxShadow: "0 1px 2px rgba(18, 52, 110, 0.04), 0 24px 60px rgba(36, 92, 170, 0.18)",
        border: "1px solid rgba(255, 255, 255, 0.9)",
        color: "#0B1B3F"
    },
    input: {
        width: "100%",
        padding: "13px 18px",
        marginBottom: "15px",
        boxSizing: "border-box",
        border: "1px solid #DCE8F6",
        borderRadius: "999px",
        color: "#0B1B3F",
        background: "#FFFFFF",
        outline: "none",
        boxShadow: "0 1px 2px rgba(18, 52, 110, 0.05), 0 4px 12px rgba(36, 92, 170, 0.07)"
    },
    button: {
        width: "100%",
        padding: "13px",
        border: "none",
        borderRadius: "999px",
        cursor: "pointer",
        background: "#0B63F6",
        fontWeight: 700,
        color: "white",
        boxShadow: "0 8px 18px rgba(11, 99, 246, 0.28)"
    },
    message: {
        marginTop: "15px",
        color: "#E5384D"
    }
};

export default App;