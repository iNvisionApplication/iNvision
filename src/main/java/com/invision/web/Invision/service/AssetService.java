package com.invision.web.Invision.service;

import com.invision.web.Invision.Repository.AssetRepository;
import com.invision.web.Invision.dto.AssetRequestDTO;
import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Category;
import com.invision.web.Invision.model.AssetStatus;
import com.invision.web.Invision.model.Condition;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class AssetService {
    @Autowired
    private AssetRepository assetRepository;

    // ADD ASSET using DTOs
    public AssetResponseDTO addAsset(AssetRequestDTO assetRequestDTO){

        //Convert DTO to entity
      Asset asset = new Asset();
      asset.setTitle(assetRequestDTO.title());
      asset.setCategory(Category.valueOf(assetRequestDTO.category())); // Converted string to enum for @jakarta.validation.constraint
      asset.setSerialNumber(assetRequestDTO.serialNumber());
      asset.setAcquisitionDate(assetRequestDTO.acquisitionDate());
      asset.setCost(BigDecimal.valueOf((assetRequestDTO.cost())));    //Converted Double to BigDecimal
      asset.setLocation(assetRequestDTO.location());
      asset.setCondition(assetRequestDTO.condition());
      asset.setPhotoPath(assetRequestDTO.path());
      asset.setStatus(AssetStatus.AVAILABLE);

      // Save entity
        Asset savedAsset = assetRepository.save(asset);

        // Converting entity to ResponseDTO
        return new AssetResponseDTO(
                savedAsset.getTitle(),
                savedAsset.getCategory(),
                savedAsset.getSerialNumber(),
                savedAsset.getAcquisitionDate(),
                savedAsset.getCost(),
                savedAsset.getLocation(),
                savedAsset.getCondition(),
                savedAsset.getPhotoPath(),
                savedAsset.getStatus()
        );
    }

    // UPDATE ASSET using DTOs
    public AssetResponseDTO updateAsset(Long assetId, AssetRequestDTO assetDetails){
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset Is Not Found: " +assetId));

        // Update entity fields from DTOs
        asset.setTitle(assetDetails.title());
        asset.setCategory(Category.valueOf(assetDetails.category()));
        asset.setSerialNumber(assetDetails.serialNumber());
        asset.setAcquisitionDate(LocalDateTime.parse(String.valueOf(assetDetails.acquisitionDate())));
        asset.setCost(BigDecimal.valueOf(assetDetails.cost()));
        asset.setLocation(assetDetails.location());
        asset.setCondition(Condition.valueOf(String.valueOf(assetDetails.condition())));
        asset.setPhotoPath(assetDetails.path());

        Asset updatedAsset = assetRepository.save(asset);

        return new AssetResponseDTO(
                updatedAsset.getTitle(),
                updatedAsset.getCategory(),
                updatedAsset.getSerialNumber(),
                updatedAsset.getAcquisitionDate(),
                updatedAsset.getCost(),
                updatedAsset.getLocation(),
                updatedAsset.getCondition(),
                updatedAsset.getPhotoPath(),
                updatedAsset.getStatus()
        );
    }

    // DELETE ASSET
    public void deleteAsset(Long assetId){
        assetRepository.deleteById(assetId);
    }
}
