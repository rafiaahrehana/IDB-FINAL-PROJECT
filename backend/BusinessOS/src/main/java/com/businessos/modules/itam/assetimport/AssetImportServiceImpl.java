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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetImportServiceImpl implements AssetImportService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final int MAX_ROWS = 1000;

    // Column order matches AssetRequest (create/update via the regular Asset UI),
    // plus the IT-hardware fields already exposed on AssetResponse/AssetRequest.
    // "employeeNumber" (not a raw DB id) - whoever fills this CSV in has the
    // human-readable EMP-0001 style number on hand, not an internal primary key.
    private static final String[] HEADERS = {
        "name", "category", "serialNumber", "description", "purchaseDate", "purchaseCost",
        "employeeNumber", "assetTag", "brand", "model", "ipAddress", "macAddress",
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
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size must be less than 5 MB");
        }

        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context");
        }

        List<String[]> rows;
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            rows = parseCsv(content);
            if (!rows.isEmpty()) {
                rows = rows.subList(1, rows.size()); // skip header
            }
        } catch (IOException e) {
            throw new BadRequestException("Could not read CSV file");
        }
        if (rows.size() > MAX_ROWS) {
            throw new BadRequestException("CSV file must not contain more than " + MAX_ROWS + " rows");
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
                String msg = e.getMessage();
                if (msg == null || msg.isBlank()) {
                    msg = "Row import failed";
                } else if (msg.contains("Exception") || msg.contains("stack") || msg.contains("\\")
                           || msg.contains("/") || msg.contains(":")) {
                    msg = "Row validation failed: " + e.getClass().getSimpleName();
                }
                errors.add(new AssetImportRowError(rowNumber, msg));
            }
        }

        return new AssetImportResultResponse(rows.size(), succeeded, errors.size(), errors);
    }

    private void importRow(String[] cols, Long companyId, int rowNumber) {
        String name = col(cols, 0);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        String serialNumber = col(cols, 2);
        if (serialNumber != null && assetRepository.existsByCompanyIdAndSerialNumber(companyId, serialNumber)) {
            throw new IllegalArgumentException("serialNumber " + serialNumber + " already exists");
        }
        String assetTag = col(cols, 7);
        if (assetTag != null && assetRepository.existsByCompanyIdAndAssetTag(companyId, assetTag)) {
            throw new IllegalArgumentException("assetTag " + assetTag + " already exists");
        }

        Asset asset = new Asset();
        asset.setName(name);
        asset.setCategory(col(cols, 1));
        asset.setSerialNumber(col(cols, 2));
        asset.setNotes(col(cols, 3));
        asset.setPurchaseDate(parseDate(col(cols, 4), "purchaseDate"));
        asset.setPurchasePrice(parseBigDecimal(col(cols, 5), "purchaseCost"));

        Company company = new Company();
        company.setId(companyId);
        asset.setCompany(company);
        asset.setStatus(AssetStatus.AVAILABLE);

        String employeeNumber = col(cols, 6);
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            Employee employee = employeeRepository.findByCompanyIdAndEmployeeNumber(companyId, employeeNumber)
                .orElseThrow(() -> new IllegalArgumentException("employeeNumber " + employeeNumber + " not found"));
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

    private BigDecimal parseBigDecimal(String value, String fieldName) {
        if (value == null) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a number: " + value);
        }
    }

    private List<String[]> parseCsv(String content) {
        List<String[]> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
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
                    currentRow.add(current.toString());
                    current.setLength(0);
                } else if (c == '\n') {
                    currentRow.add(current.toString());
                    current.setLength(0);
                    if (!currentRow.isEmpty() && !(currentRow.size() == 1 && currentRow.get(0).isBlank())) {
                        rows.add(currentRow.toArray(new String[0]));
                    }
                    currentRow = new ArrayList<>();
                } else if (c == '\r') {
                    // ignore carriage return
                } else {
                    current.append(c);
                }
            }
        }
        // last field
        currentRow.add(current.toString());
        if (!currentRow.isEmpty() && !(currentRow.size() == 1 && currentRow.get(0).isBlank())) {
            rows.add(currentRow.toArray(new String[0]));
        }
        return rows;
    }
}
