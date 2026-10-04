import {
    useEffect,
    useState
} from "react";

import {
    useNavigate
} from "react-router-dom";

import {
    getAllReturns
} from "../../services/module4/module4Api.js";

import "../../styles/module4/module4.css";


function ReturnDashboard() {

    const navigate =
        useNavigate();

    const [returns, setReturns] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    useEffect(() => {

        async function loadReturns() {

            try {

                const data =
                    await getAllReturns();

                setReturns(data);

            } catch (err) {

                console.error(err);

                setError(
                    err.message ||
                    "Unable to load return records."
                );

            } finally {

                setLoading(false);
            }
        }

        void loadReturns();

    }, []);


    return (

        <div className="module-page">

            <div className="page-header">

                <div>

                    <h1>
                        Return Management
                    </h1>

                    <p>
                        Process and review
                        rental equipment returns.
                    </p>

                </div>

                <button
                    className="primary-btn"
                    onClick={() =>
                        navigate(
                            "/returns/new"
                        )
                    }
                >
                    + Process New Return
                </button>

            </div>


            <div className="card">

                <h2>
                    Return History
                </h2>

                {
                    loading && (
                        <p>
                            Loading returns...
                        </p>
                    )
                }

                {
                    error && (

                        <div className="error-message">
                            {error}
                        </div>
                    )
                }

                {
                    !loading &&
                    !error &&
                    returns.length === 0 && (

                        <p>
                            No returns have been
                            recorded yet.
                        </p>
                    )
                }

                {
                    returns.length > 0 && (

                        <div className="table-wrapper">

                            <table className="return-table">

                                <thead>

                                <tr>
                                    <th>Return ID</th>
                                    <th>Rental ID</th>
                                    <th>Return Date</th>
                                    <th>Processed By</th>
                                    <th>Type</th>
                                    <th>Notes</th>
                                    <th>Action</th>
                                </tr>

                                </thead>

                                <tbody>

                                {
                                    returns.map(
                                        (
                                            rentalReturn
                                        ) => (

                                            <tr
                                                key={
                                                    rentalReturn.returnId
                                                }
                                            >

                                                <td>
                                                    {
                                                        rentalReturn.returnId
                                                    }
                                                </td>

                                                <td>
                                                    {
                                                        rentalReturn.rentalId
                                                    }
                                                </td>

                                                <td>
                                                    {
                                                        rentalReturn.returnDate
                                                    }
                                                </td>

                                                <td>
                                                    {
                                                        rentalReturn.processedBy
                                                    }
                                                </td>

                                                <td>

                                                        <span
                                                            className={
                                                                rentalReturn.returnType ===
                                                                "FULL"
                                                                    ? "type-badge full"
                                                                    : "type-badge partial"
                                                            }
                                                        >
                                                            {
                                                                rentalReturn.returnType
                                                            }
                                                        </span>

                                                </td>

                                                <td>
                                                    {
                                                        rentalReturn.notes ||
                                                        "-"
                                                    }
                                                </td>

                                                <td>

                                                    <button
                                                        className="small-btn"
                                                        onClick={() =>
                                                            navigate(
                                                                `/returns/${rentalReturn.returnId}`
                                                            )
                                                        }
                                                    >
                                                        View
                                                    </button>

                                                </td>

                                            </tr>
                                        )
                                    )
                                }

                                </tbody>

                            </table>

                        </div>
                    )
                }

            </div>

        </div>
    );
}

export default ReturnDashboard;
