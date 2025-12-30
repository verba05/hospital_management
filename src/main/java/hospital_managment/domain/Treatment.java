package hospital_managment.domain;

import java.util.*;

public class Treatment extends BaseEntity {

  private Patient patient;
  private Appointment appointment;
  private String instructions;
  private Vector<String> medications;

  public Treatment () {
    medications = new Vector<>();
  }

  public void setPatient (Patient newVar) {
    patient = newVar;
  }

  public Patient getPatient () {
    return patient;
  }

  public void setAppointment (Appointment newVar) {
    appointment = newVar;
  }

  public Appointment getAppointment () {
    return appointment;
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

  @Override
  public String toString() {
    return String.format("Treatment{id=%d, patientId=%s, appointmentId=%s, meds=%s, instructions=%s}",
      id,
      patient == null ? "null" : patient.getId(),
      appointment == null ? "null" : appointment.getId(),
      medications,
      instructions);
  }

}
