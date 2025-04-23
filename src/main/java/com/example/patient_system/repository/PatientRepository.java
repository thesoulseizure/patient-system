package com.example.patient_system.repository;

import com.example.patient_system.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Patient findByEmailIgnoreCase(String email);
}