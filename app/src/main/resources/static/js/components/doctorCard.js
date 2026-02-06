// doctorCard.js
import { showBookingOverlay } from './loggedPatient.js';
import { deleteDoctor } from './doctorServices.js';
import { getPatientData } from './patientServices.js';

export function createDoctorCard(doctor) {
  // Create main card container
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  // Fetch user role
  const role = localStorage.getItem("userRole");

  // Doctor info section
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const name = document.createElement("h3");
  name.textContent = doctor.name;

  const specialization = document.createElement("p");
  specialization.textContent = `Specialization: ${doctor.specialty}`;

  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email}`;

  const availability = document.createElement("p");
  availability.textContent = `Available Times: ${doctor.availability.join(", ")}`;

  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  // Card actions container
  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  // === Admin role actions ===
  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "Delete";
    removeBtn.addEventListener("click", async () => {
      if (confirm(`Are you sure you want to delete Dr. ${doctor.name}?`)) {
        const token = localStorage.getItem("token");
        try {
          const result = await deleteDoctor(doctor.id, token);
          if (result.success) {
            alert("Doctor deleted successfully.");
            card.remove();
          } else {
            alert("Failed to delete doctor.");
          }
        } catch (err) {
          console.error(err);
          alert("Error deleting doctor.");
        }
      }
    });
    actionsDiv.appendChild(removeBtn);
  }
  // === Patient not logged in actions ===
  else if (role === "patient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", () => {
      alert("Patient needs to login first.");
    });
    actionsDiv.appendChild(bookNow);
  }
  // === Logged-in patient actions ===
  else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", async (e) => {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Session expired. Please log in again.");
        window.location.href = "/pages/patientDashboard.html";
        return;
      }
      try {
        const patientData = await getPatientData(token);
        showBookingOverlay(e, doctor, patientData);
      } catch (err) {
        console.error(err);
        alert("Failed to fetch patient data.");
      }
    });
    actionsDiv.appendChild(bookNow);
  }

  // Assemble the card
  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  return card;
}
