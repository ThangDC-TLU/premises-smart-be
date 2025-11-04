package com.badmintonhub.premisessmartbe.repository;

import com.badmintonhub.premisessmartbe.entity.Premises;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PremisesRepository extends JpaRepository<Premises, Long>, JpaSpecificationExecutor<Premises> {
    @EntityGraph(attributePaths = "user")
    Optional<Premises> findWithUserById(Long id);

    @Query("""
        select p from Premises p
        where p.id <> :id
          and lower(p.businessType) = lower(:bt)
        """)
    List<Premises> findCandidatesForSimilar(@Param("id") Long id,
                                            @Param("bt") String businessType);
}
