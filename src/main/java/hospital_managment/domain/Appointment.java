package hospital_managment.domain;

import java.time.*;

public class Appointment extends BaseEntity {

  public Patient patient;
  public Doctor doctor;
  /** When the appointment is scheduled to start (with timezone). */
  private ZonedDateTime appointmentDateTime;
  /** When the request was created (UTC instant). */
  private Instant createdAt;
  /** When the appointment was confirmed by the doctor (UTC instant). */
  private Instant confirmedAt;
  private String notes;
  private AppointmentStatus status = AppointmentStatus.REQUESTED;

  public enum AppointmentStatus { REQUESTED, CONFIRMED, REJECTED, COMPLETED, CANCELLED }

  public Appointment() {}

  /**
   * Create a new appointment request by a patient for a specific zoned date/time.
   * createdAt is stored as UTC Instant for auditing and easy comparisons.
   */
  public static Appointment requestAppointment(Patient patient, ZonedDateTime when, String notes) {
    Appointment a = new Appointment();
    a.patient = patient;
    a.appointmentDateTime = when;
    a.notes = notes;
    a.createdAt = Instant.now();
    a.status = AppointmentStatus.REQUESTED;
    return a;
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

  public void setDate (LocalDate newVar) {
    if (newVar == null) {
      this.appointmentDateTime = null;
    } else if (this.appointmentDateTime == null) {
      // preserve local date, default to start of day in system zone
      this.appointmentDateTime = newVar.atStartOfDay(ZoneId.systemDefault());
    } else {
      this.appointmentDateTime = ZonedDateTime.of(newVar, this.appointmentDateTime.toLocalTime(), this.appointmentDateTime.getZone());
    }
  }

  public LocalDate getDate () {
    return appointmentDateTime == null ? null : appointmentDateTime.toLocalDate();
  }

  public void setAppointmentDateTime(ZonedDateTime when) {
    this.appointmentDateTime = when;
  }

  public ZonedDateTime getAppointmentDateTime() {
    return appointmentDateTime;
  }

  public void setNotes (String newVar) {
    notes = newVar;
  }

  public String getNotes () {
    return notes;
  }

  public void setStatus(AppointmentStatus s) {
    status = s;
  }

  public AppointmentStatus getStatus() {
    return status;
  }

  public void confirm() {
    if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.COMPLETED) return;
    this.status = AppointmentStatus.CONFIRMED;
    this.confirmedAt = Instant.now();
  }

  /**
   * Confirm appointment and set the doctor who confirmed it. Returns true if confirmation succeeded.
   */
  public boolean confirmBy(Doctor doctor) {
    if (this.status != AppointmentStatus.REQUESTED) return false;
    this.doctor = doctor;
    this.status = AppointmentStatus.CONFIRMED;
    this.confirmedAt = Instant.now();
    return true;
  }

  public void complete() {
    status = AppointmentStatus.COMPLETED;
  }

  public void cancel() {
    status = AppointmentStatus.CANCELLED;
  }

  public boolean isInFuture() {
    if (appointmentDateTime == null) return false;
    return appointmentDateTime.toInstant().isAfter(Instant.now()) || appointmentDateTime.toLocalDate().isEqual(LocalDate.now());
  }

  @Override
  public String toString() {
    return String.format("Appointment{id=%d date=%s patient=%s doctor=%s status=%s createdAt=%s}", id,
      (appointmentDateTime == null ? "null" : appointmentDateTime.toString()), (patient == null ? "null" : patient.getId()),
      (doctor == null ? "null" : doctor.getId()), status, (createdAt == null ? "null" : createdAt.toString()));
  }

}
