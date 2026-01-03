package hospital_managment.domain;

import hospital_managment.lazyload.TreatmentPatientLazyLoad;
import hospital_managment.lazyload.TreatmentAppointmentLazyLoad;
import java.time.LocalDateTime;
import java.util.*;

public class Treatment extends BaseEntity {

  private TreatmentPatientLazyLoad patientLazy;
  private TreatmentAppointmentLazyLoad appointmentLazy;
  private String instructions;
  private Vector<String> medications;
  private LocalDateTime createdAt;

  public Treatment () {
    medications = new Vector<>();
  }

  public void setPatient (Patient patient) {
    if (patient != null) {
      this.patientLazy = new TreatmentPatientLazyLoad(patient.getId());
      this.patientLazy.set(patient);
    } else {
      this.patientLazy = null;
    }
  }
  
  public void setPatientId(Integer patientId) {
    if (patientId != null) {
      this.patientLazy = new TreatmentPatientLazyLoad(patientId);
    } else {
      this.patientLazy = null;
    }
  }

  public Patient getPatient () {
    return patientLazy != null ? patientLazy.get() : null;
  }

  public void setAppointment (Appointment appointment) {
    if (appointment != null) {
      this.appointmentLazy = new TreatmentAppointmentLazyLoad(appointment.getId());
      this.appointmentLazy.set(appointment);
    } else {
      this.appointmentLazy = null;
    }
  }
  
  public void setAppointmentId(Integer appointmentId) {
    if (appointmentId != null) {
      this.appointmentLazy = new TreatmentAppointmentLazyLoad(appointmentId);
    } else {
      this.appointmentLazy = null;
    }
  }

  public Appointment getAppointment () {
    return appointmentLazy != null ? appointmentLazy.get() : null;
  }

  public void setInstructions (String newVar) {
    instructions = newVar;
  }

  public String getInstructions () {
    return instructions;
  }

  public void setMedications (Vector<String> newVar) {
    medications = newVar;
  }

  public Vector<String> getMedications () {
    return medications;
  }

  public void addMedication(String med) {
    if (med == null) return;
    if (medications == null) medications = new Vector<>();
    medications.add(med);
    incrementVersion();
  }

  public boolean removeMedication(String med) {
    if (medications == null) return false;
    boolean removed = medications.remove(med);
    if (removed) incrementVersion();
    return removed;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    Patient p = getPatient();
    Appointment a = getAppointment();
    return String.format("Treatment{id=%d, patientId=%s, appointmentId=%s, meds=%s, instructions=%s}",
      id,
      p == null ? "null" : p.getId(),
      a == null ? "null" : a.getId(),
      medications,
      instructions);
  }

}
