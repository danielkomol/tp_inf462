package com.banque.customer.repository;

import com.banque.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(String userId);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByTelephone(String telephone);
    Optional<Customer> findByNumeroCNI(String numeroCNI);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
}
