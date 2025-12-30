package hospital_managment.domain;

import hospital_managment.lazyload.PatientAppointmentsLazyLoad;
import hospital_managment.lazyload.PatientTreatmentsLazyLoad;
import java.time.*;
import java.util.*;

public class Patient extends User {

  private PatientTreatmentsLazyLoad treatments;
  private PatientAppointmentsLazyLoad appointments;

  public Patient () {
    treatments = new PatientTreatmentsLazyLoad(this);
    appointments = new PatientAppointmentsLazyLoad(this);
  }

  public List<Treatment> getTreatments () {
    return treatments.get();
  }

  public List<Appointment> getAppointments () {
    return appointments.get();
  }

  public void addTreatment(Treatment t) {
    if (t == null) return;
    getTreatments().add(t);
    incrementVersion();
  }

  public Appointment requestAppointment(Doctor doctor, LocalDate date) {
    if (doctor == null || date == null) throw new IllegalArgumentException("doctor and date required");
    Appointment a = new Appointment();
    a.setPatient(this);
    a.setDoctor(doctor);
    a.setDate(date);
    a.setNotes("Requested by patient");
    a.setStatus(Appointment.AppointmentStatus.REQUESTED);
    getAppointments().add(a);
    incrementVersion();
    return a;
  }

  public List<Appointment> getUpcomingAppointments() {
    LocalDate now = LocalDate.now();
    return getAppointments().stream()
      .filter(a -> a != null && a.getDate() != null && !a.getDate().isBefore(now))
      .toList();
  }

  @Override
  public String toString() {
    return String.format("Patient{id=%d, name=%s %s}", id, name, surname);
  }

}
