package com.invision.web.Invision.repository;

import com.invision.web.Invision.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan,Long> {
}
