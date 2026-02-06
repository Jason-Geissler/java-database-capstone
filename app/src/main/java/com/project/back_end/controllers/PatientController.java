package com.project.back_end.controllers;

import com.project.back_end.models.Patient;
import com.project.back_end.DTO.Login;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    // Constructor injection
    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    /**
     * Get patient details using token
     */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
        }

        return patientService.getPatientDetails(token);
    }

    /**
     * Create a new patient
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        try {
            boolean valid = service.validatePatient(patient);
            if (!valid) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Patient with email id or phone no already exist"));
            }

            int created = patientService.createPatient(patient);
            if (created == 1) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", "Signup successful"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Internal server error"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Patient login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    /**
     * Get patient appointments
     */
    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
        }

        return patientService.getPatientAppointment(id, token);
    }

    /**
     * Filter patient appointments
     */
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointments(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
        }

        return service.filterPatient(condition, name, token);
    }
}
