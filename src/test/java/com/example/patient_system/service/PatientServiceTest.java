package com.example.patient_system.service; // Update to com.patientmanagement.service if refactored

import com.example.patient_system.model.Patient;
import com.example.patient_system.repository.PatientRepository;
import com.example.patient_system.service.PatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientService patientService;

    @Test
    public void testRegisterPatient() {
        Patient patient = new Patient();
        patient.setName("John Doe");
        patient.setEmail("john@example.com");
        patient.setPassword("password");

        // Mock the password encoder
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Mock the repository to return false for email existence
        when(patientRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);

        // Mock the save operation
        when(patientRepository.save(patient)).thenReturn(patient);

        // Call the service method
        Patient savedPatient = patientService.registerPatient(patient);

        // Assertions
        assertEquals("John Doe", savedPatient.getName());
        assertEquals("john@example.com", savedPatient.getEmail());
        assertEquals("encodedPassword", savedPatient.getPassword());
    }

    @Test
    public void testRegisterPatientEmailExists() {
        Patient patient = new Patient();
        patient.setEmail("john@example.com");

        // Mock the repository to return true for email existence
        when(patientRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(true);

        // Verify that an exception is thrown
        assertThrows(IllegalArgumentException.class, () -> patientService.registerPatient(patient));
    }
}