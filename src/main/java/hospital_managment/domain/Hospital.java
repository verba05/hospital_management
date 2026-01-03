package hospital_managment.domain;

import hospital_managment.lazyload.HospitalDoctorsLazyLoad;
import java.util.*;

public class Hospital extends BaseEntity {

  private HospitalDoctorsLazyLoad doctors;
  private Admin admin;
  private String name;
  
  private String address;
  
  private String streetAddress;
  private String city;
  private String stateProvince;
  private String postalCode;
  private String country;
  private int appointmentIntervalMinutes;

  public Hospital () {
  }

  public List<Doctor> getDoctors () {
    if (doctors == null) {
      doctors = new HospitalDoctorsLazyLoad(getId());
    }
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

  @Deprecated
  public void setAddress (String newVar) {
    address = newVar;
  }

  @Deprecated
  public String getAddress () {
    return address;
  }

  public void setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
  }

  public String getStreetAddress() {
    return streetAddress;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCity() {
    return city;
  }

  public void setStateProvince(String stateProvince) {
    this.stateProvince = stateProvince;
  }

  public String getStateProvince() {
    return stateProvince;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCountry() {
    return country;
  }

  public void setAppointmentIntervalMinutes(int appointmentIntervalMinutes) {
    this.appointmentIntervalMinutes = appointmentIntervalMinutes;
  }

  public int getAppointmentIntervalMinutes() {
    return appointmentIntervalMinutes;
  }

  public String getFullAddress() {
    StringBuilder sb = new StringBuilder();
    if (streetAddress != null) sb.append(streetAddress);
    if (city != null) {
      if (sb.length() > 0) sb.append(", ");
      sb.append(city);
    }
    if (postalCode != null) {
      if (sb.length() > 0) sb.append(" ");
      sb.append(postalCode);
    }
    if (stateProvince != null) {
      if (sb.length() > 0) sb.append(", ");
      sb.append(stateProvince);
    }
    if (country != null && !country.equals("Poland")) {
      if (sb.length() > 0) sb.append(", ");
      sb.append(country);
    }
    return sb.toString();
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
    return String.format("Hospital{id=%d, name=%s, address=%s}", getId(), name, getFullAddress());
  }

}
