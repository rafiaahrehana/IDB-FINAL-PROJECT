package com.businessos.shared.storage;

import com.businessos.auth.user.User;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg",
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".csv", ".txt"
    );

    private final SecurityUtil securityUtil;
    
    // Fallback if not configured in application.yml
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            // 0. Validate extension against whitelist
            validateFile(file);

            // 1. Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // 2. Get file extension
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 3. Generate filename: username_UID.extension
            User user = securityUtil.getCurrentUser();
            String prefix = "guest";
            String uid = UUID.randomUUID().toString().substring(0, 8);
            
            if (user != null) {
                // Remove spaces and special chars from name for safe filename
                String cleanName = (user.getFirstName() + "_" + user.getLastName())
                        .replaceAll("[^a-zA-Z0-9.-]", "_").toLowerCase();
                prefix = cleanName;
                uid = String.valueOf(user.getId()) + "_" + uid;
            }

            String fileName = prefix + "_" + uid + extension;

            // 4. Save file to disk — also guard against path traversal
            Path targetLocation = uploadPath.resolve(fileName).normalize();
            if (!targetLocation.startsWith(uploadPath)) {
                throw new BadRequestException("Invalid file name detected");
            }
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 5. Generate and return the URL
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

            return fileDownloadUri;

        } catch (BadRequestException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    /**
     * Validates that the uploaded file has an allowed extension.
     * Rejects empty files, files with no extension, and any extension not on the whitelist.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        if (!originalFilename.contains(".")) {
            throw new BadRequestException("File must have a valid extension");
        }
        String extension = originalFilename
                .substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException(
                "File type '" + extension + "' is not allowed. "
                + "Allowed types: " + ALLOWED_EXTENSIONS);
        }
    }
}
