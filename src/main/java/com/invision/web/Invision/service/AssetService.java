package com.invision.web.Invision.service;

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

    // ADD ASSET using DTOs
    public AssetResponseDTO addAsset(AssetRequestDTO assetRequestDTO){

        Asset asset = assetMapper.AssetRequestDTOToAsset(assetRequestDTO);
        assetRepository.save(asset);
        assetMapper.AssetToAssetResponseDTO(asset);

        return assetMapper.AssetToAssetResponseDTO(asset);
    }

    public String updateAsset(Long assetId, AssetRequestDTO assetDetails){
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset Is Not Found: " +assetId));

        Asset updatedAsset = assetMapper.AssetRequestDTOToAsset(assetDetails);

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

        return "Asset updated.";
    }

    public void deleteAsset(Long assetId){
        assetRepository.deleteById(assetId);
    }

    // Get all assets
    public List<AssetResponseDTO> getAllAssets() {
        List<Asset> assets = assetRepository.findAll();
        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

    // Get asset by ID
    public AssetResponseDTO getAssetById(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found with ID: " + assetId));
        return assetMapper.AssetToAssetResponseDTO(asset);
    }

    // Search assets by category - Updated to use the search method
    public List<AssetResponseDTO> getAssetsByCategory(String category) {
        Category categoryEnum = Category.valueOf(category);
        List<Asset> assets = assetRepository.searchAndFilterAssets(null, categoryEnum, null, null, null);
        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

    // Advanced search and filter method using AssetSearchRequest record
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

    // Search and filter with individual parameters (for GET requests)
    public List<AssetResponseDTO> searchAndFilterAssets(
            String title,
            String category,
            String status,
            String location,
            String condition) {

        // Convert string parameters to enums (handle null/empty)
        Category categoryEnum = null;
        if (category != null && !category.isEmpty()) {
            try {
                categoryEnum = Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid category, keep as null
                System.out.println("Invalid category value: " + category);
            }
        }

        AssetStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = AssetStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, keep as null
                System.out.println("Invalid status value: " + status);
            }
        }

        Condition conditionEnum = null;
        if (condition != null && !condition.isEmpty()) {
            try {
                conditionEnum = Condition.valueOf(condition.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid condition, keep as null
                System.out.println("Invalid condition value: " + condition);
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
    public void bulkImportAssets(MultipartFile file) throws Exception {
        // Define the date formatter to match your CSV format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<String> errors = new ArrayList<>();
        List<Asset> assets = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim());

            int rowNumber = 1; // Start counting after header
            for (CSVRecord record : csvParser) {
                rowNumber++;
                try {
                    // Get values from CSV (using exact column names from your CSV)
                    String title = record.get("title");
                    String categoryStr = record.get("category");
                    String serialNumber = record.get("serial_number");
                    String acquisitionDateStr = record.get("acquisition_date");
                    String costStr = record.get("cost");
                    String location = record.get("location");
                    String conditionStr = record.get("condition");
                    String statusStr = record.get("status");
                    String photoPath = record.get("photo_path");

                    // Validate required fields
                    if (title == null || title.trim().isEmpty()) {
                        throw new IllegalArgumentException("Title is required");
                    }
                    if (categoryStr == null || categoryStr.trim().isEmpty()) {
                        throw new IllegalArgumentException("Category is required");
                    }
                    if (location == null || location.trim().isEmpty()) {
                        throw new IllegalArgumentException("Location is required");
                    }
                    if (statusStr == null || statusStr.trim().isEmpty()) {
                        throw new IllegalArgumentException("Status is required");
                    }

                    // Parse acquisition date (optional field)
                    LocalDateTime acquisitionDate = null;
                    if (acquisitionDateStr != null && !acquisitionDateStr.trim().isEmpty()) {
                        try {
                            acquisitionDate = LocalDateTime.parse(acquisitionDateStr.trim(), formatter);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Invalid date format for acquisition_date. Expected: yyyy-MM-dd HH:mm:ss", e);
                        }
                    }

                    // Parse cost (optional field)
                    BigDecimal cost = null;
                    if (costStr != null && !costStr.trim().isEmpty()) {
                        try {
                            cost = new BigDecimal(costStr.trim());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid cost format. Expected a valid number", e);
                        }
                    }

                    // Handle optional serial number (empty string should be null)
                    if (serialNumber != null && serialNumber.trim().isEmpty()) {
                        serialNumber = null;
                    }

                    // Handle optional photo path (empty string should be null)
                    if (photoPath != null && photoPath.trim().isEmpty()) {
                        photoPath = null;
                    }

                    // Build the Asset object
                    Asset asset = Asset.builder()
                            .title(title.trim())
                            .category(Category.valueOf(categoryStr.trim().toUpperCase()))
                            .serialNumber(serialNumber)
                            .acquisitionDate(acquisitionDate)
                            .cost(cost)
                            .location(location.trim())
                            .condition(conditionStr != null && !conditionStr.trim().isEmpty()
                                    ? Condition.valueOf(conditionStr.trim().toUpperCase())
                                    : null)
                            .status(AssetStatus.valueOf(statusStr.trim().toUpperCase()))
                            .photoPath(photoPath)
                            .build();

                    assets.add(asset);

                } catch (IllegalArgumentException e) {
                    String error = String.format("Row %d: %s", rowNumber, e.getMessage());
                    errors.add(error);
                    System.err.println(error);
                } catch (Exception e) {
                    String error = String.format("Row %d: Unexpected error - %s", rowNumber, e.getMessage());
                    errors.add(error);
                    System.err.println(error);
                }
            }

            // If there are errors, throw an exception with details
            if (!errors.isEmpty()) {
                throw new RuntimeException("Bulk import failed with " + errors.size() + " error(s):\n" + String.join("\n", errors));
            }

            // Save all valid assets
            if (!assets.isEmpty()) {
                assetRepository.saveAll(assets);
                System.out.println("Successfully imported " + assets.size() + " assets");
            } else {
                throw new RuntimeException("No valid assets to import");
            }
        } catch (Exception e) {
            System.err.println("Bulk import failed: " + e.getMessage());
            throw e;
        }
    }
}