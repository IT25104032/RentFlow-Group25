import { useEffect, useState } from "react";
import { getAllReturns } from "../../services/module4Api";

function ReturnDashboard() {

    const [returns, setReturns] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        async function loadReturns() {

            try {
                const data = await getAllReturns();
                setReturns(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        }

        loadReturns();

    }, []);

    if (loading) {
        return <p>Loading returns...</p>;
    }

    return (
        <div>
            <h1>Return Management</h1>

            {error && (
                <p>{error}</p>
            )}

            {returns.length === 0 ? (
                <p>No returns have been recorded yet.</p>
            ) : (
                <table border="1" cellPadding="10">
                    <thead>
                    <tr>
                        <th>Return ID</th>
                        <th>Rental ID</th>
                        <th>Return Date</th>
                        <th>Processed By</th>
                        <th>Type</th>
                        <th>Notes</th>
                    </tr>
                    </thead>

                    <tbody>
                    {returns.map((rentalReturn) => (
                        <tr key={rentalReturn.returnId}>
                            <td>{rentalReturn.returnId}</td>
                            <td>{rentalReturn.rentalId}</td>
                            <td>{rentalReturn.returnDate}</td>
                            <td>{rentalReturn.processedBy}</td>
                            <td>{rentalReturn.returnType}</td>
                            <td>{rentalReturn.notes}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

export default ReturnDashboard;