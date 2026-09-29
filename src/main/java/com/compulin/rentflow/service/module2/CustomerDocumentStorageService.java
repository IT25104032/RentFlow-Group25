package com.compulin.rentflow.service.module2;


import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class CustomerDocumentStorageService {

    /*
     * Directory where customer identification
     * documents will be stored.
     */
    private static final Path UPLOAD_DIRECTORY =
            Paths.get("uploads/customer-documents");


    /*
     * Save the uploaded identification document.
     *
     * Returns the relative server path that
     * will be stored in the database.
     */
    public String saveDocument(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Identification document file is required."
            );
        }


        /*
         * Get the original filename supplied
         * by the user's browser.
         */
        String originalFileName =
                StringUtils.cleanPath(
                        file.getOriginalFilename()
                );


        if (originalFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "Identification document filename is invalid."
            );
        }


        /*
         * Generate a unique filename.
         *
         * Example:
         * UUID_kasun_nic.jpg
         */
        String uniqueFileName =
                UUID.randomUUID()
                        + "_"
                        + originalFileName;


        try {

            /*
             * Create the upload directory if
             * it does not already exist.
             */
            Files.createDirectories(
                    UPLOAD_DIRECTORY
            );


            /*
             * Create the complete target path.
             */
            Path targetPath =
                    UPLOAD_DIRECTORY.resolve(
                            uniqueFileName
                    );


            /*
             * Save the uploaded file.
             */
            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            /*
             * Return the relative path.
             *
             * This is what will be stored
             * in document_copy_path.
             */
            return targetPath
                    .toString()
                    .replace("\\", "/");

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to save identification document.",
                    exception
            );
        }
    }
}