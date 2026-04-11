package com.application.lebvest.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "set_password_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class SetPasswordToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "set_password_tokens_gen",
    sequenceName = "set_password_tokens_seq")
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String token;

    @OneToOne
    @JoinColumn(name = "investor_application_id")
    private InvestorApplication application;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, columnDefinition = "timestamptz")
    private Instant createdAt;
}
