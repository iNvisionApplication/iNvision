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
@RequestMapping("/api/assets")  //added a package for apis
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
            @Valid @RequestBody AssetRequestDTO assetRequestDTO) {

        AssetResponseDTO createdAsset = assetService.addAsset(assetRequestDTO);
        return new ResponseEntity<>(createdAsset, HttpStatus.CREATED);
    }

    // Update an existing asset
    @PutMapping("/{assetId}")
    public ResponseEntity<String> updateAsset(
            @PathVariable Long assetId,
            @Valid @RequestBody AssetRequestDTO assetDetails) {

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
            @RequestPart("file") MultipartFile file) {

        // 1. Guard Clause: Check if the file wrapper is empty
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to process CSV: Uploaded file is empty.");
        }

        // 2. Metadata Check: Extract content type and file name extensions
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        // 3. Strict Extension Validation
        // Checks standard mime types ("text/csv" or "application/vnd.ms-excel" for some OS variants)
        boolean isValidMimeType = contentType != null &&
                (contentType.equalsIgnoreCase("text/csv") ||
                        contentType.equalsIgnoreCase("application/vnd.ms-excel"));

        boolean isValidExtension = filename != null && filename.toLowerCase().endsWith(".csv");

        if (!isValidMimeType && !isValidExtension) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to process CSV: Only standard .csv files are permitted.");
        }

        // 4. File Size Guard Clause (e.g., Reject files larger than 5MB to prevent memory exhaustion)
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
