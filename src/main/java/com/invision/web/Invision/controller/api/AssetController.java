package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.dto.AssetRequestDTO;
import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.dto.AssetSearchRequest;
import com.invision.web.Invision.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/assets")  //added a package for apis
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    // Get all assets
    @GetMapping
    public ResponseEntity<List<AssetResponseDTO>> getAllAssets() {
        List<AssetResponseDTO> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    // Get asset by ID
    @GetMapping("/{assetId}")
    public ResponseEntity<AssetResponseDTO> getAssetById(@PathVariable Long assetId) {
        AssetResponseDTO asset = assetService.getAssetById(assetId);
        return ResponseEntity.ok(asset);
    }

    // Advanced search and filter with query parameters (GET request)
    @GetMapping("/search")
    public ResponseEntity<List<AssetResponseDTO>> searchAndFilterAssets(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String condition) {

        List<AssetResponseDTO> assets = assetService.searchAndFilterAssets(
                title, category, status, location, condition
        );
        return ResponseEntity.ok(assets);
    }

    @PostMapping
    public ResponseEntity<AssetResponseDTO> createAsset(
            @RequestBody AssetRequestDTO assetRequestDTO) {

        AssetResponseDTO createdAsset = assetService.addAsset(assetRequestDTO);
        return new ResponseEntity<>(createdAsset, HttpStatus.CREATED);
    }

    // Update an existing asset
    @PutMapping("/{assetId}")
    public ResponseEntity<String> updateAsset(
            @PathVariable Long assetId,
            @RequestBody AssetRequestDTO assetDetails) {

        String result = assetService.updateAsset(assetId, assetDetails);
        return ResponseEntity.ok(result);
    }

    // Delete an asset
    @DeleteMapping("/{assetId}")
    public ResponseEntity<String> deleteAsset(
            @PathVariable Long assetId) {

        assetService.deleteAsset(assetId);
        return ResponseEntity.ok("Asset deleted successfully with ID: " + assetId);
    }

    // Bulk Import CSV
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCSV(
            @RequestPart("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            assetService.bulkImportAssets(file, userId);
            return ResponseEntity.ok("CSV imported successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to process CSV: " + e.getMessage());
        }
    }
}
