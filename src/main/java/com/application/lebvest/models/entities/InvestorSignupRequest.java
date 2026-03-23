package com.application.lebvest.models.entities;

import com.application.lebvest.models.enums.InvestorSignupRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@ToString
@Entity
@Table(name = "investor_signup_requests")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InvestorSignupRequest {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Personal Info
    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String nationality;
    private String countryOfResidence;
    private String address;

    // Economic Profile
    private String occupation;
    private String sourceOfFunds;
    private Long estimatedAnnualIncome;

    // Compliance
    private Boolean pep;
    private Boolean relativeOrFamilyPepStatus;
    private Boolean isUsPerson;
    private String taxIdNumber;

    // Investment Profile
    private String riskTolerance;
    private Integer yearsOfExperience;

    // Document paths
    private String nationalIdOrPassportPath;
    private String proofOfResidencePath;

    @ElementCollection
    @CollectionTable(name = "investor_address_proof_documents", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "document_path")
    private List<String> addressProofDocumentPaths;

    @ElementCollection
    @CollectionTable(name = "investor_source_of_funds_documents", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "document_path")
    private List<String> sourceOfFundsDocumentPaths;

    // Status
    @Enumerated(EnumType.STRING)
    private InvestorSignupRequestStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @Column(updatable = false, insertable = false)
    @UpdateTimestamp
    private Instant updatedAt;
}
