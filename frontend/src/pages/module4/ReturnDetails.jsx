import {
    useEffect,
    useState
} from "react";

import {
    useNavigate,
    useParams
} from "react-router-dom";

import {
    getReturnById,
    getReturnItems
} from "../../services/module4/module4Api.js";

import "../../styles/module4/module4.css";


function ReturnDetails() {

    const { returnId } =
        useParams();

    const navigate =
        useNavigate();

    const [rentalReturn, setRentalReturn] =
        useState(null);

    const [items, setItems] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    useEffect(() => {

        async function loadReturn() {

            try {

                const [
                    returnData,
                    itemData
                ] =
                    await Promise.all([
                        getReturnById(
                            returnId
                        ),
                        getReturnItems(
                            returnId
                        )
                    ]);

                setRentalReturn(
                    returnData
                );

                setItems(
                    itemData
                );

            } catch (err) {

                console.error(err);

                setError(
                    err.message ||
                    "Unable to load return details."
                );

            } finally {

                setLoading(false);
            }
        }

        void loadReturn();

    }, [returnId]);


    if (loading) {

        return (
            <div className="module-page">
                Loading...
            </div>
        );
    }


    if (error || !rentalReturn) {

        return (
            <div className="module-page">

                <div className="error-message">
                    {
                        error ||
                        "Return not found."
                    }
                </div>

            </div>
        );
    }


    return (

        <div className="module-page">

            <div className="page-header">

                <div>

                    <h1>
                        Return #
                        {
                            rentalReturn.returnId
                        }
                    </h1>

                    <p>
                        Rental return details
                        and inspection results.
                    </p>

                </div>

                <button
                    className="secondary-btn"
                    onClick={() =>
                        navigate("/returns")
                    }
                >
                    Back
                </button>

            </div>


            <div className="card">

                <h2>
                    Return Information
                </h2>

                <div className="details-grid">

                    <div>
                        <span>
                            Rental ID
                        </span>
                        <strong>
                            {
                                rentalReturn.rentalId
                            }
                        </strong>
                    </div>

                    <div>
                        <span>
                            Return Date
                        </span>
                        <strong>
                            {
                                rentalReturn.returnDate
                            }
                        </strong>
                    </div>

                    <div>
                        <span>
                            Processed By
                        </span>
                        <strong>
                            {
                                rentalReturn.processedBy
                            }
                        </strong>
                    </div>

                    <div>
                        <span>
                            Return Type
                        </span>
                        <strong>
                            {
                                rentalReturn.returnType
                            }
                        </strong>
                    </div>

                </div>


                <div className="notes-block">

                    <span>
                        Notes
                    </span>

                    <p>
                        {
                            rentalReturn.notes ||
                            "No notes"
                        }
                    </p>

                </div>

            </div>


            <div className="card">

                <h2>
                    Returned Items
                </h2>

                <div className="table-wrapper">

                    <table className="return-table">

                        <thead>

                        <tr>
                            <th>
                                Return Item ID
                            </th>
                            <th>
                                Rental Item ID
                            </th>
                            <th>
                                Quantity
                            </th>
                            <th>
                                Condition
                            </th>
                            <th>
                                Inspection Notes
                            </th>
                            <th>
                                Returned At
                            </th>
                        </tr>

                        </thead>

                        <tbody>

                        {
                            items.map(
                                (item) => (

                                    <tr
                                        key={
                                            item.returnItemId
                                        }
                                    >

                                        <td>
                                            {
                                                item.returnItemId
                                            }
                                        </td>

                                        <td>
                                            {
                                                item.rentalItemId
                                            }
                                        </td>

                                        <td>
                                            {
                                                item.quantityReturned
                                            }
                                        </td>

                                        <td>
                                            {
                                                item.conditionStatus
                                            }
                                        </td>

                                        <td>
                                            {
                                                item.inspectionNotes ||
                                                "-"
                                            }
                                        </td>

                                        <td>
                                            {
                                                item.returnedAt
                                            }
                                        </td>

                                    </tr>
                                )
                            )
                        }

                        </tbody>

                    </table>

                </div>

            </div>

        </div>
    );
}

export default ReturnDetails;
