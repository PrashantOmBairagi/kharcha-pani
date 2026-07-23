package com.prashant.kharchapaniapplication.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prashant.kharchapaniapplication.expense.Expense;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.hibernate.internal.util.collections.CollectionHelper.listOf;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required.")
    @Email(message = "Enter valid Email address.")
    private String email; //Using as Username

    @Column(nullable = false)
    @NotBlank(message = "Password is required.")
    @Size(min = 6,
            message = "Password must be between 6 and 50 characters.")
    private String password;

    @Size(min = 3, max = 20,
            message = "First name must be between 3 and 20 characters.")
    private String firstName;

    @Size(max = 20,
            message = "Last name cannot exceed 20 characters.")
    private String lastName;

    @Column(unique = true)
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Enter a valid 10-digit Indian mobile number."
    )
    private String phone;

    @Column(nullable =true, precision = 19, scale = 2)
    private Long budget;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private boolean profileComplete;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Expense> expenses = new ArrayList<>();

    //overridden methods of implemented class userDetail
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return listOf();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}