package com.invision.web.Invision.service;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.enums.*;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.dto.AssetRequestDTO;
import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.dto.AssetSearchRequest;
import com.invision.web.Invision.mapper.AssetMapper;
import com.invision.web.Invision.model.Asset;

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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;
    private final AuditLogService auditLogService;

    public AssetResponseDTO addAsset(AssetRequestDTO assetRequestDTO){
        Asset asset = assetMapper.AssetRequestDTOToAsset(assetRequestDTO);
        assetRepository.save(asset);

        auditLogService.logCreate(getCurrentUserId(), EntityType.ASSET, asset.getAssetId(), "Title: " + asset.getTitle() + " | S/N: " + asset.getSerialNumber());

        return assetMapper.AssetToAssetResponseDTO(asset);
    }

    public String updateAsset(Long assetId, AssetRequestDTO assetDetails){
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset Is Not Found: " + assetId));

        String oldDetails = "Title: " + asset.getTitle() + " | Status: " + asset.getStatus();

        asset.setTitle(assetDetails.title());
        asset.setCategory(assetDetails.category());
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

    // Retire an Asset
    public void retireAsset(Long assetId){
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset Is Not Found: " + assetId));

        String oldStatus = String.valueOf(asset.getStatus());
        asset.setStatus(AssetStatus.RETIRED);
        assetRepository.save(asset);

        auditLogService.logUpdate(getCurrentUserId(), EntityType.ASSET, assetId,oldStatus, "Status: Retired");
    }

    // Get Available And Loaned Assets
    public List<AssetResponseDTO> getAvailAndLoanedAssets(){
        return assetRepository.searchAndFilterAssets(null, null, null, null, null)
                .stream()
                .filter(asset -> asset.getStatus() != AssetStatus.RETIRED)
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
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

    // Search with AssetSearchRequest object (POST endpoint)
    public List<AssetResponseDTO> searchAndFilterAssets(AssetSearchRequest searchRequest) {
        List<Asset> assets = assetRepository.searchAndFilterAssets(
                searchRequest.title(),
                searchRequest.category(),
                searchRequest.status(),
                searchRequest.location(),
                searchRequest.condition()
        );

        // Apply relevance sorting if searching by title
        if (searchRequest.title() != null && !searchRequest.title().trim().isEmpty()) {
            String searchTerm = searchRequest.title().toLowerCase().trim();
            assets.sort((a1, a2) -> {
                int score1 = calculateRelevance(a1.getTitle(), searchTerm);
                int score2 = calculateRelevance(a2.getTitle(), searchTerm);
                return Integer.compare(score2, score1);
            });
        }

        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

    // Search with String parameters (GET endpoint)
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

        Location locationEnum = null;
        if (location != null && !location.isEmpty()) {
            try {
                locationEnum = Location.valueOf(location.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid location - will return empty results
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
                title, categoryEnum, statusEnum, locationEnum, conditionEnum
        );

        // Apply relevance sorting if searching by title
        if (title != null && !title.trim().isEmpty()) {
            String searchTerm = title.toLowerCase().trim();
            assets.sort((a1, a2) -> {
                int score1 = calculateRelevance(a1.getTitle(), searchTerm);
                int score2 = calculateRelevance(a2.getTitle(), searchTerm);
                return Integer.compare(score2, score1);
            });
        }

        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

    // Calculate relevance score for sorting search results
    private int calculateRelevance(String title, String searchTerm) {
        if (title == null) return 0;
        String lowerTitle = title.toLowerCase();

        if (lowerTitle.equals(searchTerm)) {
            return 100;  // Exact match - highest relevance
        } else if (lowerTitle.startsWith(searchTerm)) {
            return 50;   // Starts with - medium relevance
        } else if (lowerTitle.contains(searchTerm)) {
            return 10;   // Contains - lower relevance
        }
        return 0;
    }

    @Transactional
    public void bulkImportAssets(MultipartFile file) throws Exception {
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
                    String locationStr = record.get("location");
                    String conditionStr = record.get("condition");
                    String statusStr = record.get("status");
                    String photoPath = record.get("photo_path");

                    if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title is required");
                    if (categoryStr == null || categoryStr.trim().isEmpty()) throw new IllegalArgumentException("Category is required");
                    if (locationStr == null || locationStr.trim().isEmpty()) throw new IllegalArgumentException("Location is required");
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

                    // Convert location string to enum
                    Location locationEnum;
                    try {
                        locationEnum = Location.valueOf(locationStr.trim());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid location value: " + locationStr + ". Valid values: " +
                                java.util.Arrays.toString(Location.values()));
                    }

                    Asset asset = Asset.builder()
                            .title(title.trim())
                            .category(Category.valueOf(categoryStr.trim().toUpperCase()))
                            .serialNumber(serialNumber)
                            .acquisitionDate(acquisitionDate)
                            .cost(cost)
                            .location(locationEnum)
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
                Long currentUserId = getCurrentUserId();
                for (Asset a : assets) {
                    String details = "Bulk imported via CSV. Title: " + a.getTitle() + " | S/N: " + (a.getSerialNumber() != null ? a.getSerialNumber() : "N/A");
                    auditLogService.logCreate(currentUserId, EntityType.ASSET, a.getAssetId(), details);
                }
            } else {
                throw new RuntimeException("No valid assets to import");
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
            return userDetails.getId();
        }
        return null;
    }
}