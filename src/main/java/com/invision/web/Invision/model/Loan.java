package com.invision.web.Invision.model;

import com.invision.web.Invision.enums.AssetLoanStatus;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.LoanPeriod;
import com.invision.web.Invision.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name="loan")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @NotNull
    private LocalDateTime requestDate;

    @NotNull
    private String description;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @Column(name ="checkout_date")
    private LocalDateTime checkoutDate;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private LoanPeriod loanPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_department")
    private Department userDepartment;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_loan_status")
    private AssetLoanStatus assetLoanStatus;

    @Column(name="due_date")
    private LocalDateTime dueDate;

    @Column(name="return_date")
    private LocalDateTime returnDate;

    public boolean isOverdue(){
        return dueDate != null
                && dueDate.isBefore(LocalDateTime.now())
                && status != LoanStatus.RETURNED;
    }

}
