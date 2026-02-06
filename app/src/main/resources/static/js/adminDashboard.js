// adminDashboard.js

// ------------------------
// Imports
// ------------------------
import { openModal } from "../components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "../services/doctorServices.js";
import { createDoctorCard } from "../components/doctorCard.js";

// ------------------------
// Event Binding
// ------------------------

// "Add Doctor" button opens modal
const addDocBtn = document.getElementById("addDocBtn");
if (addDocBtn) {
  addDocBtn.addEventListener("click", () => openModal("addDoctor"));
}

// ------------------------
// Load Doctor Cards on Page Load
// ------------------------
window.addEventListener("DOMContentLoaded", async () => {
  await loadDoctorCards();

  // Attach search and filter events
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
  if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
  if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});

// ------------------------
// Functions
// ------------------------

// Fetch all doctors and render as cards
export async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error loading doctors:", error);
  }
}

// Render a list of doctor cards
export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  contentDiv.innerHTML = ""; // Clear existing content

  if (!doctors || doctors.length === 0) {
    contentDiv.textContent = "No doctors found.";
    return;
  }

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

// Filter doctors based on search/filter inputs
export async function filterDoctorsOnChange() {
  try {
    const nameInput = document.getElementById("searchBar")?.value || null;
    const timeInput = document.getElementById("filterTime")?.value || null;
    const specialtyInput = document.getElementById("filterSpecialty")?.value || null;

    const doctors = await filterDoctors(
      nameInput?.trim() || null,
      timeInput?.trim() || null,
      specialtyInput?.trim() || null
    );

    if (!doctors || doctors.length === 0) {
      const contentDiv = document.getElementById("content");
      if (contentDiv) contentDiv.textContent = "No doctors found with the given filters.";
      return;
    }

    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("Failed to filter doctors. Please try again.");
  }
}

// Add a new doctor via modal form submission
export async function adminAddDoctor() {
  try {
    // Collect form inputs
    const name = document.getElementById("docName")?.value.trim();
    const email = document.getElementById("docEmail")?.value.trim();
    const phone = document.getElementById("docPhone")?.value.trim();
    const password = document.getElementById("docPassword")?.value.trim();
    const specialty = document.getElementById("docSpecialty")?.value.trim();
    const availabilityElems = document.querySelectorAll("input[name='availability']:checked");

    const availableTimes = Array.from(availabilityElems).map((el) => el.value);

    // Validate token
    const token = localStorage.getItem("token");
    if (!token) {
      alert("Admin not logged in. Please login first.");
      return;
    }

    // Build doctor object
    const doctor = { name, email, phone, password, specialty, availableTimes };

    // Call saveDoctor API
    const result = await saveDoctor(doctor, token);

    if (result.success) {
      alert("Doctor added successfully!");
      // Close modal and reload doctor list
      const modal = document.getElementById("addDoctorModal");
      if (modal) modal.classList.remove("active");

      await loadDoctorCards();
    } else {
      alert(`Failed to add doctor: ${result.message}`);
    }
  } catch (error) {
    console.error("Error adding doctor:", error);
    alert("Something went wrong while adding the doctor.");
  }
}
