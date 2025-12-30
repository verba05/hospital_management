package hospital_managment.domain;

import java.time.*;

public class Appointment extends BaseEntity {

  private Patient patient;
  private Doctor doctor;
  private Treatment treatment;
  private LocalDate appointmentDate;
  private LocalTime appointmentTime;
  private String rejectedReason;
  private String fallbackAction;
  private String fallbackTargetId;
  private String doctorNotes;
  private String patientNotes;
  //TODO finelise patientNotes
  private AppointmentStatus status = AppointmentStatus.REQUESTED;

  public enum AppointmentStatus { REQUESTED, COMPLETED, CANCELLED }

  public Appointment() {}  

  public Appointment(Patient patient, LocalDate date, LocalTime time, String doctorNotes, Doctor doctor) {
    Appointment a = new Appointment();
    a.patient = patient;
    a.appointmentDate = date;
    a.appointmentTime = time;
    a.doctorNotes = doctorNotes;
    a.status = AppointmentStatus.REQUESTED;
  }

  public void setPatient (Patient newVar) {
    patient = newVar;
  }

  public Patient getPatient () {
    return patient;
  }

  public void setDoctor (Doctor newVar) {
    doctor = newVar;
  }

  public Doctor getDoctor () {
    return doctor;
  }

  public void setTreatment (Treatment newVar) {
    treatment = newVar;
  }

  public Treatment getTreatment () {
    return treatment;
  }

  public void setPatientNotes(String newVar){
    patientNotes = newVar;
  }

  public String getPatientNotes(){
    return patientNotes;
  }

  public void setDate (LocalDate newVar) {
    if (newVar != null) {
      this.appointmentDate = newVar;
    } else {
      this.appointmentDate = LocalDate.now();
    }
  }

  public LocalDate getDate () {
    return appointmentDate;
  }

  public void setTime (LocalDate newTime) {
    if (newTime != null) {
      this.appointmentDate = newTime;
    } else {
      this.appointmentDate = LocalDate.now();
    }
  }

  public LocalTime getTime(){
    return appointmentTime;
  }

  public void setNotes (String newVar) {
    doctorNotes = newVar;
  }

  public String getNotes () {
    return doctorNotes;
  }

  public void setStatus(AppointmentStatus s) {
    status = s;
  }

  public AppointmentStatus getStatus() {
    return status;
  }

  public void complete() {
    status = AppointmentStatus.COMPLETED;
  }

  public void cancel() {
    status = AppointmentStatus.CANCELLED;
  }

  public String getRejectedReason() { return rejectedReason; }
  public String getFallbackAction() { return fallbackAction; }
  public String getFallbackTargetId() { return fallbackTargetId; }

  @Override
  public String toString() {
    return String.format("Appointment{id=%d date=%s time=%s patient=%s doctor=%s status=%s}", id,
      appointmentDate.toString(), appointmentTime.toString(), (patient == null ? "null" : patient.getId()),
      (doctor == null ? "null" : doctor.getId()), status);
  }

}
