package com.businessos.modules.itam.assetimport;

import com.businessos.modules.hrm.asset.Asset;
import com.businessos.modules.hrm.asset.AssetRepository;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.enums.AssetStatus;
import com.businessos.modules.company.Company;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetImportServiceImpl implements AssetImportService {

    // Column order matches AssetRequest (create/update via the regular Asset UI),
    // plus the IT-hardware fields already exposed on AssetResponse/AssetRequest.
    private static final String[] HEADERS = {
        "name", "category", "serialNumber", "description", "purchaseDate", "purchaseCost",
        "assignedToEmployeeId", "assetTag", "brand", "model", "ipAddress", "macAddress",
        "processorModel", "ramSize", "storageSize", "operatingSystem", "warrantyExpiry"
    };

    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityUtil securityUtil;
    private final TransactionTemplate transactionTemplate;

    @Override
    public String getTemplateCsv() {
        return String.join(",", HEADERS) + "\n";
    }

    @Override
    public AssetImportResultResponse importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required");
        }

        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context");
        }

        List<String[]> rows;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            rows = reader.lines().skip(1).map(this::parseCsvLine).toList();
        } catch (IOException e) {
            throw new BadRequestException("Could not read CSV file: " + e.getMessage());
        }

        int succeeded = 0;
        List<AssetImportRowError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNumber = i + 2; // +1 for 0-index, +1 for header row
            String[] cols = rows.get(i);
            if (cols.length == 1 && cols[0].isBlank()) continue; // skip blank lines

            try {
                // Each row committed in its own transaction, so one bad row doesn't
                // roll back the rows already successfully imported.
                transactionTemplate.executeWithoutResult(status -> importRow(cols, companyId, rowNumber));
                succeeded++;
            } catch (Exception e) {
                errors.add(new AssetImportRowError(rowNumber, e.getMessage()));
            }
        }

        return new AssetImportResultResponse(rows.size(), succeeded, errors.size(), errors);
    }

    private void importRow(String[] cols, Long companyId, int rowNumber) {
        String name = col(cols, 0);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        Asset asset = new Asset();
        asset.setName(name);
        asset.setCategory(col(cols, 1));
        asset.setSerialNumber(col(cols, 2));
        asset.setNotes(col(cols, 3));
        asset.setPurchaseDate(parseDate(col(cols, 4), "purchaseDate"));
        asset.setPurchasePrice(parseDouble(col(cols, 5), "purchaseCost"));

        Company company = new Company();
        company.setId(companyId);
        asset.setCompany(company);
        asset.setStatus(AssetStatus.AVAILABLE);

        String employeeIdStr = col(cols, 6);
        if (employeeIdStr != null && !employeeIdStr.isBlank()) {
            Long employeeId = parseLong(employeeIdStr, "assignedToEmployeeId");
            Employee employee = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("assignedToEmployeeId " + employeeId + " not found"));
            asset.setAssignedTo(employee);
            asset.setStatus(AssetStatus.ASSIGNED);
            asset.setAssignedAt(LocalDate.now());
        }

        asset.setAssetTag(col(cols, 7));
        asset.setBrand(col(cols, 8));
        asset.setModel(col(cols, 9));
        asset.setIpAddress(col(cols, 10));
        asset.setMacAddress(col(cols, 11));
        asset.setProcessorModel(col(cols, 12));
        asset.setRamSize(col(cols, 13));
        asset.setStorageSize(col(cols, 14));
        asset.setOperatingSystem(col(cols, 15));
        asset.setWarrantyExpiry(parseDate(col(cols, 16), "warrantyExpiry"));

        assetRepository.save(asset);
    }

    private String col(String[] cols, int index) {
        if (index >= cols.length) return null;
        String value = cols[index].trim();
        return value.isEmpty() ? null : value;
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid date (YYYY-MM-DD): " + value);
        }
    }

    private Double parseDouble(String value, String fieldName) {
        if (value == null) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a number: " + value);
        }
    }

    private Long parseLong(String value, String fieldName) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a whole number: " + value);
        }
    }

    // Minimal RFC4180-style CSV line parser (handles quoted fields containing commas).
    // Not using a library here to avoid adding a new dependency for a single-file need.
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
