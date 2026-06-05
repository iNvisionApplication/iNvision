package com.invision.web.Invision.service;

import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.dto.AssetRequestDTO;
import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.mapper.AssetMapper;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Category;
import com.invision.web.Invision.model.AssetStatus;

import jakarta.transaction.Transactional;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

//    @Autowired
//    public AssetService(AssetRepository assetRepository, AssetMapper assetMapper) {
//        this.assetRepository = assetRepository;
//        this.assetMapper = assetMapper;
//    }

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
        asset.setCategory(Category.AUDIO);
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

    // Add these methods to your AssetService class

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

    // Search assets by category
    public List<AssetResponseDTO> getAssetsByCategory(String category) {
        List<Asset> assets = assetRepository.findByCategory(Category.valueOf(category));
        return assets.stream()
                .map(assetMapper::AssetToAssetResponseDTO)
                .collect(Collectors.toList());
    }

}
