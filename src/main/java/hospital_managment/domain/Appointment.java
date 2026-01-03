package hospital_managment.domain;

import hospital_managment.lazyload.AppointmentPatientLazyLoad;
import hospital_managment.lazyload.AppointmentDoctorLazyLoad;
import hospital_managment.lazyload.AppointmentTreatmentLazyLoad;
import java.time.*;

public class Appointment extends BaseEntity {

  private AppointmentPatientLazyLoad patientLazy;
  private AppointmentDoctorLazyLoad doctorLazy;
  private AppointmentTreatmentLazyLoad treatmentLazy;
  private LocalDate appointmentDate;
  private LocalTime appointmentTime;
  private String doctorNotes;
  private AppointmentStatus status = AppointmentStatus.SCHEDULED;

  public enum AppointmentStatus { SCHEDULED, NO_SHOW, COMPLETED, CANCELED }

  public Appointment() {}  

  public Appointment(Patient patient, LocalDate date, LocalTime time, String doctorNotes, Doctor doctor) {
    this.setPatient(patient);
    this.setDoctor(doctor);
    this.appointmentDate = date;
    this.appointmentTime = time;
    this.doctorNotes = doctorNotes;
    this.status = AppointmentStatus.SCHEDULED;
  }

  public void setPatient (Patient patient) {
    if (patient != null) {
      this.patientLazy = new AppointmentPatientLazyLoad(patient.getId());
      this.patientLazy.set(patient);
    } else {
      this.patientLazy = null;
    }
  }
  
  public void setPatientId(Integer patientId) {
    if (patientId != null) {
      this.patientLazy = new AppointmentPatientLazyLoad(patientId);
    } else {
      this.patientLazy = null;
    }
  }

  public Patient getPatient () {
    return patientLazy != null ? patientLazy.get() : null;
  }

  public void setDoctor (Doctor doctor) {
    if (doctor != null) {
      this.doctorLazy = new AppointmentDoctorLazyLoad(doctor.getId());
      this.doctorLazy.set(doctor);
    } else {
      this.doctorLazy = null;
    }
  }
  
  public void setDoctorId(Integer doctorId) {
    if (doctorId != null) {
      this.doctorLazy = new AppointmentDoctorLazyLoad(doctorId);
    } else {
      this.doctorLazy = null;
    }
  }

  public Doctor getDoctor () {
    return doctorLazy != null ? doctorLazy.get() : null;
  }
  
  public Integer getPatientId() {
    return patientLazy != null ? patientLazy.getId() : null;
  }
  
  public Integer getDoctorId() {
    return doctorLazy != null ? doctorLazy.getId() : null;
  }

  public void setTreatment (Treatment treatment) {
    if (treatment != null) {
      this.treatmentLazy = new AppointmentTreatmentLazyLoad(this.getId());
      this.treatmentLazy.set(treatment);
    } else {
      this.treatmentLazy = null;
    }
  }
  
  public void setTreatmentByAppointmentId(Integer appointmentId) {
    if (appointmentId != null) {
      this.treatmentLazy = new AppointmentTreatmentLazyLoad(appointmentId);
    } else {
      this.treatmentLazy = null;
    }
  }

  public Treatment getTreatment () {
    return treatmentLazy != null ? treatmentLazy.get() : null;
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

  public void setTime (LocalTime newTime) {
    if (newTime != null) {
      this.appointmentTime = newTime;
    } else {
      this.appointmentTime = LocalTime.now();
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
    status = AppointmentStatus.CANCELED;
  }
  
  @Override
  public String toString() {
    Patient p = getPatient();
    Doctor d = getDoctor();
    return String.format("Appointment{id=%d date=%s time=%s patient=%s doctor=%s status=%s}", id,
      appointmentDate.toString(), appointmentTime.toString(), (p == null ? "null" : p.getId()),
      (d == null ? "null" : d.getId()), status);
  }

}
