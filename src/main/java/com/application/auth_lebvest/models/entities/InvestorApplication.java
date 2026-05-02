package com.application.auth_lebvest.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "investor_applications")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InvestorApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "investor_application_id_gen",
    sequenceName = "investor_application_id_seq",
    allocationSize = 1)
    private Long investorApplicationId;

    private String identityDocumentKey;

    private String proofOfAddressDocumentKey;

    private String selfieDocumentKey;
}
