package com.invision.web.Invision.tool;

import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.dto.AssetSearchRequest;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.enums.*;
import com.invision.web.Invision.service.AssetService;
import com.invision.web.Invision.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InvisionTools {

    private final AssetService assetService;
    private final LoanService loanService;

    @Tool(description = """
                        Searches the asset inventory.
                        All parameters are optional.
                        Use any combination of:
                            - title (partial asset name or keyword)
                            - category (AUDIO, CAMERA, LAPTOP)
                            - location (IT_STOREROOM, VIDEO_LOCKER, PRODUCTION_STUDIO)
                            - status (AVAILABLE, LOANED, RETIRED)

                        If the user provides only one field, search using only that field.
                        If the user provides multiple fields, combine them.
                        If a field is not mentioned, leave it null so it is not used as a filter.
                        """)
    public List<AssetResponseDTO> searchAssets(
            @ToolParam(description = "Optional. Asset title or keyword.") String title,
            @ToolParam(description = "Optional. Asset category.") String category,
            @ToolParam(description = "Optional. Asset location.") String location,
            @ToolParam(description = "Optional. Asset availability status.") String status
    ){
        AssetSearchRequest request = new AssetSearchRequest(
                title,
                parseCategory(category),
                parseStatus(status),
                parseLocation(location),
                null
        );

            return assetService.searchAndFilterAssets(request);
    }

    @Tool(description = "Get the current user's loans. Call this when the user asks about " +
            "their loan history, active loans, or the status of a specific loan request.")
    public List<LoanResponseDTO> getUserLoans(
           @ToolParam(description = "Filter by status: PENDING, APPROVED, RETURNED, REJECTED"+
           "Leave empty to get all loans.") String status){
        if(status != null){
            return loanService.getUserLoansByStatus(LoanStatus.valueOf(status));
        }
        return loanService.getUserLoans();
    }

    @Tool(description = "Submit a loan request for an asset. Only call this after confirming " +
            "the asset name and loan period with the user. Never call without explicit user confirmation.")
    public LoanResponseDTO submitLoanRequest(
           @ToolParam(description = "The ID of the asset to request") Long assetId,
           @ToolParam(description = "Loan period: ONE_WEEK,TWO_WEEKS,THREE_WEEKS, FOUR_WEEKS")String loanPeriod,
           @ToolParam(description = "Reason for the loan") String description
    ){
        LoanRequestDTO requestDTO = new LoanRequestDTO(assetId,description,LoanPeriod.valueOf(loanPeriod));
        return loanService.requestLoan(requestDTO);
    }

    @Tool(description = "Get loans pending approval. For MANAGER role only."+
            "Call this when a manager asks about pending requests or approval queue.")
    public List<LoanResponseDTO> getDepartmentPendingApprovals(){

        return loanService.getDepartmentLoansByStatus(LoanStatus.PENDING);
    }

    private Category parseCategory(String category) {
        if (category == null) return null;

        return switch (category.trim().toLowerCase()) {
            case "camera", "cameras" -> Category.CAMERA;
            case "laptop", "laptops" -> Category.LAPTOP;
            case "audio", "microphone", "microphones" -> Category.AUDIO;
            default -> null;
        };
    }

    private Location parseLocation(String location) {
        if (location == null) return null;

        return switch (location.trim().toLowerCase()) {
            case "it storeroom", "it room" -> Location.IT_STOREROOM;
            case "video locker" -> Location.VIDEO_LOCKER;
            case "production studio", "studio" -> Location.PRODUCTION_STUDIO;
            default -> null;
        };
    }

    private AssetStatus parseStatus(String status) {
        if (status == null) return null;

        return switch (status.trim().toLowerCase()) {
            case "available" -> AssetStatus.AVAILABLE;
            case "loaned", "borrowed" -> AssetStatus.LOANED;
            case "retired" -> AssetStatus.RETIRED;
            default -> null;
        };
    }





}
