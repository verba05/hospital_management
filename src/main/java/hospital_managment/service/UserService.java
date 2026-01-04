package hospital_managment.service;

import hospital_managment.domain.User;
import hospital_managment.domain.Patient;
import hospital_managment.domain.Doctor;
import hospital_managment.domain.Admin;
import hospital_managment.domain.UserRole;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.Query;
import hospital_managment.repository.UserRepository;
import hospital_managment.repository.PatientRepository;
import hospital_managment.repository.DoctorRepository;
import hospital_managment.repository.AdminRepository;
import java.util.List;

public class UserService {

    public User authenticate(String login, String passwordHash) {
        UserRepository userRepo = (UserRepository) UnitOfWorkContext.getRegistry().getRepository(User.class);
        Query query = new Query()
            .where("login", Query.Operator.EQUALS, login);
        
        List<User> users = userRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        if (users.isEmpty()) {
            return null;
        }
        
        User user = users.get(0);
        if (user.checkPassword(passwordHash)) {
            return user;
        }
        
        return null;
    }

    public User getUserById(int id) {
        UserRepository userRepo = (UserRepository) UnitOfWorkContext.getRegistry().getRepository(User.class);
        Query query = new Query()
            .where("id", Query.Operator.EQUALS, id)
            .limit(1);
        
        List<User> users = userRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        return users.isEmpty() ? null : users.get(0);
    }

    public Patient getPatientById(int id) {
        try {
            PatientRepository patientRepo = (PatientRepository) UnitOfWorkContext.getRegistry().getRepository(Patient.class);
            
            Query query = new Query()
                .where("id", Query.Operator.EQUALS, id)
                .limit(1);
            
            List<Patient> patients = patientRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
            
            Patient result = patients.isEmpty() ? null : patients.get(0);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Doctor getDoctorById(int id) {
        DoctorRepository doctorRepo = (DoctorRepository) UnitOfWorkContext.getRegistry().getRepository(Doctor.class);
        Query query = new Query()
            .where("d.doctor_id", Query.Operator.EQUALS, id)
            .limit(1);
        
        List<Doctor> doctors = doctorRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        return doctors.isEmpty() ? null : doctors.get(0);
    }

    public Admin getAdminById(int id) {
        AdminRepository adminRepo = (AdminRepository) UnitOfWorkContext.getRegistry().getRepository(Admin.class);
        Query query = new Query()
            .where("u.id", Query.Operator.EQUALS, id)
            .limit(1);
        
        List<Admin> admins = adminRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        return admins.isEmpty() ? null : admins.get(0);
    }

    public Patient createPatient(String name, String surname, String email, String login, String passwordHash) {
        Patient patient = new Patient();
        patient.setName(name);
        patient.setSurname(surname);
        patient.setEmail(email);
        patient.setLogin(login);
        patient.setPassword(passwordHash);
        patient.setRole(UserRole.PATIENT);
        patient.setEmailVerified(false);
        
        UnitOfWorkContext.getCurrent().registerNew(patient);
        
        return patient;
    }

    public Admin createAdmin(Admin admin) {
        UnitOfWorkContext.getCurrent().registerNew(admin);
        return admin;
    }

    public void updateUser(User user) {
        UnitOfWorkContext.getCurrent().registerDirty(user);
    }

    public void deleteUser(User user) {
        UnitOfWorkContext.getCurrent().registerRemoved(user);
    }

    public void updatePatient(Patient patient) {
        UnitOfWorkContext.getCurrent().registerDirty(patient);
    }

    public void deletePatient(Patient patient) {
        UnitOfWorkContext.getCurrent().registerRemoved(patient);
    }

    public void changePassword(int userId, String newPasswordHash) {
        User user = getUserById(userId);
        if (user != null) {
            user.setPassword(newPasswordHash);
            UnitOfWorkContext.getCurrent().registerDirty(user);
        }
    }

    public List<User> searchUsers(String emailPattern) {
        UserRepository userRepo = (UserRepository) UnitOfWorkContext.getRegistry().getRepository(User.class);
        Query query = new Query()
            .where("email", Query.Operator.LIKE, "%" + emailPattern + "%");
        
        return userRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
    }
}
