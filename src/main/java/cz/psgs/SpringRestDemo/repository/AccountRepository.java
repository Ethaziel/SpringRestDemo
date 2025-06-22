package cz.psgs.SpringRestDemo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.psgs.SpringRestDemo.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

}
