import {
    Link
} from "react-router-dom";


function Sidebar() {

    return (

        <div className="sidebar">

            <h2>
                RentFlow
            </h2>

            <Link to="/returns">
                Return Management
            </Link>

            <Link to="/returns/new">
                Process Return
            </Link>

            <Link to="/damages">
                Damage Records
            </Link>

            <Link to="/lost-items">
                Lost Items
            </Link>

            <Link to="/settlements">
                Settlements
            </Link>

        </div>
    );
}

export default Sidebar;
