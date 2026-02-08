package org.example.g2bplatform.repository;

import org.example.g2bplatform.entity.PasswordResetToken;
import org.example.g2bplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
            String tokenHash, Instant expiresAt);

    void deleteByUser(User user);

    void deleteByExpiresAtBefore(Instant instant);
}
