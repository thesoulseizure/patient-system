package com.example.patient_system.service;

import com.example.patient_system.model.Patient;
import com.example.patient_system.repository.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientService(PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Patient registerPatient(Patient patient) {
        String email = patient.getEmail().toLowerCase().trim();
        if (patientRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        patient.setEmail(email);
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        return patientRepository.save(patient);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
    }

    public Patient findByEmail(String email) {
        return patientRepository.findByEmailIgnoreCase(email.toLowerCase().trim());
    }
}