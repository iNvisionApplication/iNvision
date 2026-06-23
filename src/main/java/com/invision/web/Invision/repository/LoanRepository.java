package com.invision.web.Invision.repository;

import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findByAssetAssetId(Long assetId);

    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findByUserUserId(Long userID);

    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findByUserUserIdAndStatus(Long userId, LoanStatus status);

    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findByStatus(LoanStatus status);

    boolean existsByUserUserIdAndAssetAssetIdAndStatusIn(
            Long userId,
            Long assetId,
            List<LoanStatus> statuses
    );

    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findByDueDateBeforeAndStatusNot(LocalDateTime now, LoanStatus status);

    // Method to return only approved OVERDUE loans
    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findByDueDateBeforeAndStatus(
            LocalDateTime now,
            LoanStatus status
    );

    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findByDueDateBeforeAndStatusNotAndUserDepartment(
            LocalDateTime now,
            LoanStatus status,
            Department department
    );

    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findByDueDateBeforeAndStatusNotAndUserUserId(
            LocalDateTime now,
            LoanStatus status,
            Long userId
    );

    int countByUserUserIdAndStatus(Long userId, LoanStatus status);

    int countByUserUserIdAndStatusAndDueDateBefore(
            Long userId,
            LoanStatus status,
            LocalDateTime now
    );

    long countByStatus(LoanStatus status);

    long countByStatusAndDueDateBefore(
            LoanStatus status,
            LocalDateTime now
    );

    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findTop5ByOrderByRequestDateDesc();

    @EntityGraph(attributePaths = {"asset", "user"})
    Page<Loan> findByUserUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"asset", "user"})
    Page<Loan> findByUserDepartment(
            Department department,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"asset", "user"})
    Page<Loan> findByStatusAndUserDepartment(
            LoanStatus status,
            Department department,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"asset", "user"})
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"asset", "user"})
    List<Loan> findTop5ByUserUserIdOrderByRequestDateDesc(Long userId);

    // Overriding the default findAll to clear the N+1 problem on global admin views
    @Override
    @EntityGraph(attributePaths = {"asset", "user"})
    Page<Loan> findAll(Pageable pageable);
}