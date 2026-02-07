package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Retrieves a patient using their email address.
     *
     * @param email the patient's email
     * @return the matching Patient entity, or null if not found
     */
    Patient findByEmail(String email);

    /**
     * Retrieves a patient using either their email address or phone number.
     *
     * @param email the patient's email
     * @param phone the patient's phone number
     * @return the matching Patient entity, or null if neither match
     */
    Patient findByEmailOrPhone(String email, String phone);
}
