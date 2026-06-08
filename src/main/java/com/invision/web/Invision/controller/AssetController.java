package com.invision.web.Invision.controller;

import com.invision.web.Invision.dto.AssetRequestDTO;
import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.dto.AssetSearchRequest;
import com.invision.web.Invision.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
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

    // Search assets by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<AssetResponseDTO>> getAssetsByCategory(@PathVariable String category) {
        List<AssetResponseDTO> assets = assetService.getAssetsByCategory(category);
        return ResponseEntity.ok(assets);
    }

    // NEW: Advanced search and filter with query parameters (GET request)
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

    // Advanced search and filter with POST body
    @PostMapping("/search")
    public ResponseEntity<List<AssetResponseDTO>> searchAndFilterAssetsPost(
            @RequestBody AssetSearchRequest searchRequest) {

        List<AssetResponseDTO> assets = assetService.searchAndFilterAssets(searchRequest);
        return ResponseEntity.ok(assets);
    }

    // Create a new asset
    @PostMapping
    public ResponseEntity<AssetResponseDTO> createAsset(@RequestBody AssetRequestDTO assetRequestDTO) {
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
    public ResponseEntity<String> deleteAsset(@PathVariable Long assetId) {
        assetService.deleteAsset(assetId);
        return ResponseEntity.ok("Asset deleted successfully with ID: " + assetId);
    }
}