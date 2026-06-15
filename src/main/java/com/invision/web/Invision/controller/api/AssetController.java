package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.dto.AssetRequestDTO;
import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.dto.AssetSearchRequest;
import com.invision.web.Invision.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // Search and filter assets with query parameters
    @GetMapping("/search")
    public ResponseEntity<List<AssetResponseDTO>> searchAssets(
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

    // Create asset
    @PostMapping
    public ResponseEntity<AssetResponseDTO> createAsset(@Valid @RequestBody AssetRequestDTO assetRequestDTO) {
        AssetResponseDTO createdAsset = assetService.addAsset(assetRequestDTO);
        return new ResponseEntity<>(createdAsset, HttpStatus.CREATED);
    }

    // Update asset
    @PutMapping("/update/{assetId}")
    public ResponseEntity<String> updateAsset(
            @PathVariable Long assetId,
            @Valid @RequestBody AssetRequestDTO assetDetails) {
        String result = assetService.updateAsset(assetId, assetDetails);
        return ResponseEntity.ok(result);
    }

    // Retire an asset
    @PutMapping("/retire/{assetId}")
    public ResponseEntity<String> retireAsset(@PathVariable Long assetId) {
        assetService.retireAsset(assetId);
        return ResponseEntity.ok("Asset retired successfully with ID: " + assetId);
    }

    // Get available and loaned assets
    @GetMapping("/available-loaned")
    public ResponseEntity<List<AssetResponseDTO>> getAvailableAndLoanedAssets() {
        List<AssetResponseDTO> assets = assetService.getAvailAndLoanedAssets();
        return ResponseEntity.ok(assets);
    }

    // Bulk Import CSV
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCSV(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to process CSV: Uploaded file is empty.");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        boolean isValidMimeType = contentType != null &&
                (contentType.equalsIgnoreCase("text/csv") ||
                        contentType.equalsIgnoreCase("application/vnd.ms-excel"));

        boolean isValidExtension = filename != null && filename.toLowerCase().endsWith(".csv");

        if (!isValidMimeType && !isValidExtension) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to process CSV: Only standard .csv files are permitted.");
        }

        long maxBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to process CSV: File size exceeds the maximum allowed limit of 5MB.");
        }

        try {
            assetService.bulkImportAssets(file);
            return ResponseEntity.ok("CSV imported successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to process CSV: " + e.getMessage());
        }
    }
}