package com.invision.web.Invision.model;

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

    private LocalDateTime checkoutDate;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private LoanPeriod loanPeriod;

    private LocalDateTime dueDate;

    private LocalDateTime returnDate;

    public boolean isOverdue(){
        return dueDate != null
                && dueDate.isBefore(LocalDateTime.now())
                && status != LoanStatus.RETURNED;
    }

}
