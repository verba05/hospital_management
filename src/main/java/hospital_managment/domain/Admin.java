package hospital_managment.domain;

import hospital_managment.lazyload.AdminHospitalLazyLoad;

public class Admin extends User {

  private AdminHospitalLazyLoad hospitalLazy;

  public Admin () { }

  public void setHospital (Hospital hospital) {
    if (hospital != null) {
      this.hospitalLazy = new AdminHospitalLazyLoad(hospital.getId());
      this.hospitalLazy.set(hospital);
    } else {
      this.hospitalLazy = null;
    }
  }
  
  public void setHospitalId(Integer hospitalId) {
    if (hospitalId != null) {
      this.hospitalLazy = new AdminHospitalLazyLoad(hospitalId);
    } else {
      this.hospitalLazy = null;
    }
  }

  public Hospital getHospital () {
    return hospitalLazy != null ? hospitalLazy.get() : null;
  }

  @Override
  public String toString() {
    return String.format("Admin{id=%d name=%s %s}", id, name, surname);
  }

}
