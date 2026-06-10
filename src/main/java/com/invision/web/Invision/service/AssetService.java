package com.invision.web.Invision.service;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.enums.EntityType;
import com.invision.web.Invision.exceptions.asset.BulkImportException;
import com.invision.web.Invision.exceptions.asset.DuplicateSerialNumberException;
import com.invision.web.Invision.exceptions.asset.ResourceNotFoundException;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.dto.AssetRequestDTO;
import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.dto.AssetSearchRequest;
import com.invision.web.Invision.mapper.AssetMapper;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.enums.Category;
import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.Condition;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;
    private final AuditLogService auditLogService;

    // Helper method to check for duplicate serial numbers
    private void checkDuplicateSerialNumber(String serialNumber, Long excludeAssetId) {
        // Skip check if no serial number provided
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return;
        }

        // Look for existing asset with this serial number
        java.util.Optional<Asset> existingAsset = assetRepository.findBySerialNumber(serialNumber);

        if (existingAsset.isPresent()) {
            // If we're updating, allow the same asset to keep its serial number
            if (excludeAssetId == null || !existingAsset.get().getAssetId().equals(excludeAssetId)) {
                throw new DuplicateSerialNumberException(serialNumber);
            }
        }
    }

    public AssetResponseDTO addAsset(AssetRequestDTO assetRequestDTO, Long userId){
        // Check for duplicate serial number BEFORE saving
        checkDuplicateSerialNumber(assetRequestDTO.serialNumber(), null);

        Asset asset = assetMapper.AssetRequestDTOToAsset(assetRequestDTO);
        assetRepository.save(asset);

        auditLogService.logCreate(userId, EntityType.ASSET, asset.getAssetId(), "Title: " + asset.getTitle() + " | S/N: " + asset.getSerialNumber());

        return assetMapper.AssetToAssetResponseDTO(asset);
    }

    public String updateAsset(Long assetId, AssetRequestDTO assetDetails){
        // First check if asset exists
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));

        // Check for duplicate serial number (excluding this asset)
        checkDuplicateSerialNumber(assetDetails.serialNumber(), assetId);

        String oldDetails = "Title: " + asset.getTitle() + " | Status: " + asset.getStatus();

        asset.setTitle(assetDetails.title());
        asset.setCategory(Category.valueOf(assetDetails.category()));
        asset.setSerialNumber(assetDetails.serialNumber());
        asset.setAcquisitionDate(assetDetails.acquisitionDate());
        asset.setCost(BigDecimal.valueOf(assetDetails.cost()));
        asset.setLocation(assetDetails.location());
        asset.setCondition(assetDetails.condition());
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setPhotoPath(assetDetails.path());

        assetRepository.save(asset);

        String newDetails = "Title: " + asset.getTitle() + " | Status: " + asset.getStatus();

        auditLogService.logUpdate(getCurrentUserId(), EntityType.ASSET, assetId, oldDetails, newDetails);

        return "Asset updated.";
    }

    public void deleteAsset(Long assetId){
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));

        String snapshot = "Title: " + asset.getTitle() + " | S/N: " + asset.getSerialNumber();

        assetRepository.deleteById(assetId);

        auditLogService.logDelete(getCurrentUserId(), EntityType.ASSET, assetId, snapshot);
    }

    public List<AssetResponseDTO> getAllAssets() {
        List<Asset> assets = assetRepository.findAll();
        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

    public AssetResponseDTO getAssetById(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));
        return assetMapper.AssetToAssetResponseDTO(asset);
    }

    public List<AssetResponseDTO> getAssetsByCategory(String category) {
        Category categoryEnum = Category.valueOf(category);
        List<Asset> assets = assetRepository.searchAndFilterAssets(null, categoryEnum, null, null, null);
        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AssetResponseDTO> searchAndFilterAssets(AssetSearchRequest searchRequest) {
        List<Asset> assets = assetRepository.searchAndFilterAssets(
                searchRequest.title(),
                searchRequest.category(),
                searchRequest.status(),
                searchRequest.location(),
                searchRequest.condition()
        );
        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AssetResponseDTO> searchAndFilterAssets(
            String title,
            String category,
            String status,
            String location,
            String condition) {

        Category categoryEnum = null;
        if (category != null && !category.isEmpty()) {
            try {
                categoryEnum = Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid category - will return empty results
            }
        }

        AssetStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = AssetStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status - will return empty results
            }
        }

        Condition conditionEnum = null;
        if (condition != null && !condition.isEmpty()) {
            try {
                conditionEnum = Condition.valueOf(condition.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid condition - will return empty results
            }
        }

        List<Asset> assets = assetRepository.searchAndFilterAssets(
                title, categoryEnum, statusEnum, location, conditionEnum
        );
        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void bulkImportAssets(MultipartFile file, Long userId) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<String> errors = new ArrayList<>();
        List<Asset> assets = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim());

            int rowNumber = 1;
            for (CSVRecord record : csvParser) {
                rowNumber++;
                try {
                    String title = record.get("title");
                    String categoryStr = record.get("category");
                    String serialNumber = record.get("serial_number");
                    String acquisitionDateStr = record.get("acquisition_date");
                    String costStr = record.get("cost");
                    String location = record.get("location");
                    String conditionStr = record.get("condition");
                    String statusStr = record.get("status");
                    String photoPath = record.get("photo_path");

                    if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title is required");
                    if (categoryStr == null || categoryStr.trim().isEmpty()) throw new IllegalArgumentException("Category is required");
                    if (location == null || location.trim().isEmpty()) throw new IllegalArgumentException("Location is required");
                    if (statusStr == null || statusStr.trim().isEmpty()) throw new IllegalArgumentException("Status is required");

                    // Check for duplicate serial numbers in CSV
                    if (serialNumber != null && !serialNumber.trim().isEmpty()) {

                        // Check duplicate in current batch
                        final String currentSerial = serialNumber;
                        boolean duplicateInBatch = assets.stream()
                                .anyMatch(a -> currentSerial.equals(a.getSerialNumber()));

                        if (duplicateInBatch) {
                            errors.add(String.format("Row %d: Duplicate serial number '%s' in same CSV",
                                    rowNumber, serialNumber));
                            continue;
                        }

                        // Check duplicate in database
                        if (assetRepository.findBySerialNumber(serialNumber).isPresent()) {
                            errors.add(String.format("Row %d: Serial number '%s' already exists",
                                    rowNumber, serialNumber));
                            continue;
                        }
                    }

                    LocalDateTime acquisitionDate = null;
                    if (acquisitionDateStr != null && !acquisitionDateStr.trim().isEmpty()) {
                        acquisitionDate = LocalDateTime.parse(acquisitionDateStr.trim(), formatter);
                    }

                    BigDecimal cost = null;
                    if (costStr != null && !costStr.trim().isEmpty()) {
                        cost = new BigDecimal(costStr.trim());
                    }

                    if (serialNumber != null && serialNumber.trim().isEmpty()) serialNumber = null;
                    if (photoPath != null && photoPath.trim().isEmpty()) photoPath = null;

                    Asset asset = Asset.builder()
                            .title(title.trim())
                            .category(Category.valueOf(categoryStr.trim().toUpperCase()))
                            .serialNumber(serialNumber)
                            .acquisitionDate(acquisitionDate)
                            .cost(cost)
                            .location(location.trim())
                            .condition(conditionStr != null && !conditionStr.trim().isEmpty() ? Condition.valueOf(conditionStr.trim().toUpperCase()) : null)
                            .status(AssetStatus.valueOf(statusStr.trim().toUpperCase()))
                            .photoPath(photoPath)
                            .build();

                    assets.add(asset);

                } catch (IllegalArgumentException e) {
                    errors.add(String.format("Row %d: %s", rowNumber, e.getMessage()));
                } catch (Exception e) {
                    errors.add(String.format("Row %d: Unexpected error - %s", rowNumber, e.getMessage()));
                }
            }

            // THROW BULK IMPORT EXCEPTION INSTEAD OF RUNTIME EXCEPTION
            if (!errors.isEmpty()) {
                throw new BulkImportException("Bulk import failed with " + errors.size() + " error(s):\n" + String.join("\n", errors));
            }

            if (!assets.isEmpty()) {
                assetRepository.saveAll(assets);
                auditLogService.logCreate(userId, EntityType.ASSET, null, "Bulk imported " + assets.size() + " assets via CSV.");
            } else {
                // THROW BULK IMPORT EXCEPTION INSTEAD OF RUNTIME EXCEPTION
                throw new BulkImportException("No valid assets to import");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public Long getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId(); // Returns your actual logged-in user's database ID
        }
        return null; // System or unauthenticated action
    }
}