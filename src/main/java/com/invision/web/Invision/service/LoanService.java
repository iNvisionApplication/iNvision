package com.invision.web.Invision.service;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.dto.LoanStatusDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.enums.*;
import com.invision.web.Invision.event.LoanRequestEvent;
import com.invision.web.Invision.exception.asset.AssetNotFoundException;
import com.invision.web.Invision.exception.loan.*;
import com.invision.web.Invision.exception.user.UserNotFoundException;
import com.invision.web.Invision.mapper.LoanMapper;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import com.invision.web.Invision.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;


    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public List<LoanResponseDTO> getAllOverdueLoans(){
        return loanRepository.findByDueDateBeforeAndStatus(LocalDateTime.now(), LoanStatus.APPROVED)
                .stream()
                .map(loanMapper::loanToLoanResponseDTO)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public List<LoanResponseDTO> getOverdueLoansByDepartment(Department department) {
        List<Loan> loans = loanRepository.findByDueDateBeforeAndStatusNotAndUserDepartment(
                LocalDateTime.now(), LoanStatus.RETURNED, department);

        if (loans.isEmpty()) {
            throw new NoLoansFoundException("No overdue loans found for department: " + department);
        }

        return loans.stream()
                .map(loanMapper::loanToLoanResponseDTO)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public List<LoanResponseDTO> getUserOverdueLoans(Long userId){
        // FIX 1: Added negation operator so it doesn't reject valid profiles
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("This user doesn't exist");
        }

        List<Loan> loans = loanRepository.findByDueDateBeforeAndStatusNotAndUserUserId(LocalDateTime.now(), LoanStatus.RETURNED, userId);
        if(loans.isEmpty()){
            throw new NoLoansFoundException("This user has no overdue loans");
        }

        return loans.stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    @PreAuthorize("hasRole('ROLE_BORROWER')")
    public List<LoanResponseDTO> getCurrentUserOverdueLoans(){
        User user = getAuthenticatedUser();

        List<Loan> loans = loanRepository.findByDueDateBeforeAndStatusNotAndUserUserId(LocalDateTime.now(), LoanStatus.RETURNED, user.getUserId());
        if(loans.isEmpty()){
            throw new NoLoansFoundException("You have no overdue loans");
        }

        return loans.stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public List<LoanResponseDTO> getLoansByAsset(Long assetId){
        List<Loan> loans = loanRepository.findByAssetAssetId(assetId);

        if(loans.isEmpty()){
            throw new NoLoansFoundException("This asset has no loan history");
        }

        return loans.stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getUserLoans(){
        User user = getAuthenticatedUser();
        List<Loan> loans = loanRepository.findByUserUserId(user.getUserId());

        if(loans.isEmpty()){
            throw new NoLoansFoundException("This user has no loan history");
        }

        return loans.stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    @PreAuthorize("hasAnyRole('ROLE_MANAGER')") //only Managers can update loanStatus
    @Transactional
    public LoanResponseDTO updateLoanStatus(Long loanId, LoanStatusDTO actionDTO) throws BadRequestException {
        User manager = getAuthenticatedUser();
        String managerEmail = manager.getEmail();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        if (manager.getDepartment() != loan.getUser().getDepartment()) {
            throw new InvalidLoanStatusChangeException(
                    "Managers can only approve or reject loans from their own department"
            );
        }

        if (loan.getDescription() == null) {
            loan.setDescription("No description provided");
        }

        LoanStatus oldStatus = loan.getStatus();
        LoanStatus newStatus = actionDTO.loanStatus();

        if(oldStatus == LoanStatus.RETURNED || oldStatus == LoanStatus.REJECTED){
            throw new InvalidLoanStatusChangeException("Cannot change status of closed loan");
        }

        Asset asset = loan.getAsset();
        String assetInfo = "Asset ID: " + (asset != null ? asset.getAssetId() : "N/A");

        // FIX 2: Consolidated logic path updates to remove duplicate code blocks fighting each other
        // fix 3: Fixing the due date reflection from back to front
        if (newStatus == LoanStatus.APPROVED) {

            assert asset != null;
            if(asset.getStatus() == AssetStatus.LOANED)
                throw new BadRequestException("This asset is already been loaned out.");

            loan.setStatus(newStatus);
            loan.setAssetLoanStatus(AssetLoanStatus.PENDING_COLLECTION);

            if (asset != null) {
                asset.setStatus(AssetStatus.LOANED);
                assetRepository.save(asset);
            }

            User user = loan.getUser();
            notificationService.sendAll(user.getUserId(), user.getEmail(),
                    NotificationReason.LOAN_STATUS_UPDATED,
                    "Your loan for " + (asset != null ? asset.getTitle() : "Asset") + " was approved by " + managerEmail + ".");

            auditLogService.logCheckOut(manager.getUserId(), loanId, assetInfo);

        } else if (newStatus == LoanStatus.RETURNED) {
            loan.setReturnDate(LocalDateTime.now());
            loan.setAssetLoanStatus(AssetLoanStatus.RETURN_CONFIRMED);
            loan.setStatus(newStatus);

            if (asset != null) {
                asset.setStatus(AssetStatus.AVAILABLE);
                assetRepository.save(asset);
            }

            auditLogService.logCheckIn(manager.getUserId(), loanId, assetInfo);

        } else if (newStatus == LoanStatus.REJECTED) {
            loan.setAssetLoanStatus(AssetLoanStatus.LOAN_REJECTED);
            loan.setStatus(newStatus);
            User user = loan.getUser();
            notificationService.sendAll(user.getUserId(), user.getEmail(),
                    NotificationReason.LOAN_STATUS_UPDATED,
                    "Your loan for " + (asset != null ? asset.getTitle() : "Asset") + " was rejected by " + managerEmail + ".");

            auditLogService.logUpdate(manager.getUserId(), EntityType.LOAN, loanId, "Status: " + oldStatus, "Status: " + newStatus);
        } else {
            auditLogService.logUpdate(manager.getUserId(), EntityType.LOAN, loanId, "Status: " + oldStatus, "Status: " + newStatus);
        }

        Loan saved = loanRepository.saveAndFlush(loan);
        return loanMapper.loanToLoanResponseDTO(saved);
    }

    @PreAuthorize("hasRole ('ROLE_MANAGER')")
    public List<LoanResponseDTO> getDepartmentLoansByStatus(LoanStatus status){
        User manager = getAuthenticatedUser();
        return loanRepository.findByUserDepartmentAndStatus(manager.getDepartment(),status).stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    @PreAuthorize("hasRole('ROLE_BORROWER')")
    @Transactional
    public LoanResponseDTO requestLoan(LoanRequestDTO requestDTO){
        User requester = getAuthenticatedUser();
        Asset asset = assetRepository.findById(requestDTO.assetId()).orElseThrow(
                () -> new AssetNotFoundException("This asset does not exist")
        );

        if(loanRepository.countByUserUserIdAndStatus(requester.getUserId(), LoanStatus.APPROVED) > 5){
            throw new ExceededLoanRequestException("User had too many active loans");
        }

        if (loanRepository.existsByUserUserIdAndAssetAssetIdAndStatusIn(requester.getUserId(), requestDTO.assetId(), List.of(LoanStatus.APPROVED, LoanStatus.PENDING))) {
            throw new BadLoanRequest("User has already has an active loan for this asset");
        }

        if(asset.getStatus() == AssetStatus.RETIRED){
            throw new BadLoanRequest("This asset is retired and cannot be loaned");
        }

        List<User> managers = userRepository.findByDepartmentAndRole(requester.getDepartment(), Role.MANAGER);
        if (managers.isEmpty()) {
            throw new BadLoanRequest("No managers found for department: " + requester.getDepartment());
        }

        List<User> copyOfManagers = new ArrayList<>(managers);
        Collections.shuffle(copyOfManagers);

        // FIX 3: Safe execution limits prevent IndexOutOfBoundsException if department only holds 1 manager
        notificationService.sendAll(copyOfManagers.get(0).getUserId(), copyOfManagers.get(0).getEmail(),
                NotificationReason.LOAN_REQUEST,
                "Loan for " + asset.getTitle() + " was requested by " + requester.getEmail() + ".");

        if (copyOfManagers.size() > 1) {
            notificationService.sendAll(copyOfManagers.get(1).getUserId(), copyOfManagers.get(1).getEmail(),
                    NotificationReason.LOAN_REQUEST,
                    "Loan for " + asset.getTitle() + " was requested by " + requester.getEmail() + ".");
        }
        LoanPeriod period;
        long daysBetween = ChronoUnit.DAYS.between(requestDTO.checkoutDate(),requestDTO.dueDate());
        int weeks = (int) Math.ceil( ((double) daysBetween/7));
        switch (weeks){
            case 1:period = LoanPeriod.ONE_WEEK;
            break;
            case 2:period = LoanPeriod.TWO_WEEKS;
            break;
            case 3:period = LoanPeriod.THREE_WEEKS;
            break;
            case 4:period = LoanPeriod.FOUR_WEEKS;
            break;
            default:
                throw new BadLoanRequest("Loan period can not excide four weeks");

        }

        Loan loan = Loan.builder()
                .asset(asset)
                .user(requester)
                .requestDate(LocalDateTime.now())
                .checkoutDate(requestDTO.checkoutDate())
                .dueDate(requestDTO.checkoutDate().plusDays(period.getDays()))
                .status(LoanStatus.PENDING)
                .assetLoanStatus(AssetLoanStatus.PENDING_APPROVAL)
                .userDepartment(requester.getDepartment())
                .description(requestDTO.description())
                .loanPeriod(period)
                .build();

        eventPublisher.publishEvent(new LoanRequestEvent(
                requester.getUserId(),copyOfManagers.get(0).getEmail(), copyOfManagers.size() > 1 ? copyOfManagers.get(1).getEmail():null,asset.getTitle(),requester.getEmail()
        ));

        Loan savedLoan = loanRepository.save(loan);

        // Safe to call logging frameworks now that database assignment populated structural keys
        auditLogService.logCreate(requester.getUserId(), EntityType.LOAN, savedLoan.getLoanId(), "Loan requested for Asset ID: " + requestDTO.assetId());

        return loanMapper.loanToLoanResponseDTO(savedLoan);
    }

    public Page<LoanResponseDTO> getAllLoansByStatus(LoanStatus status, int page, int size) {
        User currentUser = getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").descending());

        Page<Loan> loans;

        if (currentUser.getRole() == Role.MANAGER) {
            loans = loanRepository.findByStatusAndUserDepartment(
                    status,
                    currentUser.getDepartment(),
                    pageable
            );
        } else {
            loans = loanRepository.findByStatus(status, pageable);
        }

        return loanRepository.findByStatus(status, pageable)
                .map(loanMapper::loanToLoanResponseDTO);
    }

    public List<LoanResponseDTO> getAllLoans(){
        return loanRepository.findAll().stream()
                .map(loanMapper::loanToLoanResponseDTO).toList();
    }

    @Transactional
    public List<LoanResponseDTO> getUserLoansByStatus( LoanStatus status){
        User user = getAuthenticatedUser();

        List<LoanResponseDTO> loans = loanRepository.findByUserUserIdAndStatus(user.getUserId(), status).stream()
                .map(loanMapper::loanToLoanResponseDTO).toList();

        if(loans.isEmpty()){
            throw new NoLoansFoundException("User does not have any " + status.toString() + " loans");
        }
        return loans;
    }

    @PreAuthorize("hasRole('ROLE_BORROWER')")
    @Transactional
    public LoanResponseDTO loanActionReturn(Long loanId){
        Loan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new NoLoansFoundException("This loan does not exist")
        );
        loan.setAssetLoanStatus(AssetLoanStatus.PENDING_RETURN_CONFIRMATION);

        Loan saved = loanRepository.save(loan);
        return loanMapper.loanToLoanResponseDTO(saved);
    }

    @PreAuthorize("hasRole('ROLE_BORROWER')")
    @Transactional
    public LoanResponseDTO loanActionCollect(Long loanId){
        Loan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new NoLoansFoundException("This loan does not exist")
        );
        loan.setAssetLoanStatus(AssetLoanStatus.COLLECTED);
        return loanMapper.loanToLoanResponseDTO(loanRepository.save(loan));
    }

    public Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    private User getAuthenticatedUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    // Inside LoanService.java

    public Page<LoanResponseDTO> getAllLoans(int page, int size) {
        User currentUser = getAuthenticatedUser();
        // Sort transactions so the newest requests appear at the top of the admin list
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").descending());

        Page<Loan> loans;

        if (currentUser.getRole() == Role.MANAGER) {
            loans = loanRepository.findByUserDepartment(
                    currentUser.getDepartment(),
                    pageable
            );
        } else {
            loans = loanRepository.findAll(pageable);
        }

        return loanRepository.findAll(pageable)
                .map(loanMapper::loanToLoanResponseDTO);
    }

    public Page<LoanResponseDTO> getUserLoans(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").descending());

        Page<Loan> loans = loanRepository.findByUserUserId(userId, pageable);

        if (loans.isEmpty()) {
            throw new NoLoansFoundException("This user has no loan history");
        }

        return loans.map(loanMapper::loanToLoanResponseDTO);
    }
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public org.springframework.data.domain.Page<LoanResponseDTO> getDepartmentLoans(int page, int size) {
        User manager = getAuthenticatedUser();

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("requestDate").descending());

        return loanRepository.findByUserDepartment(manager.getDepartment(), pageable)
                .map(loanMapper::loanToLoanResponseDTO);
    }

    // Add this endpoint method inside LoanService.java
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @Transactional
    public LoanResponseDTO confirmLoanReturn(Long loanId) {
        User manager = getAuthenticatedUser();
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan record not found"));

        loan.setStatus(LoanStatus.RETURNED);
        loan.setAssetLoanStatus(AssetLoanStatus.RETURN_CONFIRMED);
        loan.setReturnDate(LocalDateTime.now());

        Asset asset = loan.getAsset();
        if (asset != null) {
            asset.setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(asset);
        }

        String assetInfo = "Asset ID: " + (asset != null ? asset.getAssetId() : "N/A");
        auditLogService.logCheckIn(manager.getUserId(), loanId, assetInfo);

        return loanMapper.loanToLoanResponseDTO(loanRepository.save(loan));
    }

    public void checkAssetCollected(User user) {
        System.out.println(user.getName());
        if (user.getRole() == Role.BORROWER) {
            List<Loan> loans = loanRepository.findByUserUserIdAndAssetLoanStatusInAndCheckoutDateBefore(
                    user.getUserId(),
                    List.of(AssetLoanStatus.PENDING_APPROVAL, AssetLoanStatus.PENDING_COLLECTION),
                    LocalDateTime.now()
            );

            if (!loans.isEmpty()) {
                for (Loan loan : loans) {
                    loan.setAssetLoanStatus(AssetLoanStatus.LOAN_REJECTED);
                    loan.setStatus(LoanStatus.REJECTED);

                    Asset asset = loan.getAsset();
                    asset.setStatus(AssetStatus.AVAILABLE);
                    assetRepository.save(asset);
                    notificationService.sendSystemNotification(
                            user.getUserId(),
                            NotificationReason.LOAN_STATUS_UPDATED,
                            "Your loan for " + (asset != null ? asset.getTitle() : "Asset") + " was rejected because the collection period has passed."
                    );
                }

                loanRepository.saveAll(loans);
            }
        }
    }

}
