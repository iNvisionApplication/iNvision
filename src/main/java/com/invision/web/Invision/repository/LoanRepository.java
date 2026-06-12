package com.invision.web.Invision.repository;

import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan,Long> {
    List<Loan> findByAssetAssetId(Long assetId);

    List<Loan> findByUserUserId(Long userID);

    List<Loan> findByUserUserIdAndStatus(Long userId, LoanStatus status);

    List<Loan> findByStatus(LoanStatus status);

    boolean existsByUserUserIdAndAssetAssetIdAndStatusIn(Long userId, Long assetId, List<LoanStatus> statuses);

    List<Loan> findByDueDateBeforeAndStatusNot(LocalDateTime now, LoanStatus status);

    List<Loan> findByDueDateBeforeAndStatusNotAndUserDepartment(
            LocalDateTime now, LoanStatus status, Department department);

    List<Loan> findByDueDateBeforeAndStatusNotAndUserUserId(LocalDateTime now, LoanStatus status, Long userId);

    int countByUserUserIdAndStatus(Long userId, LoanStatus status);

    long countByStatus(LoanStatus status);
    List<Loan> findTop5ByOrderByRequestDateDesc();
}