package com.badmintonhub.premisessmartbe.repository;

import com.badmintonhub.premisessmartbe.entity.Premises;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremisesRepository extends JpaRepository<Premises, Long> {
}
