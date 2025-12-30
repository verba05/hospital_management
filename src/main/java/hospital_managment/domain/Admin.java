package hospital_managment.domain;

public class Admin extends User {

  public Hospital hospital;

  public Admin () { }

  public void setHospital (Hospital newVar) {
    hospital = newVar;
  }

  public Hospital getHospital () {
    return hospital;
  }

  @Override
  public String toString() {
    return String.format("Admin{id=%d name=%s %s}", id, name, surname);
  }

}
