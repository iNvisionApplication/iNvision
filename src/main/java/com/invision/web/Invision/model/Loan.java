package com.invision.web.Invision.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long loanId;

    @OneToOne
    private long assetId;

    @ManyToOne
    private long userId;

    private LocalDateTime requestDate;

    private Status status;

    private LocalDateTime checkoutDate;

    private LocalDateTime dueDate;

    private LocalDateTime returnDate;
}
