package com.growx.service;

import com.growx.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Handles file storage for uploaded images (disease detection).
 *
 * Stores files under: {uploadDir}/disease-images/farm-{farmId}/{uuid}_{filename}
 *
 * The upload directory is configured via application properties:
 *   growx.storage.upload-dir=uploads
 *
 * This service is designed with a contract that can later be replaced
 * by a cloud storage implementation (e.g., AWS S3) without changing callers.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    private final Path rootUploadPath;

    public FileStorageService(@Value("${growx.storage.upload-dir:uploads}") String uploadDir) {
        this.rootUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootUploadPath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, ex);
        }
    }

    /**
     * Stores the uploaded image file and returns its relative path.
     *
     * @param file   the uploaded multipart file
     * @param farmId the farm this image belongs to (used to organise storage)
     * @return relative path of the stored file, suitable for storing in the database
     */
    public String storeImage(MultipartFile file, Long farmId) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + "_" + LocalDate.now() + "." + extension;

        // Organise by farm subdirectory
        Path farmDir = rootUploadPath.resolve("disease-images").resolve("farm-" + farmId);
        try {
            Files.createDirectories(farmDir);
            Path targetPath = farmDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return the relative path for storage in the DB
            return "disease-images/farm-" + farmId + "/" + uniqueFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store image file: " + ex.getMessage(), ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Image file must not exceed 10 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only PNG and JPEG image formats are accepted.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
