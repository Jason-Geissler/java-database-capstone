package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    /** Book a new appointment */
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1; // success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // failure
        }
    }

    /** Update an existing appointment */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment updatedAppointment) {
        Map<String, String> response = new HashMap<>();
        Optional<Appointment> existingOpt = appointmentRepository.findById(updatedAppointment.getId());

        if (!existingOpt.isPresent()) {
            response.put("message", "Appointment not found");
            return ResponseEntity.badRequest().body(response);
        }

        Appointment existing = existingOpt.get();

        // Validate doctor availability or patient ownership
        Doctor doctor = doctorRepository.findById(updatedAppointment.getDoctor().getId()).orElse(null);
        if (doctor == null) {
            response.put("message", "Doctor not found");
            return ResponseEntity.badRequest().body(response);
        }

        // Example: simple conflict check (can be expanded)
        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctor.getId(),
                updatedAppointment.getAppointmentTime(),
                updatedAppointment.getAppointmentTime().plusHours(1)
        );

        if (!doctorAppointments.isEmpty() && !doctorAppointments.contains(existing)) {
            response.put("message", "Doctor is not available at the requested time");
            return ResponseEntity.badRequest().body(response);
        }

        // Update fields
        existing.setAppointmentTime(updatedAppointment.getAppointmentTime());
        existing.setDoctor(updatedAppointment.getDoctor());
        existing.setStatus(updatedAppointment.getStatus());

        appointmentRepository.save(existing);
        response.put("message", "Appointment updated successfully");
        return ResponseEntity.ok(response);
    }

    /** Cancel an appointment */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (!appointmentOpt.isPresent()) {
            response.put("message", "Appointment not found");
            return ResponseEntity.badRequest().body(response);
        }

        Appointment appointment = appointmentOpt.get();
        Long patientIdFromToken = tokenService.getUserIdFromToken(token);

        if (!appointment.getPatient().getId().equals(patientIdFromToken)) {
            response.put("message", "You are not authorized to cancel this appointment");
            return ResponseEntity.status(403).body(response);
        }

        appointmentRepository.delete(appointment);
        response.put("message", "Appointment canceled successfully");
        return ResponseEntity.ok(response);
    }

    /** Retrieve appointments for a doctor on a specific date, optionally filtered by patient name */
    @Transactional
    public Map<String, Object> getAppointments(String pname, LocalDate date, String token) {
        Map<String, Object> result = new HashMap<>();
        Long doctorId = tokenService.getUserIdFromToken(token); // assuming token maps to doctor

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Appointment> appointments;

        if (pname != null && !pname.trim().isEmpty()) {
            appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctorId, pname, start, end);
        } else {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctorId, start, end);
        }

        result.put("appointments", appointments);
        return result;
    }

    /** Change status of an appointment */
    @Transactional
    public void changeStatus(int status, long id) {
        appointmentRepository.updateStatus(status, id);
    }
}

