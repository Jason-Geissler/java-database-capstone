package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /** Save a new doctor */
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1; // Conflict: email exists
            }
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Internal error
        }
    }

    /** Update an existing doctor */
    public int updateDoctor(Doctor doctor) {
        Optional<Doctor> existing = doctorRepository.findById(doctor.getId());
        if (!existing.isPresent()) return -1;

        doctorRepository.save(doctor);
        return 1;
    }

    /** Delete a doctor and all their appointments */
    public int deleteDoctor(Long doctorId) {
        Optional<Doctor> existing = doctorRepository.findById(doctorId);
        if (!existing.isPresent()) return -1;

        appointmentRepository.deleteAllByDoctorId(doctorId);
        doctorRepository.deleteById(doctorId);
        return 1;
    }

    /** Validate doctor's login */
    public String validateDoctor(String email, String password) {
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null) return "Doctor not found";
        if (!doctor.getPassword().equals(password)) return "Invalid password";

        return tokenService.generateToken(doctor.getId());
    }

    /** Get all doctors */
    @Transactional
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /** Find doctors by partial name match */
    @Transactional
    public List<Doctor> findDoctorByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, "");
    }

    /** Get available slots for a doctor on a given date */
    @Transactional
    public List<LocalTime> getDoctorAvailability(Long doctorId, LocalDate date) {
        List<LocalTime> allSlots = new ArrayList<>();
        for (int hour = 9; hour <= 17; hour++) {
            allSlots.add(LocalTime.of(hour, 0));
        }

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, date.atStartOfDay(), date.atTime(23, 59)
        );

        for (Appointment a : appointments) {
            allSlots.remove(a.getAppointmentTime().toLocalTime());
        }

        return allSlots;
    }

    /** Filter doctors by name, specialty, and AM/PM time */
    @Transactional
    public List<Doctor> filterDoctorsByNameSpecialtyAndTime(String name, String specialty, String timePeriod) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return filterDoctorByTime(doctors, timePeriod);
    }

    /** Filter a list of doctors by AM/PM */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String timePeriod) {
        return doctors.stream().filter(doc -> {
            List<LocalTime> availableTimes = new ArrayList<>();
            for (int hour = 9; hour <= 17; hour++) availableTimes.add(LocalTime.of(hour, 0));

            if ("AM".equalsIgnoreCase(timePeriod)) {
                return availableTimes.stream().anyMatch(t -> t.isBefore(LocalTime.NOON));
            } else if ("PM".equalsIgnoreCase(timePeriod)) {
                return availableTimes.stream().anyMatch(t -> !t.isBefore(LocalTime.NOON));
            }
            return true;
        }).collect(Collectors.toList());
    }

    /** Filter doctors by name and time */
    @Transactional
    public List<Doctor> filterDoctorByNameAndTime(String name, String timePeriod) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, "");
        return filterDoctorByTime(doctors, timePeriod);
    }

    /** Filter doctors by name and specialty */
    @Transactional
    public List<Doctor> filterDoctorByNameAndSpecialty(String name, String specialty) {
        return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
    }

    /** Filter doctors by time and specialty */
    @Transactional
    public List<Doctor> filterDoctorByTimeAndSpecialty(String specialty, String timePeriod) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return filterDoctorByTime(doctors, timePeriod);
    }

    /** Filter doctors by specialty */
    @Transactional
    public List<Doctor> filterDoctorBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty);
    }

    /** Filter doctors by availability time (AM/PM) */
    @Transactional
    public List<Doctor> filterDoctorsByTime(String timePeriod) {
        List<Doctor> doctors = doctorRepository.findAll();
        return filterDoctorByTime(doctors, timePeriod);
    }
}
