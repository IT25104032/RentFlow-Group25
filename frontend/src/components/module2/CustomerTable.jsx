function CustomerTable({ customers, onSelectCustomer }) {

    if (!customers || customers.length === 0) {
        return (
            <div className="module2-empty-state">
                No renters found.
            </div>
        );
    }

    return (
        <div className="module2-table-wrapper">

            <table className="module2-table">

                <thead>
                <tr>
                    <th>Renter Name</th>
                    <th>Phone</th>
                    <th>Email</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
                </thead>

                <tbody>
                {customers.map((customer) => (
                    <tr key={customer.customerId}>

                        <td>
                            {customer.customerName}
                        </td>

                        <td>
                            {customer.phone}
                        </td>

                        <td>
                            {customer.email || "-"}
                        </td>

                        <td>
                            {customer.customerType}
                        </td>

                        <td>
                            {customer.customerStatus}
                        </td>

                        <td>
                            <button
                                type="button"
                                className="module2-btn module2-btn-primary module2-btn-small"
                                onClick={() =>
                                    onSelectCustomer(customer)
                                }
                            >
                                Select
                            </button>
                        </td>

                    </tr>
                ))}
                </tbody>

            </table>

        </div>
    );
}

export default CustomerTable;