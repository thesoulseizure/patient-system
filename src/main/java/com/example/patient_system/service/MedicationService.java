package com.example.patient_system.service;

import com.example.patient_system.model.Medication;
import com.example.patient_system.model.Patient;
import com.example.patient_system.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    public Medication addMedication(Patient patient, Medication medication) {
        medication.setPatient(patient);
        return medicationRepository.save(medication);
    }

    public List<Medication> getMedicationsByPatient(Long patientId) {
        return medicationRepository.findByPatientId(patientId);
    }

    public void deleteMedication(Long medicationId) {
        medicationRepository.deleteById(medicationId);
    }
}