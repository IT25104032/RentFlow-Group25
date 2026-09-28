import { useRef, useState } from "react";
import { createDocument } from "../../services/module2/documentService";

function IdentificationForm({ customerId }) {

    const fileInputRef = useRef(null);

    const [documentData, setDocumentData] = useState({
        documentType: "",
        documentNumber: "",
        documentCopy: null,
        expiryDate: "",
        notes: ""
    });

    function handleChange(event) {
        const { name, value } = event.target;

        setDocumentData({
            ...documentData,
            [name]: value
        });
    }

    function handleFileChange(event) {
        const file = event.target.files[0];

        setDocumentData({
            ...documentData,
            documentCopy: file
        });
    }

    function removeDocument() {
        setDocumentData({
            ...documentData,
            documentCopy: null
        });

        if (fileInputRef.current) {
            fileInputRef.current.value = "";
        }
    }

    function clearForm() {
        setDocumentData({
            documentType: "",
            documentNumber: "",
            documentCopy: null,
            expiryDate: "",
            notes: ""
        });

        if (fileInputRef.current) {
            fileInputRef.current.value = "";
        }
    }

    function clearDocumentType() {
        setDocumentData({
            ...documentData,
            documentType: ""
        });
    }

    async function handleSubmit(event) {
        event.preventDefault();

        if (!customerId) {
            alert("Please register the renter first.");
            return;
        }

        if (!documentData.documentCopy) {
            alert("Please select a document copy.");
            return;
        }

        try {
            const documentRequest = {
                customerId: customerId,
                documentType: documentData.documentType,
                documentNumber: documentData.documentNumber,
                documentCopyPath: documentData.documentCopy.name,
                expiryDate: documentData.expiryDate || null,
                checkedBy: 3,
                notes: documentData.notes
            };

            const response = await createDocument(documentRequest);

            console.log("Identification document saved:", response);

            alert("Identification document saved successfully!");

            clearForm();

        } catch (error) {
            console.error(
                "Failed to save identification document:",
                error
            );

            alert("Failed to save identification document.");
        }
    }

    return (
        <div>
            <div className="module2-card-header">
                <div className="module2-section-number">02</div>

                <div>
                    <h2>Identification Document</h2>
                    <p className="module2-card-description">
                        Record the renter's identification information.
                    </p>
                </div>
            </div>

            <form
                className="module2-form"
                onSubmit={handleSubmit}
            >
                <div className="module2-form-row">

                    <div className="module2-form-group">
                        <label>
                            Document Type
                            <span className="module2-required">*</span>
                        </label>

                        <div className="module2-select-wrapper">

                            <select
                                name="documentType"
                                value={documentData.documentType}
                                onChange={handleChange}
                                required
                            >
                                <option value="" disabled>
                                    Select document type
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

                            <button
                                type="button"
                                className="module2-clear-selection"
                                onClick={clearDocumentType}
                            >
                                Clear
                            </button>

                        </div>
                    </div>

                    <div className="module2-form-group">
                        <label>
                            Document Number
                            <span className="module2-required">*</span>
                        </label>

                        <input
                            type="text"
                            name="documentNumber"
                            value={documentData.documentNumber}
                            onChange={handleChange}
                            placeholder="Enter document number"
                            required
                        />
                    </div>

                </div>

                <div className="module2-form-row">

                    <div className="module2-form-group">
                        <label>
                            Document Copy
                            <span className="module2-required">*</span>
                        </label>

                        <input
                            ref={fileInputRef}
                            type="file"
                            onChange={handleFileChange}
                            required
                        />

                        {documentData.documentCopy && (
                            <div className="module2-file-preview">

                                <span className="module2-file-name">
                                    📄 {documentData.documentCopy.name}
                                </span>

                                <button
                                    type="button"
                                    className="module2-file-remove"
                                    onClick={removeDocument}
                                    aria-label="Remove selected document"
                                >
                                    ×
                                </button>

                            </div>
                        )}
                    </div>

                    <div className="module2-form-group">
                        <label>Expiry Date</label>

                        <input
                            type="date"
                            name="expiryDate"
                            value={documentData.expiryDate}
                            onChange={handleChange}
                        />
                    </div>

                </div>

                <div className="module2-form-group">

                    <label>Notes</label>

                    <input
                        type="text"
                        name="notes"
                        value={documentData.notes}
                        onChange={handleChange}
                        placeholder="Optional notes"
                    />

                </div>

                <div className="module2-form-actions">

                    <button
                        type="button"
                        className="module2-btn module2-btn-secondary"
                        onClick={clearForm}
                    >
                        Clear Form
                    </button>

                    <button
                        type="submit"
                        className="module2-btn module2-btn-primary"
                        disabled={!customerId}
                    >
                        Save Identification
                    </button>

                </div>

            </form>
        </div>
    );
}

export default IdentificationForm;