package com.application.lebvest.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "investor_signup_applications")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class InvestorSignupApplication {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private  String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String nationality;

    @Column(nullable = false)
    private String countryOfResidence;

    private String identityDocumentKey;

    private String proofOfResidenceDocumentKey;

    @ElementCollection
    @CollectionTable(
            name = "source_of_funds_documents_keys",
            joinColumns = @JoinColumn(name = "investor_signup_application_id")
    )
    private List<String> sourceOfFundsDocumentsKeys;

}
