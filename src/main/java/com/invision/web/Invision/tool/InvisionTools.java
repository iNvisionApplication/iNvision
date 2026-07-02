package com.invision.web.Invision.tool;

import com.invision.web.Invision.dto.*;
import com.invision.web.Invision.enums.*;
import com.invision.web.Invision.service.AssetService;
import com.invision.web.Invision.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
@RequiredArgsConstructor
public class InvisionTools {

    //tools agent calls after prompt

    private final AssetService assetService;
    private final LoanService loanService;
    public record DateTimeRequest(String timezone) {}
    public record DateTimeResponse(
            String isoDateTime,
            String dayOfWeek,
            String date,
            String time,
            String zone
    ) {}


    @Tool(description = """
    Returns the current date and time.

    Always use this tool when answering questions about:
    - today's date
    - current time
    - current day
    - tomorrow
    - yesterday
    - next week
    - scheduling
    - deadlines

    Never assume the current date.
    """)
    public DateTimeResponse currentDateTime() {

        ZonedDateTime now = ZonedDateTime.now();

        return new DateTimeResponse(
                now.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
                now.getDayOfWeek().toString(),
                now.toLocalDate().toString(),
                now.toLocalTime().toString(),
                now.getZone().toString()
        );
    }

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
            @ToolParam(description = "The start date and time of the loan in ISO-8601 format (e.g., '2026-07-06T09:00:00')") String checkoutDateStr,
            @ToolParam(description = "The end date and time of the loan in ISO-8601 format (e.g., '2026-07-13T17:00:00')") String dueDateStr,
            @ToolParam(description = "Reason for the loan") String description
    ){
        LocalDateTime checkoutDate = LocalDateTime.parse(checkoutDateStr);
        LocalDateTime dueDate = LocalDateTime.parse(dueDateStr);
        LoanRequestDTO requestDTO = new LoanRequestDTO(assetId,description,checkoutDate, dueDate);
        return loanService.requestLoan(requestDTO);
    }

    @Tool(description = "Get loans pending approval. For MANAGER role only."+
            "Call this when a manager asks about pending requests or approval queue.")
    public List<LoanResponseDTO> getDepartmentPendingApprovals(){

        return loanService.getDepartmentLoansByStatus(LoanStatus.PENDING);
    }

    @Tool(description = """
        Cancels an existing asset loan request.

        Never use a loan ID remembered from a previous conversation turn.
        Never guess a loan ID.
        If multiple pending loans match the user's request, ask the user to clarify.
        Use this tool when the borrower wants to:
            - cancel a loan request
            - withdraw a loan application
            - no longer borrow an asset
            - cancel a pending loan
            - cancel a loan that is awaiting approval
            - cancel a loan that is awaiting collection

        Required:
            - loanId

        Optional:
            - reason (if the user gives one)

        If the user does not provide a reason, leave it empty and a default cancellation reason will be used.

        Only use this tool for cancelling the user's own loan request. Do not use it for approving, rejecting on behalf of staff, returning assets, or deleting loans.
        """)
    public LoanResponseDTO cancelLoan(
            @ToolParam(description = "The ID of the loan to cancel.") Long loanId,
            @ToolParam(description = "Optional reason for cancelling the loan request.") String reason
    ) {
        LoanRejectionDTO rejectionDTO = new LoanRejectionDTO(loanId, reason);
        return loanService.rejectLoan(rejectionDTO);
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
