package com.example.patient_system.controller;

import com.example.patient_system.model.Appointment;
import com.example.patient_system.model.Doctor;
import com.example.patient_system.model.Medication;
import com.example.patient_system.model.Patient;
import com.example.patient_system.service.AppointmentService;
import com.example.patient_system.service.DoctorService;
import com.example.patient_system.service.MedicationService;
import com.example.patient_system.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicationService medicationService;

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @GetMapping("/")
    public String home(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        logger.info("Accessing homepage, authenticated: {}", authentication != null && authentication.isAuthenticated());
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                logger.info("Fetching patient for email: {}", email);
                Patient patient = patientService.findByEmail(email);
                if (patient == null) {
                    logger.error("Patient not found for email: {}, redirecting to /register", email);
                    return "redirect:/register";
                }
                // Validate patient object
                if (patient.getId() == null) {
                    logger.error("Patient ID is null for email: {}, redirecting to /register", email);
                    return "redirect:/register";
                }
                logger.info("Patient found: name={}, id={}", patient.getName(), patient.getId());
                model.addAttribute("patientName", patient.getName() != null ? patient.getName() : "Unknown");
                // Handle appointments with try-catch
                try {
                    List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient);
                    logger.info("Appointments fetched for patient {}: size={}", patient.getId(), appointments != null ? appointments.size() : "null");
                    model.addAttribute("appointments", appointments != null ? appointments : Collections.emptyList());
                    model.addAttribute("appointmentCount", appointments != null ? appointments.size() : 0);
                } catch (Exception e) {
                    logger.error("Failed to fetch appointments for patient {}: {}", patient.getId(), e.getMessage(), e);
                    model.addAttribute("appointments", Collections.emptyList());
                    model.addAttribute("appointmentCount", 0);
                }
                // Handle medications with try-catch
                try {
                    List<Medication> medications = medicationService.getMedicationsByPatient(patient.getId());
                    logger.info("Medications fetched for patient {}: size={}", patient.getId(), medications != null ? medications.size() : "null");
                    model.addAttribute("medications", medications != null ? medications : Collections.emptyList());
                    model.addAttribute("medicationCount", medications != null ? medications.size() : 0);
                } catch (Exception e) {
                    logger.error("Failed to fetch medications for patient {}: {}", patient.getId(), e.getMessage(), e);
                    model.addAttribute("medications", Collections.emptyList());
                    model.addAttribute("medicationCount", 0);
                }
            } else {
                logger.info("Unauthenticated user, showing login prompt");
                model.addAttribute("message", "Please log in to manage your health.");
                model.addAttribute("loginUrl", "/login");
                model.addAttribute("patientName", null);
            }
            boolean hasError = redirectAttributes.getFlashAttributes().containsKey("error") &&
                    redirectAttributes.getFlashAttributes().get("error") != null;
            model.addAttribute("error", hasError);
            if (redirectAttributes.getFlashAttributes().containsKey("success")) {
                model.addAttribute("success", redirectAttributes.getFlashAttributes().get("success"));
            }
            logger.info("Rendering index.html");
            return "index";
        } catch (Exception e) {
            logger.error("Error in home page: {}", e.getMessage(), e);
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "index";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        logger.info("Displaying registration form");
        model.addAttribute("patient", new Patient());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("patient") Patient patient, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Processing registration for patient: {}", patient.getEmail());
        if (result.hasErrors()) {
            logger.warn("Validation errors found in registration form");
            return "register";
        }
        try {
            patientService.registerPatient(patient);
            logger.info("Patient registered successfully: {}", patient.getEmail());
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            logger.error("Registration failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, @RequestParam(value = "error", required = false) String error) {
        logger.info("Displaying login form, error parameter: {}", error);
        if (error != null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        return "login";
    }

    @GetMapping("/appointments")
    public String showAppointments(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        logger.info("Accessing appointments page, authenticated: {}", authentication != null && authentication.isAuthenticated());
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to /appointments");
            return "redirect:/login";
        }
        String email = authentication.getName();
        Patient patient = patientService.findByEmail(email);
        if (patient == null) {
            logger.error("Patient not found for email: {}, redirecting to /register", email);
            return "redirect:/register";
        }
        List<Doctor> doctors = doctorService.getAllDoctors() != null ? doctorService.getAllDoctors() : Collections.emptyList();
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient) != null
                ? appointmentService.getAppointmentsByPatient(patient) : Collections.emptyList();
        model.addAttribute("doctors", doctors);
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("patientId", patient.getId());
        model.addAttribute("appointments", appointments);
        if (redirectAttributes.getFlashAttributes().containsKey("success")) {
            model.addAttribute("success", redirectAttributes.getFlashAttributes().get("success"));
        }
        if (redirectAttributes.getFlashAttributes().containsKey("error")) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", redirectAttributes.getFlashAttributes().get("error"));
        }
        logger.info("Rendering appointments.html for patientId: {}", patient.getId());
        return "appointments";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@RequestParam(required = false) Long patientId, @RequestParam Long doctorId,
                                  @RequestParam String appointmentTime, @RequestParam(required = false) String notes,
                                  Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Received booking request - patientId: {}, doctorId: {}, appointmentTime: {}, notes: {}",
                patientId, doctorId, appointmentTime, notes);

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to book appointment");
            return "redirect:/login";
        }
        String email = authentication.getName();
        Patient patient = patientService.findByEmail(email);
        if (patient == null) {
            logger.error("Patient not found for email: {}", email);
            redirectAttributes.addFlashAttribute("error", "User not found for email: " + email);
            return "redirect:/appointments";
        }
        if (patientId == null || !patient.getId().equals(patientId)) {
            logger.warn("Patient ID missing or mismatched - using logged-in patient ID: {}", patient.getId());
            patientId = patient.getId();
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime time = LocalDateTime.parse(appointmentTime, formatter);
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctorService.getDoctorById(doctorId));
            appointment.setAppointmentTime(time);
            appointment.setStatus("SCHEDULED");
            appointment.setNotes(notes != null ? notes : "");
            appointmentService.bookAppointment(appointment);
            redirectAttributes.addFlashAttribute("success", "Appointment booked successfully!");
        } catch (DateTimeParseException e) {
            logger.error("DateTime parsing failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Invalid appointment time format: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during booking: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/appointments";
    }

    @GetMapping("/medications")
    public String showMedications(Model model, Authentication authentication) {
        logger.info("Accessing medications page, authenticated: {}", authentication != null && authentication.isAuthenticated());
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to /medications");
            return "redirect:/login";
        }
        String email = authentication.getName();
        Patient patient = patientService.findByEmail(email);
        if (patient == null) {
            logger.error("Patient not found for email: {}, redirecting to /register", email);
            return "redirect:/register";
        }
        List<Medication> medications = medicationService.getMedicationsByPatient(patient.getId()) != null
                ? medicationService.getMedicationsByPatient(patient.getId()) : Collections.emptyList();
        model.addAttribute("medications", medications);
        model.addAttribute("medication", new Medication());
        model.addAttribute("patientId", patient.getId());
        logger.info("Rendering medications.html for patientId: {}", patient.getId());
        return "medications";
    }

    @PostMapping("/medications/add")
    public String addMedication(@RequestParam Long patientId, @ModelAttribute Medication medication, Authentication authentication, RedirectAttributes redirectAttributes) {
        logger.info("Adding medication for patientId: {}, medication: {}", patientId, medication.getName());
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to add medication");
            return "redirect:/login";
        }
        String email = authentication.getName();
        Patient patient = patientService.findByEmail(email);
        if (patient == null || !patient.getId().equals(patientId)) {
            logger.error("Patient not found or ID mismatch for email: {}, redirecting", email);
            return "redirect:/medications?patientId=" + (patient != null ? patient.getId() : 0);
        }
        try {
            medicationService.addMedication(patient, medication);
            logger.info("Medication added successfully for patientId: {}", patientId);
            redirectAttributes.addFlashAttribute("success", "Medication added successfully!");
        } catch (Exception e) {
            logger.error("Failed to add medication: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to add medication: " + e.getMessage());
        }
        return "redirect:/medications?patientId=" + patientId;
    }

    @PostMapping("/medications/delete/{id}")
    public String deleteMedication(@PathVariable Long id, @RequestParam Long patientId, Authentication authentication, RedirectAttributes redirectAttributes) {
        logger.info("Deleting medication id: {} for patientId: {}", id, patientId);
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to delete medication");
            return "redirect:/login";
        }
        String email = authentication.getName();
        Patient patient = patientService.findByEmail(email);
        if (patient == null || !patient.getId().equals(patientId)) {
            logger.error("Patient not found or ID mismatch for email: {}, redirecting", email);
            return "redirect:/medications?patientId=" + (patient != null ? patient.getId() : 0);
        }
        try {
            medicationService.deleteMedication(id);
            logger.info("Medication deleted successfully for patientId: {}", patientId);
            redirectAttributes.addFlashAttribute("success", "Medication deleted successfully!");
        } catch (Exception e) {
            logger.error("Failed to delete medication: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete medication: " + e.getMessage());
        }
        return "redirect:/medications?patientId=" + patientId;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        logger.info("Accessing profile page, authenticated: {}", authentication != null && authentication.isAuthenticated());
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to /profile");
            return "redirect:/login";
        }
        String email = authentication.getName();
        Patient patient = patientService.findByEmail(email);
        if (patient == null) {
            logger.error("Patient not found for email: {}, redirecting to /register", email);
            return "redirect:/register";
        }
        model.addAttribute("patient", patient);
        logger.info("Rendering profile.html for patientId: {}", patient.getId());
        return "profile";
    }

    @GetMapping("/logout")
    public String logoutPage() {
        logger.info("Logout requested");
        return "redirect:/login?logout=true";
    }
}