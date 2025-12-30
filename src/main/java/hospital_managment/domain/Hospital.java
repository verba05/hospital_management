package hospital_managment.domain;

import hospital_managment.lazyload.HospitalDoctorsLazyLoad;
import java.util.*;

public class Hospital extends BaseEntity {

  private HospitalDoctorsLazyLoad doctors;
  private Admin admin;
  private String name;
  private String address;

  public Hospital () {
    doctors = new HospitalDoctorsLazyLoad(this);
  }

  public List<Doctor> getDoctors () {
    return doctors.get();
  }

  public void setAdmin (Admin newVar) {
    admin = newVar;
  }

  public Admin getAdmin () {
    return admin;
  }

  public void setName (String newVar) {
    name = newVar;
  }

  public String getName () {
    return name;
  }

  public void setAddress (String newVar) {
    address = newVar;
  }

  public String getAddress () {
    return address;
  }

  public int getId () {
    return id;
  }

  public void addDoctor(Doctor d) {
    if (d == null) return;
    getDoctors().add(d);
    incrementVersion();
  }

  public boolean removeDoctor(Doctor d) {
    if (d == null) return false;
    boolean removed = getDoctors().remove(d);
    if (removed) incrementVersion();
    return removed;
  }

  public List<Doctor> findDoctorsBySpecialty(String specialty) {
    if (specialty == null) return Collections.emptyList();
    return getDoctors().stream()
      .filter(d -> d != null && specialty.equals(d.getSpecialty()))
      .toList();
  }

  @Override
  public String toString() {
    return String.format("Hospital{id=%d, name=%s, address=%s}", getId(), name, address);
  }

}
