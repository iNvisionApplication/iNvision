package com.invision.web.Invision.mapper;

import com.invision.web.Invision.dto.AssetRequestDTO;
import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.Category;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AssetMapper {

    public AssetResponseDTO AssetToAssetResponseDTO(Asset asset){
        return new AssetResponseDTO(asset.getAssetId(), asset.getTitle(), String.valueOf(asset.getCategory()), asset.getSerialNumber(), asset.getAcquisitionDate(),asset.getCost().doubleValue(),
                asset.getLocation(),asset.getCondition(), asset.getPhotoPath(),asset.getStatus());
    }

    public Asset AssetRequestDTOToAsset(AssetRequestDTO requestDTO){
        return Asset.builder().title(requestDTO.title()).category(requestDTO.category())
                .serialNumber(requestDTO.serialNumber()).acquisitionDate(requestDTO.acquisitionDate()).cost(new BigDecimal(requestDTO.cost())).location(requestDTO.location()).condition(requestDTO.condition()).status(AssetStatus.AVAILABLE).photoPath(requestDTO.path()).build();
    }
}
