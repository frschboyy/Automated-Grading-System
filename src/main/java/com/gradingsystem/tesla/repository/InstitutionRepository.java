package com.gradingsystem.tesla.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.gradingsystem.tesla.model.Institution;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    public Institution findByName(String name);
}
