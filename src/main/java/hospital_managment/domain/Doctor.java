package hospital_managment.domain;

import hospital_managment.lazyload.DoctorAppointmentsLazyLoad;
import java.time.*;
import java.util.*;

public class Doctor extends User {

  private DoctorSchedule schedule;
  private Hospital hospital;
  private DoctorAppointmentsLazyLoad appointments;
  private String specialty;
  private int office;

  public Doctor () {
    appointments = new DoctorAppointmentsLazyLoad(this);
  }

  public void setSchedule (DoctorSchedule newVar) {
    schedule = newVar;
  }

  public DoctorSchedule getSchedule () {
    return schedule;
  }

  public void setHospital (Hospital newVar) {
    hospital = newVar;
  }

  public Hospital getHospital () {
    return hospital;
  }

  public List<Appointment> getAppointments () {
    return appointments.get();
  }

  public void setSpecialty (String newVar) {
    specialty = newVar;
  }

  public String getSpecialty () {
    return specialty;
  }

  public void setOffice (int newVar) {
    office = newVar;
  }

  public int getOffice () {
    return office;
  }

  public Appointment createFollowUpAppointment(Appointment previous, LocalDate date) {
    if (previous == null || date == null) throw new IllegalArgumentException("previous appointment and date required");
    Appointment a = new Appointment();
    a.setPatient(previous.getPatient());
    a.setDoctor(this);
    a.setDate(date);
    a.setNotes("Follow-up from appointment " + previous.getId());
    a.setStatus(Appointment.AppointmentStatus.REQUESTED);
    getAppointments().add(a);
    return a;
  }

  public Treatment createTreatmentForAppointment(Appointment appointment, String instructions, Collection<String> meds) {
    if (appointment == null) throw new IllegalArgumentException("appointment required");
    Treatment t = new Treatment();
    t.setPatient(appointment.getPatient());
    t.setAppointment(appointment);
    t.setInstructions(instructions);
    if (meds != null) {
      for (String m : meds) t.addMedication(m);
    }
    appointment.getPatient().addTreatment(t);
    appointment.setTreatment(t);
    return t;
  }

  // public boolean isAvailable(LocalDate date, LocalTime time) {
  //   if (schedule == null) return true;
  //   return schedule.isAvailable(date, time);
  // }

  public void addAppointment(Appointment a) {
    if (a == null) return;
    getAppointments().add(a);
    incrementVersion();
  }

  @Override
  public String toString() {
    return String.format("Doctor{id=%d name=%s %s specialty=%s office=%d}", id, name, surname, specialty, office);
  }

}
