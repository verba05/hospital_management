package hospital_managment.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class User extends BaseEntity {

  protected String name;
  protected String surname;
  protected String email;
  private UserRole role;
  private String login;
  private String passwordHash;

  public User () { }

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

  @Override
  public String toString() {
    return String.format("User{id=%d, login=%s, name=%s %s, role=%s}", id, login, name, surname, role);
  }

}
