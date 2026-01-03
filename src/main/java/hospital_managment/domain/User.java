package hospital_managment.domain;

public class User extends BaseEntity {

  protected String name;
  protected String surname;
  protected String email;
  private UserRole role;
  private String login;
  private String passwordHash;
  private boolean emailVerified;

  public User () { 
    this.emailVerified = false;
  }

  public void setName (String newVar) {
    name = newVar;
  }

  public String getName () {
    return name;
  }

  public void setSurname (String newVar) {
    surname = newVar;
  }

  public String getSurname () {
    return surname;
  }

  public void setEmail (String newVar) {
    email = newVar;
  }

  public String getEmail () {
    return email;
  }

  public int getId () {
    return id;
  }

  public void setRole (UserRole newVar) {
    role = newVar;
  }

  public UserRole getRole () {
    return role;
  }

  public void setLogin (String newVar) {
    login = newVar;
  }

  public String getLogin () {
    return login;
  }

  public void setPassword(String hash) {
    this.passwordHash = hash;
  }

  public boolean checkPassword(String hash) {
    if (passwordHash == null) return false;
    return passwordHash.equals(hash);
  }

  public String getPasswordHash () {
    return passwordHash;
  }

  public boolean isEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  @Override
  public String toString() {
    return String.format("User{id=%d, login=%s, name=%s %s, role=%s, verified=%s}", 
      id, login, name, surname, role, emailVerified);
  }

}
