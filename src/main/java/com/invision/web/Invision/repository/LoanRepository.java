package com.invision.web.Invision.repository;

import com.invision.web.Invision.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan,Long> {
    List<Loan> findByAssetAssetId(Long assetId);
    List<Loan> findByUserUserId(Long userID);
}
