import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import Sidebar from "../../components/Sidebar";
import { getReturns } from "../../services/module4Api";

function ReturnDashboard() {

    const [returns, setReturns] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => {

        loadReturns();

    }, []);


    async function loadReturns() {

        try {

            const data = await getReturns();

            setReturns(data);

        } catch (error) {

            setError(error.message);
        }
    }


    return (

        <div className="layout">

            <Sidebar />

            <main className="content">

                <h1>Return Management</h1>

                <Link
                    className="button"
                    to="/returns/process">

                    Process New Return

                </Link>


                {error && (
                    <p className="error">{error}</p>
                )}


                <table>

                    <thead>

                    <tr>
                        <th>Return ID</th>
                        <th>Rental ID</th>
                        <th>Return Date</th>
                        <th>Type</th>
                        <th>Notes</th>
                    </tr>

                    </thead>


                    <tbody>

                    {returns.map(item => (

                        <tr key={item.returnId}>

                            <td>
                                {item.returnId}
                            </td>

                            <td>
                                {item.rental?.rentalId}
                            </td>

                            <td>
                                {item.returnDate}
                            </td>

                            <td>
                                {item.returnType}
                            </td>

                            <td>
                                {item.notes}
                            </td>

                        </tr>

                    ))}

                    </tbody>

                </table>

            </main>

        </div>
    );
}

export default ReturnDashboard;