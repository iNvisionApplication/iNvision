package com.invision.web.Invision.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Table(name="loan")
@Entity
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @ManyToOne
    @JoinColumn(name ="asset_id")
    private Asset asset;

    @ManyToOne
    @JoinColumn(name ="user_id")
    private User user;

    @NotNull
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime checkoutDate;

    private LocalDateTime dueDate;

    private LocalDateTime returnDate;
}
