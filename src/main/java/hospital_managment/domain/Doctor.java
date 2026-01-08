package hospital_managment.domain;

import hospital_managment.lazyload.DoctorAppointmentsLazyLoad;
import hospital_managment.lazyload.DoctorHospitalLazyLoad;
import java.time.*;
import java.util.*;

public class Doctor extends User {

  private EnumMap<DayOfWeek,TimeRange> weekSchedule;
  private DoctorHospitalLazyLoad hospitalLazy;
  private DoctorAppointmentsLazyLoad appointments;
  private String specialty;
  private int office;
  
  public transient String scheduleStartTime;
  public transient String scheduleEndTime;
  public transient List<Integer> scheduleWorkingDays;

  public Doctor () {
    weekSchedule = null;
  }

  // Week schedule methods
  public void setWeekSchedule (EnumMap<DayOfWeek,TimeRange> newVar) {
    weekSchedule = newVar;
  }

  public EnumMap<DayOfWeek,TimeRange> getWeekSchedule () {
    return weekSchedule;
  }

  public void setHospital (Hospital hospital) {
    if (hospital != null) {
      this.hospitalLazy = new DoctorHospitalLazyLoad(hospital.getId());
      this.hospitalLazy.set(hospital);
    } else {
      this.hospitalLazy = null;
    }
  }
  
  public void setHospitalId(Integer hospitalId) {
    if (hospitalId != null) {
      this.hospitalLazy = new DoctorHospitalLazyLoad(hospitalId);
    } else {
      this.hospitalLazy = null;
    }
  }

  public Hospital getHospital () {
    return hospitalLazy != null ? hospitalLazy.get() : null;
  }

  public List<Appointment> getAppointments () {
    if (appointments == null) {
      appointments = new DoctorAppointmentsLazyLoad(getId());
    }
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
    a.setStatus(Appointment.AppointmentStatus.SCHEDULED);
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

  public void addAppointment(Appointment a) {
    if (a == null) return;
    getAppointments().add(a);
  }

  @Override
  public String toString() {
    return String.format("Doctor{id=%d name=%s %s specialty=%s office=%d}", id, name, surname, specialty, office);
  }

}
