package com.badmintonhub.premisessmartbe.repository;

import com.badmintonhub.premisessmartbe.entity.Premises;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PremisesRepository extends JpaRepository<Premises, Long> {

}
