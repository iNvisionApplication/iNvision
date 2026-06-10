package com.invision.web.Invision.repository;

import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan,Long> {
    List<Loan> findByAssetAssetId(Long assetId);
    List<Loan> findByUserUserId(Long userID);
    List<Loan> findByUserUserIdAndStatus(Long userId, LoanStatus status);
    boolean existsByUserUserIdAndAssetAssetIdAndStatusIn(Long userId, Long assetId, List<LoanStatus> statuses);
}
