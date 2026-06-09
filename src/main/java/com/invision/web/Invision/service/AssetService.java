package com.invision.web.Invision.service;

import com.invision.web.Invision.enums.EntityType;
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

    public AssetResponseDTO addAsset(AssetRequestDTO assetRequestDTO, Long userId){
        Asset asset = assetMapper.AssetRequestDTOToAsset(assetRequestDTO);
        assetRepository.save(asset);

        auditLogService.logCreate(userId, EntityType.ASSET, asset.getAssetId(), "Title: " + asset.getTitle() + " | S/N: " + asset.getSerialNumber());

        return assetMapper.AssetToAssetResponseDTO(asset);
    }

    public String updateAsset(Long assetId, AssetRequestDTO assetDetails, Long userId){
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset Is Not Found: " + assetId));

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

        auditLogService.logUpdate(userId, EntityType.ASSET, assetId, oldDetails, newDetails);

        return "Asset updated.";
    }

    public void deleteAsset(Long assetId, Long userId){
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset Is Not Found: " + assetId));

        String snapshot = "Title: " + asset.getTitle() + " | S/N: " + asset.getSerialNumber();

        assetRepository.deleteById(assetId);

        auditLogService.logDelete(userId, EntityType.ASSET, assetId, snapshot);
    }

    public List<AssetResponseDTO> getAllAssets() {
        List<Asset> assets = assetRepository.findAll();
        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

    public AssetResponseDTO getAssetById(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found with ID: " + assetId));
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

    // UPDATED METHOD - Main search and filter logic
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

            if (!errors.isEmpty()) {
                throw new RuntimeException("Bulk import failed with " + errors.size() + " error(s):\n" + String.join("\n", errors));
            }

            if (!assets.isEmpty()) {
                assetRepository.saveAll(assets);
                auditLogService.logCreate(userId, EntityType.ASSET, null, "Bulk imported " + assets.size() + " assets via CSV.");
            } else {
                throw new RuntimeException("No valid assets to import");
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
