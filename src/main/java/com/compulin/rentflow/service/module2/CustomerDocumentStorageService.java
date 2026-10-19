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

    public String saveDocument(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Identification document file is required.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException(
                    "Identification document filename is invalid.");
        }

        String extension = org.springframework.util.StringUtils
                .getFilenameExtension(originalName);

        extension = extension == null
                ? ""
                : extension.toLowerCase(java.util.Locale.ROOT);

        if (!java.util.Set.of("pdf", "jpg", "jpeg", "png")
                .contains(extension)) {
            throw new IllegalArgumentException(
                    "Only PDF, JPG, JPEG and PNG documents are allowed.");
        }

        String uniqueFileName = UUID.randomUUID() + "." + extension;

        try {
            Path uploadRoot = UPLOAD_DIRECTORY.toAbsolutePath().normalize();

            Files.createDirectories(uploadRoot);

            Path targetPath = uploadRoot.resolve(uniqueFileName).normalize();

            if (!targetPath.startsWith(uploadRoot)) {
                throw new IllegalArgumentException(
                        "Invalid document storage path.");
            }

            Files.copy(
                    file.getInputStream(),
                    targetPath
            );

            return UPLOAD_DIRECTORY.resolve(uniqueFileName)
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