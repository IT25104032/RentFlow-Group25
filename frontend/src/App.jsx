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
                "http://localhost:8081/api/auth/login",
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
                "http://localhost:8081/api/auth/logout",
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
        background: "#f4f6f8"
    },
    form: {
        width: "350px",
        padding: "30px",
        background: "white",
        borderRadius: "10px",
        boxShadow: "0 4px 15px rgba(0,0,0,0.1)"
    },
    input: {
        width: "100%",
        padding: "12px",
        marginBottom: "15px",
        boxSizing: "border-box",
        border: "1px solid #ccc",
        borderRadius: "5px"
    },
    button: {
        width: "100%",
        padding: "12px",
        border: "none",
        borderRadius: "5px",
        cursor: "pointer",
        background: "#222",
        color: "white"
    },
    message: {
        marginTop: "15px",
        color: "#b91c1c"
    }
};

export default App;