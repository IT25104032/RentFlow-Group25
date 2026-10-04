import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    createCustomer,
    getAllCustomers,
    searchCustomers,
    updateCustomer
} from "../../services/module2/customerService";

import {
    getDocumentsByCustomerId,
    createDocument,
    updateDocument
} from "../../services/module2/documentService";

import {
    getSecondaryContact,
    createSecondaryContact,
    updateSecondaryContact
} from "../../services/module2/secondaryContactService";

import RentalStepper from "../../components/module2/RentalStepper";

import "./RegisterRenter.css";



const COMPANY_ID = 1000;
const USER_ID = 3;


function RegisterRenter() {

    const navigate = useNavigate();
    /*
     * Search state
     */

    const documentFileInputRef = useRef(null);

    const [searchType, setSearchType] = useState("name");

    const [searchName, setSearchName] = useState("");

    const [searchPhone, setSearchPhone] = useState("");

    const [searchResults, setSearchResults] = useState([]);

    const [hasSearched, setHasSearched] = useState(false);

    const [allRenters, setAllRenters] = useState([]);

    const [loadingRenters, setLoadingRenters] = useState(false);

    const [renterLoadError, setRenterLoadError] = useState("");

    const [renterSearch, setRenterSearch] = useState("");

    const [renterSort, setRenterSort] = useState("newest");

    const [currentPage, setCurrentPage] = useState(1);

    const rentersPerPage = 5;

    async function loadRenters() {
        setLoadingRenters(true);
        setRenterLoadError("");

        try {
            const renters = await getAllCustomers(1000);
            setAllRenters(renters);
        } catch (error) {
            setRenterLoadError(
                error.message || "Failed to load renters."
            );
        } finally {
            setLoadingRenters(false);
        }
    }

    useEffect(() => {
        loadRenters();
    }, []);

    const filteredRenters = allRenters
        .filter(renter => {
            const searchValue =
                renterSearch.trim().toLowerCase();

            if (!searchValue) {
                return true;
            }

            const renterName =
                (renter.customerName || "").toLowerCase();

            const renterPhone =
                (renter.phone || "").toLowerCase();

            return (
                renterName.includes(searchValue) ||
                renterPhone.includes(searchValue)
            );
        })
        .sort((firstRenter, secondRenter) => {

            const firstDate =
                new Date(firstRenter.createdAt);

            const secondDate =
                new Date(secondRenter.createdAt);

            if (renterSort === "newest") {
                return secondDate - firstDate;
            }

            return firstDate - secondDate;
        });

    const totalPages =
        Math.ceil(
            filteredRenters.length / rentersPerPage
        );

    const startIndex =
        (currentPage - 1) * rentersPerPage;

    const paginatedRenters =
        filteredRenters.slice(
            startIndex,
            startIndex + rentersPerPage
        );

    /*
     * Selected renter
     */
    const [selectedRenter, setSelectedRenter] =
        useState(null);

    const [savedRenter, setSavedRenter] =
        useState(null);


    /*
     * Controls whether the registration
     * section is displayed.
     */
    const [showRenterForm, setShowRenterForm] =
        useState(false);


    /*
     * Loading / messages
     */
    const [loading, setLoading] =
        useState(false);

    const [saving, setSaving] =
        useState(false);

    const [error, setError] =
        useState("");

    const [success, setSuccess] =
        useState("");


    /*
     * Renter details
     */
    const [renterForm, setRenterForm] =
        useState({

            customerName: "",
            phone: "",
            email: "",
            address: "",
            customerType: "INDIVIDUAL",
            customerStatus: "ACTIVE"

        });


    /*
     * Identification
     */
    const [documentForm, setDocumentForm] =
        useState({

            documentType: "",
            documentNumber: "",
            documentCopy: null,
            expiryDate: "",
            notes: ""

        });

    const [existingDocumentFile, setExistingDocumentFile] = useState(null);
    const [existingDocumentId, setExistingDocumentId] = useState(null);

    /*
     * Secondary contact
     */
    const [secondaryForm, setSecondaryForm] =
        useState({

            contactName: "",
            relationship: "",
            phoneNumber: "",
            alternatePhone: "",
            email: "",
            address: "",
            notes: ""

        });


    /*
     * Accordion state
     */
    const [identificationOpen, setIdentificationOpen] =
        useState(false);

    const [secondaryOpen, setSecondaryOpen] =
        useState(false);


    /*
     * Change search type.
     */
    function handleSearchTypeChange(event) {

        const type = event.target.value;

        setSearchType(type);

        setSearchName("");
        setSearchPhone("");

        setSearchResults([]);
        setHasSearched(false);

        setError("");
        setSuccess("");
    }


    /*
     * Search existing renters.
     */
    async function handleSearch(event) {

        event.preventDefault();

        setError("");
        setSuccess("");

        const name =
            searchName.trim();

        const phone =
            searchPhone.trim();


        /*
         * Validate the search fields.
         */
        if (
            searchType === "name"
            && !name
        ) {

            setError(
                "Please enter a renter name."
            );

            return;
        }


        if (
            searchType === "phone"
            && !phone
        ) {

            setError(
                "Please enter a phone number."
            );

            return;
        }


        if (
            searchType === "both"
            && (!name || !phone)
        ) {

            setError(
                "Please enter both name and phone number."
            );

            return;
        }


        setLoading(true);


        try {

            const results =
                await searchCustomers({

                    companyId: COMPANY_ID,

                    name:
                        searchType === "name"
                        || searchType === "both"
                            ? name
                            : null,

                    phone:
                        searchType === "phone"
                        || searchType === "both"
                            ? phone
                            : null

                });


            setSearchResults(results);

            setHasSearched(true);

        } catch (err) {

            console.error(err);

            setError(
                "Unable to search renters."
            );

        } finally {

            setLoading(false);
        }
    }


    /*
     * Select an existing renter.
     *
     * The renter information is copied
     * into the editable form.
     */
    async function handleSelectRenter(renter) {
        setSelectedRenter(renter);
        setShowRenterForm(true);

        setRenterForm({
            customerName: renter.customerName || "",
            phone: renter.phone || "",
            email: renter.email || "",
            address: renter.address || "",
            customerType: renter.customerType || "INDIVIDUAL",
            customerStatus: renter.customerStatus || "ACTIVE"
        });

        setError("");
        setSuccess("");

        try {
            const existingDocuments =
                await getDocumentsByCustomerId(
                    renter.customerId
                );

            if (
                existingDocuments &&
                existingDocuments.length > 0
            ) {
                const document =
                    existingDocuments[0];

                setDocumentForm({
                    documentType:
                        document.documentType || "",
                    documentNumber:
                        document.documentNumber || "",
                    documentCopy: null,
                    expiryDate:
                        document.expiryDate || "",
                    notes:
                        document.notes || ""
                });

                setExistingDocumentId(document.documentId || null);
                setExistingDocumentFile(document.documentCopyPath || null);
            } else {
                setDocumentForm({
                    documentType: "",
                    documentNumber: "",
                    documentCopy: null,
                    expiryDate: "",
                    notes: ""
                });

                setExistingDocumentFile(null);
            }


            const existingContact =
                await getSecondaryContact(
                    renter.customerId
                );

            if (existingContact) {
                setSecondaryForm({
                    contactName:
                        existingContact.contactName || "",
                    relationship:
                        existingContact.relationship || "",
                    phoneNumber:
                        existingContact.phoneNumber || "",
                    alternatePhone:
                        existingContact.alternatePhone || "",
                    email:
                        existingContact.email || "",
                    address:
                        existingContact.address || "",
                    notes:
                        existingContact.notes || ""
                });
            } else {
                setSecondaryForm({
                    contactName: "",
                    relationship: "",
                    phoneNumber: "",
                    alternatePhone: "",
                    email: "",
                    address: "",
                    notes: ""
                });
            }

            setIdentificationOpen(false);
            setSecondaryOpen(false);

        } catch (err) {
            console.error(err);

            setError(
                "Unable to load the renter's additional details."
            );
        }
    }


    /*
     * Start registering a brand-new renter.
     */
    function handleRegisterNewRenter() {

        setSelectedRenter(null);

        setShowRenterForm(true);

        setRenterForm({

            customerName: "",
            phone: "",
            email: "",
            address: "",
            customerType: "INDIVIDUAL",
            customerStatus: "ACTIVE"

        });


        setDocumentForm({

            documentType: "",
            documentNumber: "",
            documentCopy: null,
            expiryDate: "",
            notes: ""

        });
        setExistingDocumentFile(null);
        setExistingDocumentId(null);

        setSecondaryForm({

            contactName: "",
            relationship: "",
            phoneNumber: "",
            alternatePhone: "",
            email: "",
            address: "",
            notes: ""

        });


        setIdentificationOpen(false);
        setSecondaryOpen(false);

        setError("");
        setSuccess("");
    }


    /*
     * Change renter.
     */
    function handleChangeRenter() {

        setSelectedRenter(null);

        setShowRenterForm(false);

        setIdentificationOpen(false);
        setSecondaryOpen(false);

        setError("");
        setSuccess("");
    }


    /*
     * Change renter form fields.
     */
    function handleRenterChange(event) {

        const {
            name,
            value
        } = event.target;


        setRenterForm(
            previous => ({

                ...previous,

                [name]: value

            })
        );
    }


    /*
     * Change identification fields.
     */
    function handleDocumentChange(event) {

        const {
            name,
            value
        } = event.target;


        setDocumentForm(
            previous => ({

                ...previous,

                [name]: value

            })
        );
    }


    /*
     * Select identification file.
     */
    function handleDocumentFileChange(event) {

        const file =
            event.target.files[0] || null;


        setDocumentForm(
            previous => ({

                ...previous,

                documentCopy: file

            })
        );
    }


    /*
    * Remove the currently selected identification file.
    */
    function handleRemoveDocumentFile() {

        setDocumentForm(
            previous => ({
                ...previous,
                documentCopy: null
            })
        );

        if (documentFileInputRef.current) {
            documentFileInputRef.current.value = "";
        }
    }

    /*
     * Change secondary contact fields.
     */
    function handleSecondaryChange(event) {

        const {
            name,
            value
        } = event.target;


        setSecondaryForm(
            previous => ({

                ...previous,

                [name]: value

            })
        );
    }


    /*
     * Save identification document.
     */
    async function saveIdentification(customerId) {

        /*
         * If the identification section is
         * collapsed, identification is optional.
         */
        if (!identificationOpen) {
            return;
        }


        /*
         * Validate the required identification
         * fields before sending data to the backend.
         */
        if (
            !documentForm.documentType ||
            !documentForm.documentNumber
        ) {
            throw new Error(
                "Please complete the identification document details."
            );
        }


        /*
         * If this renter already has an identification
         * document and the user did not select a new file,
         * keep the existing document unchanged.
         */
        if (
            selectedRenter &&
            existingDocumentId &&
            !documentForm.documentCopy
        ) {
            return;
        }


        /*
         * Prepare the identification data that will
         * be sent to Spring Boot.
         */
        const documentData = {
            customerId,

            documentType:
            documentForm.documentType,

            documentNumber:
            documentForm.documentNumber,

            documentFile:
            documentForm.documentCopy,

            expiryDate:
                documentForm.expiryDate || null,

            checkedBy:
            USER_ID,

            notes:
                documentForm.notes || null
        };


        /*
         * If this renter already has an identification
         * document, update that existing document.
         */
        if (
            selectedRenter &&
            existingDocumentId
        ) {

            await updateDocument(
                existingDocumentId,
                customerId,
                documentData
            );

            return;
        }


        /*
         * If there is no existing identification document,
         * create a new one.
         */
        await createDocument(
            documentData
        );
    }

    /*
 * Save secondary contact.
 *
 * If the customer does not have a secondary
 * contact yet, create one.
 *
 * If the customer already has one, update it.
 */
    async function saveSecondaryContact(
        customerId
    ) {

        if (!secondaryOpen) {
            return;
        }


        if (
            !secondaryForm.contactName
            || !secondaryForm.phoneNumber
        ) {

            throw new Error(
                "Please complete the secondary contact details."
            );
        }


        /*
         * Prepare the contact information
         * entered by the staff user.
         */
        const contactData = {

            customerId,

            contactName:
            secondaryForm.contactName,

            relationship:
            secondaryForm.relationship,

            phoneNumber:
            secondaryForm.phoneNumber,

            alternatePhone:
            secondaryForm.alternatePhone,

            email:
            secondaryForm.email,

            address:
            secondaryForm.address,

            notes:
            secondaryForm.notes
        };


        /*
         * First check whether this renter
         * already has a secondary contact.
         */
        const existingContact =
            await getSecondaryContact(
                customerId
            );


        /*
         * No existing contact:
         * create a new record.
         */
        if (!existingContact) {

            await createSecondaryContact(
                contactData
            );

            return;
        }


        /*
         * Existing contact:
         * update that existing record.
         */
        await updateSecondaryContact(

            existingContact.secondaryContactId,

            customerId,

            contactData
        );
    }


    /*
     * Save renter.
     *
     * New renter:
     *   create customer
     *
     * Existing renter:
     *   update customer
     */
    async function handleSaveRenter(event) {

        event.preventDefault();

        setError("");
        setSuccess("");

        setSaving(true);


        try {

            let customer;


            if (selectedRenter) {

                customer =
                    await updateCustomer(

                        selectedRenter.customerId,

                        COMPANY_ID,

                        {

                            companyId:
                            COMPANY_ID,

                            customerName:
                            renterForm.customerName,

                            email:
                            renterForm.email,

                            phone:
                            renterForm.phone,

                            address:
                            renterForm.address,

                            customerType:
                            renterForm.customerType,

                            customerStatus:
                            renterForm.customerStatus,

                            createdBy:
                            selectedRenter.createdBy

                        }
                    );

            } else {

                customer =
                    await createCustomer({

                        companyId:
                        COMPANY_ID,

                        customerName:
                        renterForm.customerName,

                        email:
                        renterForm.email,

                        phone:
                        renterForm.phone,

                        address:
                        renterForm.address,

                        customerType:
                        renterForm.customerType,

                        customerStatus:
                        renterForm.customerStatus,

                        createdBy:
                        USER_ID

                    });
            }


            /*
             * Save manually entered identification.
             */
            await saveIdentification(
                customer.customerId
            );


            /*
             * Save manually entered secondary contact.
             */
            await saveSecondaryContact(
                customer.customerId
            );

            await loadRenters();

            /*
             * Keep the renter selected.
             */
            setSelectedRenter(customer);
            setSavedRenter(customer);
            setShowRenterForm(true);

            setSuccess(
                selectedRenter
                    ? "Renter updated successfully."
                    : "Renter registered successfully."
            );

        } catch (err) {

            console.error(err);

            setError(
                err.message ||
                "Failed to save renter."
            );

        } finally {

            setSaving(false);
        }
    }


    function handleContinueToRental() {

        if (!selectedRenter) {
            setError("Please save or select a renter first.");
            return;
        }

        navigate("/rentals/create", {
            state: {
                selectedRenter: savedRenter
            }
        });
    }

    return (

        <div className="register-renter-page">


            {/* =====================================
                TOP NAVIGATION
            ===================================== */}

            <header className="register-renter-topbar">

                <div className="register-renter-brand">
                    RentFlow
                </div>


                <div className="register-renter-user">

                    <div className="register-renter-user-avatar">
                        <span>●</span>
                    </div>

                    <span>
                        Rental Officer
                    </span>

                    <span className="register-renter-chevron">
                       ⌄
                    </span>

                </div>

            </header>


            {/* =====================================
                MAIN CONTENT
            ===================================== */}

            <main className="register-renter-content">


                {/* PAGE HEADING */}

                <div className="register-renter-heading">

                    <h1>
                        Register Renter
                    </h1>

                    <p>
                        Search for an existing renter or register a new one,
                        then proceed to add details.
                    </p>

                </div>


                {/* =================================
                    PROGRESS STEPPER
                ================================= */}

                <RentalStepper activeStep={1} />

                {/* =================================
                    REGISTERED RENTERS
                ================================= */}

                {!showRenterForm && (
                    <section className="register-renter-card">

                        <div className="register-renter-card-heading">
                            <div className="heading-icon">⌕</div>

                            <h2>Registered Renters</h2>
                        </div>

                        <div className="register-renter-search-content">

                            <div className="search-controls">

                                <input
                                    type="text"
                                    value={renterSearch}
                                    onChange={event => {
                                        setRenterSearch(event.target.value);
                                        setCurrentPage(1);
                                    }}
                                    placeholder="Search by renter name or phone number"
                                />

                                <select
                                    value={renterSort}
                                    onChange={event => {
                                        setRenterSort(event.target.value);
                                        setCurrentPage(1);
                                    }}
                                >
                                    <option value="newest">
                                        Newest
                                    </option>

                                    <option value="oldest">
                                        Oldest
                                    </option>
                                </select>

                            </div>

                        </div>

                        {/* =================================
                            RENTERS TABLE
                        ================================= */}

                        <div className="search-results">

                            <div className="results-title">
                                Registered Renters
                                <span>({filteredRenters.length})</span>
                            </div>

                            {loadingRenters && (
                                <div className="no-results">
                                    <p>Loading renters...</p>
                                </div>
                            )}

                            {renterLoadError && (
                                <div className="no-results">
                                    <p>{renterLoadError}</p>
                                </div>
                            )}

                            {!loadingRenters &&
                                !renterLoadError &&
                                filteredRenters.length > 0 && (

                                    <div className="results-table-wrapper">
                                        <table>

                                            <thead>
                                            <tr>
                                                <th>Name</th>
                                                <th>Phone</th>
                                                <th>Email</th>
                                                <th>Type</th>
                                                <th>Status</th>
                                                <th>Action</th>
                                            </tr>
                                            </thead>

                                            <tbody>
                                            {paginatedRenters.map(renter => (
                                                <tr key={renter.customerId}>

                                                    <td>
                                                        {renter.customerName}
                                                    </td>

                                                    <td>
                                                        {renter.phone}
                                                    </td>

                                                    <td>
                                                        {renter.email || "-"}
                                                    </td>

                                                    <td>
                                                        {renter.customerType}
                                                    </td>

                                                    <td>
                                        <span
                                            className={
                                                renter.customerStatus ===
                                                "ACTIVE"
                                                    ? "status active"
                                                    : "status"
                                            }
                                        >
                                            {renter.customerStatus}
                                        </span>
                                                    </td>

                                                    <td>
                                                        <button
                                                            type="button"
                                                            className="select-button"
                                                            onClick={() =>
                                                                handleSelectRenter(renter)
                                                            }
                                                        >
                                                            Edit
                                                        </button>
                                                    </td>

                                                </tr>
                                            ))}
                                            </tbody>

                                        </table>
                                        {totalPages > 1 && (
                                            <div className="pagination">

                                                <button
                                                    type="button"
                                                    disabled={currentPage === 1}
                                                    onClick={() =>
                                                        setCurrentPage(currentPage - 1)
                                                    }
                                                >
                                                    Previous
                                                </button>

                                                <span>
                                                    Page {currentPage} of {totalPages}
                                                </span>

                                                <button
                                                    type="button"
                                                    disabled={currentPage === totalPages}
                                                    onClick={() =>
                                                        setCurrentPage(currentPage + 1)
                                                    }
                                                >
                                                    Next
                                                </button>

                                            </div>
                                        )}
                                    </div>
                                )}

                            {!loadingRenters &&
                                !renterLoadError &&
                                filteredRenters.length === 0 && (

                                    <div className="no-results">

                                        <div className="no-results-icon">
                                            ⌕
                                        </div>

                                        <h3>
                                            {renterSearch
                                                ? "No renters found"
                                                : "No renters registered"}
                                        </h3>

                                        <p>
                                            {renterSearch
                                                ? "No renters match your search."
                                                : "There are currently no renters registered for this company."}
                                        </p>

                                    </div>
                                )}

                        </div>

                        {/* =================================
                            REGISTER NEW RENTER
                        ================================= */}

                        <div className="register-new-area">
                            <button
                                type="button"
                                className="primary-button register-new-button"
                                onClick={handleRegisterNewRenter}
                            >
                                <span>+</span>
                                Register New Renter
                            </button>
                        </div>

                    </section>
                )}

                {/* =====================================
                    RENTER DETAILS PAGE
                ===================================== */}

                {showRenterForm && (

                    <section className="register-form-card">


                        {/* FORM HEADER */}

                        <div className="register-form-heading">

                            <div>

                                <span className="form-heading-icon">
                                    👥
                                </span>

                                <strong>
                                    {selectedRenter
                                        ? "Renter Details"
                                        : "Register New Renter"}
                                </strong>

                            </div>


                            <button
                                type="button"
                                onClick={
                                    handleChangeRenter
                                }
                                className="change-renter-button"
                            >
                                Change Renter
                            </button>

                        </div>


                        <form
                            onSubmit={handleSaveRenter}
                        >


                            {/* RENTER DETAILS */}

                            <div className="renter-details-section">

                                <div className="section-title">
                                    Renter Details
                                </div>


                                <div className="renter-fields-grid">


                                    <div className="field">

                                        <label>
                                            Full Name
                                            <span>*</span>
                                        </label>

                                        <input
                                            name="customerName"
                                            value={
                                                renterForm.customerName
                                            }
                                            onChange={
                                                handleRenterChange
                                            }
                                            required
                                        />

                                    </div>


                                    <div className="field">

                                        <label>
                                            Customer Type
                                        </label>

                                        <select
                                            name="customerType"
                                            value={
                                                renterForm.customerType
                                            }
                                            onChange={
                                                handleRenterChange
                                            }
                                        >

                                            <option value="INDIVIDUAL">
                                                Individual
                                            </option>

                                            <option value="BUSINESS">
                                                Business
                                            </option>

                                        </select>

                                    </div>


                                    <div className="field">

                                        <label>
                                            Phone Number
                                            <span>*</span>
                                        </label>

                                        <input
                                            name="phone"
                                            value={
                                                renterForm.phone
                                            }
                                            onChange={
                                                handleRenterChange
                                            }
                                            required
                                        />

                                    </div>


                                    <div className="field">

                                        <label>
                                            Email
                                        </label>

                                        <input
                                            type="email"
                                            name="email"
                                            value={
                                                renterForm.email
                                            }
                                            onChange={
                                                handleRenterChange
                                            }
                                        />

                                    </div>


                                    <div className="field full">

                                        <label>
                                            Address
                                        </label>

                                        <textarea
                                            name="address"
                                            value={
                                                renterForm.address
                                            }
                                            onChange={
                                                handleRenterChange
                                            }
                                        />

                                    </div>


                                    <div className="field status-field">

                                        <label>
                                            Status
                                        </label>

                                        <select
                                            name="customerStatus"
                                            value={
                                                renterForm.customerStatus
                                            }
                                            onChange={
                                                handleRenterChange
                                            }
                                        >

                                            <option value="ACTIVE">
                                                ACTIVE
                                            </option>

                                            <option value="INACTIVE">
                                                INACTIVE
                                            </option>

                                            <option value="BLOCKED">
                                                BLOCKED
                                            </option>

                                        </select>

                                    </div>

                                </div>

                            </div>


                            {/* =================================
                                IDENTIFICATION
                            ================================= */}

                            <div className="accordion">

                                <button
                                    type="button"
                                    className="accordion-header"
                                    onClick={() =>
                                        setIdentificationOpen(
                                            !identificationOpen
                                        )
                                    }
                                >

                                    <span>

                                        <span className="accordion-icon">
                                            ▣
                                        </span>

                                        Identification Document

                                    </span>

                                    <span>
                                        {identificationOpen
                                            ? "⌃"
                                            : "⌄"}
                                    </span>

                                </button>


                                {identificationOpen && (

                                    <div className="accordion-body">

                                        <div className="renter-fields-grid">

                                            <div className="field">

                                                <label>
                                                    Document Type
                                                    <span>*</span>
                                                </label>

                                                <select
                                                    name="documentType"
                                                    value={
                                                        documentForm.documentType
                                                    }
                                                    onChange={
                                                        handleDocumentChange
                                                    }
                                                >

                                                    <option value="">
                                                        Select document
                                                    </option>

                                                    <option value="NIC_ID">
                                                        NIC / ID
                                                    </option>

                                                    <option value="DRIVING_LICENCE">
                                                        Driving Licence
                                                    </option>

                                                    <option value="PASSPORT">
                                                        Passport
                                                    </option>

                                                    <option value="OTHER">
                                                        Other
                                                    </option>

                                                </select>

                                            </div>


                                            <div className="field">

                                                <label>
                                                    Document Number
                                                    <span>*</span>
                                                </label>

                                                <input
                                                    name="documentNumber"
                                                    value={
                                                        documentForm.documentNumber
                                                    }
                                                    onChange={
                                                        handleDocumentChange
                                                    }
                                                />

                                            </div>


                                            <div className="field">

                                                <label>
                                                    Document Copy
                                                    <span>*</span>
                                                </label>

                                                <input
                                                    id="document-file-input"
                                                    ref={documentFileInputRef}
                                                    type="file"
                                                    accept=".jpg,.jpeg,.png,.pdf"
                                                    onChange={
                                                        handleDocumentFileChange
                                                    }
                                                />

                                                {existingDocumentFile && !documentForm.documentCopy && (
                                                    <div className="selected-file">
                                                        <span className="selected-file-name">
                                                            📄 Existing document:{" "}
                                                            {existingDocumentFile.split("/").pop()}
                                                        </span>
                                                    </div>
                                                )}

                                                {documentForm.documentCopy && (

                                                    <div className="selected-file">

                                                        <span className="selected-file-name">

                                                            📄{" "}
                                                            {documentForm.documentCopy.name}

                                                        </span>

                                                        <button
                                                            type="button"
                                                            className="remove-file-button"
                                                            onClick={
                                                                handleRemoveDocumentFile
                                                            }
                                                            aria-label="Remove selected document"
                                                        >
                                                            ×
                                                        </button>

                                                    </div>

                                                )}

                                                <small className="file-help-text">
                                                    Supported: JPG, JPEG, PNG, PDF
                                                </small>

                                            </div>

                                            <div className="field">

                                                <label>
                                                    Expiry Date
                                                </label>

                                                <input
                                                    type="date"
                                                    name="expiryDate"
                                                    value={
                                                        documentForm.expiryDate
                                                    }
                                                    onChange={
                                                        handleDocumentChange
                                                    }
                                                />

                                            </div>


                                            <div className="field full">

                                                <label>
                                                    Notes
                                                </label>

                                                <input
                                                    name="notes"
                                                    value={
                                                        documentForm.notes
                                                    }
                                                    onChange={
                                                        handleDocumentChange
                                                    }
                                                />

                                            </div>

                                        </div>

                                    </div>

                                )}

                            </div>


                            {/* =================================
                                SECONDARY CONTACT
                            ================================= */}

                            <div className="accordion">

                                <button
                                    type="button"
                                    className="accordion-header"
                                    onClick={() =>
                                        setSecondaryOpen(
                                            !secondaryOpen
                                        )
                                    }
                                >

                                    <span>

                                        <span className="accordion-icon">
                                            ☎
                                        </span>

                                        Secondary Contact

                                    </span>

                                    <span>
                                        {secondaryOpen
                                            ? "⌃"
                                            : "⌄"}
                                    </span>

                                </button>


                                {secondaryOpen && (

                                    <div className="accordion-body">

                                        <div className="renter-fields-grid">

                                            <div className="field">

                                                <label>
                                                    Contact Name
                                                    <span>*</span>
                                                </label>

                                                <input
                                                    name="contactName"
                                                    value={
                                                        secondaryForm.contactName
                                                    }
                                                    onChange={
                                                        handleSecondaryChange
                                                    }
                                                />

                                            </div>


                                            <div className="field">

                                                <label>
                                                    Relationship
                                                </label>

                                                <input
                                                    name="relationship"
                                                    value={
                                                        secondaryForm.relationship
                                                    }
                                                    onChange={
                                                        handleSecondaryChange
                                                    }
                                                />

                                            </div>


                                            <div className="field">

                                                <label>
                                                    Phone Number
                                                    <span>*</span>
                                                </label>

                                                <input
                                                    name="phoneNumber"
                                                    value={
                                                        secondaryForm.phoneNumber
                                                    }
                                                    onChange={
                                                        handleSecondaryChange
                                                    }
                                                />

                                            </div>


                                            <div className="field">

                                                <label>
                                                    Alternate Phone
                                                </label>

                                                <input
                                                    name="alternatePhone"
                                                    value={
                                                        secondaryForm.alternatePhone
                                                    }
                                                    onChange={
                                                        handleSecondaryChange
                                                    }
                                                />

                                            </div>


                                            <div className="field">

                                                <label>
                                                    Email
                                                </label>

                                                <input
                                                    type="email"
                                                    name="email"
                                                    value={
                                                        secondaryForm.email
                                                    }
                                                    onChange={
                                                        handleSecondaryChange
                                                    }
                                                />

                                            </div>


                                            <div className="field">

                                                <label>
                                                    Address
                                                </label>

                                                <input
                                                    name="address"
                                                    value={
                                                        secondaryForm.address
                                                    }
                                                    onChange={
                                                        handleSecondaryChange
                                                    }
                                                />

                                            </div>


                                            <div className="field full">

                                                <label>
                                                    Notes
                                                </label>

                                                <input
                                                    name="notes"
                                                    value={
                                                        secondaryForm.notes
                                                    }
                                                    onChange={
                                                        handleSecondaryChange
                                                    }
                                                />

                                            </div>

                                        </div>

                                    </div>

                                )}

                            </div>


                            {/* MESSAGES */}

                            {error && (

                                <div className="form-error">
                                    {error}
                                </div>

                            )}


                            {success && (

                                <div className="form-success">
                                    {success}
                                </div>

                            )}


                            {/* ACTIONS */}

                            <div className="form-actions">

                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={handleChangeRenter}
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="primary-button"
                                    disabled={saving}
                                >
                                    {saving
                                        ? "Saving..."
                                        : "Save Renter"}
                                </button>

                                <button
                                    type="button"
                                    className="primary-button"
                                    onClick={handleContinueToRental}
                                    disabled={saving || !savedRenter}
                                >
                                    Continue to Rental Details
                                </button>

                            </div>

                        </form>

                    </section>

                )}

            </main>

        </div>
    );
}


export default RegisterRenter;