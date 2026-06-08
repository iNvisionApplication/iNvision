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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // NEW: Advanced search and filter method using AssetSearchRequest record
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

    // NEW: Search and filter with individual parameters (for GET requests)
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
}