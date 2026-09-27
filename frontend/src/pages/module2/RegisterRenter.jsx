import CustomerForm from "../../components/module2/CustomerForm";
import IdentificationForm from "../../components/module2/IdentificationForm";
import SecondaryContactForm from "../../components/module2/SecondaryContactForm";
import "../../components/module2/module2.css";

function RegisterRenter() {
    return (
        <div className="module2-page">
            <div className="module2-container">

                <div className="module2-header">
                    <h1>Register Renter</h1>
                    <p>
                        Create a new renter profile and record their identification details.
                    </p>
                </div>

                <section className="module2-card">
                    <CustomerForm />
                </section>

                <section className="module2-card">
                    <IdentificationForm />
                </section>

                <section className="module2-card">
                    <SecondaryContactForm />
                </section>

            </div>
        </div>
    );
}

export default RegisterRenter;