package com.banque.operator.repository;

import com.banque.operator.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByCode(String code);
    List<Operator> findByStatut(Operator.StatutOperateur statut);
    List<Operator> findByType(Operator.TypeOperateur type);
    boolean existsByCode(String code);
}
