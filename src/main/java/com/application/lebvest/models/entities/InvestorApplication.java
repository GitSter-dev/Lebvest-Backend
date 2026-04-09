package com.application.lebvest.models.entities;

import com.application.lebvest.models.enums.InvestorApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "investor_applications")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class InvestorApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "investor_applications_gen",
            sequenceName = "investor_applications_seq"
    )
    private Long id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false, unique = true, columnDefinition = "citext")
    @EqualsAndHashCode.Include
    private String email;

    private String identityDocumentKey;

    private String addressProofDocumentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "investor_application_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private InvestorApplicationStatus applicationStatus = InvestorApplicationStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
