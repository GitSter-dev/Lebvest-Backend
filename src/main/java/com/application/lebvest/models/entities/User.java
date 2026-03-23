package com.application.lebvest.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Table(name = "users")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User  {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @OneToOne
    private UserRole role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

//    @Override
//    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
//        return  List.of(new SimpleGrantedAuthority(role.getRoleName()));
//    }
//
//    @Override
//    public @Nullable String getPassword() {
//        return passwordHash;
//    }
//
//    @Override
//    public @NonNull String getUsername() {
//        return email;
//    }
}
