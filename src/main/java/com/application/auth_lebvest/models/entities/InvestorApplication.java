package com.application.auth_lebvest.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "investor_applications")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InvestorApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "investor_application_id_gen",
    sequenceName = "investor_application_id_seq",
    allocationSize = 1)
    private Long investorApplicationId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String identityDocumentKey;

    private String proofOfAddressDocumentKey;

    private String selfieDocumentKey;
}
