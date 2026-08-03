package com.ledger.repository;
import com.ledger.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u.id FROM User u WHERE u.username = :username")
    Optional<Long> findIdByUsername(@org.springframework.data.repository.query.Param("username") String username);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  //this annotation applirunes database locking on finding the user id when another action is beind done on that row
    Optional<User> findById(Long id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE User u SET u.balance = u.balance + :amount WHERE u.id = :id")
    void addBalance(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("amount") java.math.BigDecimal amount);
}
