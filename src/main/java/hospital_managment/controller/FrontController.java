package hospital_managment.controller;

import io.github.cdimascio.dotenv.Dotenv;
import hospital_managment.patterns.RepositoryRegistry;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.repository.*;
import hospital_managment.security.AuthContext;
import hospital_managment.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebServlet("/api/*")
public class FrontController extends HttpServlet {
    private UserRepository userRepo;
    private PatientRepository patientRepo;
    private DoctorRepository doctorRepo;
    private AdminRepository adminRepo;
    private HospitalRepository hospitalRepo;
    private AppointmentRepository appointmentRepo;
    private TreatmentRepository treatmentRepo;
    private EmailVerificationTokenRepository tokenRepo;
    
    private AuthController authController;
    private UserController userController;
    private DoctorController doctorController;
    private HospitalController hospitalController;
    private AppointmentController appointmentController;
    private TreatmentController treatmentController;
    
    private String jdbcUrl;
    private String dbUsername;
    private String dbPassword;

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ServletException("PostgreSQL JDBC Driver not found", e);
        }
        
        Dotenv dotenv = Dotenv.load();
        jdbcUrl = dotenv.get("SUPABASE_URL");
        dbUsername = dotenv.get("SUPABASE_USERNAME");
        dbPassword = dotenv.get("SUPABASE_PASSWORD");

        if (jdbcUrl == null || dbUsername == null || dbPassword == null) {
            throw new ServletException("Database configuration missing in .env file");
        }

        userRepo = new UserRepository();
        patientRepo = new PatientRepository();
        doctorRepo = new DoctorRepository();
        adminRepo = new AdminRepository();
        hospitalRepo = new HospitalRepository();
        appointmentRepo = new AppointmentRepository();
        treatmentRepo = new TreatmentRepository();
        tokenRepo = new EmailVerificationTokenRepository();
        
        RepositoryRegistry.getInstance().register(userRepo);
        RepositoryRegistry.getInstance().register(patientRepo);
        RepositoryRegistry.getInstance().register(doctorRepo);
        RepositoryRegistry.getInstance().register(adminRepo);
        RepositoryRegistry.getInstance().register(hospitalRepo);
        RepositoryRegistry.getInstance().register(appointmentRepo);
        RepositoryRegistry.getInstance().register(treatmentRepo);
        RepositoryRegistry.getInstance().register(tokenRepo);
        
        authController = new AuthController();
        userController = new UserController();
        doctorController = new DoctorController();
        hospitalController = new HospitalController();
        appointmentController = new AppointmentController();
        treatmentController = new TreatmentController();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        Connection connection = null;
        
        try {
            connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
            connection.setAutoCommit(false);
            UnitOfWorkContext.begin(connection);

            String token = AuthContext.extractToken(request);
            if (token != null) {
                Claims claims = JwtUtil.validateToken(token);
                if (claims != null) {
                    int userId = Integer.parseInt(claims.getSubject());
                    String role = claims.get("role", String.class);
                    AuthContext.setCurrentUser(userId, role);
                }
            }

            String pathInfo = request.getPathInfo();
            String method = request.getMethod();

            routeRequest(pathInfo, method, request, response);

            try {
                UnitOfWorkContext.commit();
            } catch (IllegalStateException e) {
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        } catch (Exception e) {
            try {
                UnitOfWorkContext.rollback();
            } catch (Exception rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw new ServletException("Request processing failed", e);
        } finally {
            AuthContext.clear();
            UnitOfWorkContext.end();
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new ServletException("Failed to close connection", e);
                }
            }
        }
    }

    private void routeRequest(String pathInfo, String method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (pathInfo == null || pathInfo.equals("/")) {
            response.getWriter().write("{\"message\":\"Hospital Management API\"}");
            return;
        }

        String[] pathParts = pathInfo.substring(1).split("/");
        String resource = pathParts[0];

        switch (resource) {
            case "login":
                if ("POST".equals(method)) {
                    authController.login(request, response);
                }
                break;

            case "register":
                if ("POST".equals(method)) {
                    authController.register(request, response);
                }
                break;

            case "verify-email":
                if ("GET".equals(method)) {
                    authController.verifyEmail(request, response);
                }
                break;

            case "admins":
                if ("GET".equals(method) && pathParts.length > 1 && "hospital".equals(pathParts[1])) {
                    hospitalController.getAdminHospital(request, response);
                }
                break;

            case "users":
                if ("GET".equals(method) && pathParts.length > 1) {
                    int userId = Integer.parseInt(pathParts[1]);
                    userController.getUser(request, response, userId);
                }
                break;

            case "patients":
                if ("GET".equals(method) && pathParts.length > 1) {
                    int patientId = Integer.parseInt(pathParts[1]);
                    userController.getPatient(request, response, patientId);
                } else if ("PUT".equals(method) && pathParts.length > 1) {
                    int patientId = Integer.parseInt(pathParts[1]);
                    userController.updatePatient(request, response, patientId);
                } else if ("DELETE".equals(method) && pathParts.length > 1) {
                    int patientId = Integer.parseInt(pathParts[1]);
                    userController.deletePatient(request, response, patientId);
                } else if ("POST".equals(method) && pathParts.length > 1 && "change-password".equals(pathParts[2])) {
                    int patientId = Integer.parseInt(pathParts[1]);
                    userController.changePassword(request, response, patientId);
                }
                break;

            case "hospitals":
                if ("GET".equals(method) && pathParts.length == 1) {
                    hospitalController.searchHospitals(request, response);
                } else if ("GET".equals(method) && pathParts.length > 1) {
                    int hospitalId = Integer.parseInt(pathParts[1]);
                    hospitalController.getHospital(request, response, hospitalId);
                } else if ("PUT".equals(method) && pathParts.length > 1) {
                    int hospitalId = Integer.parseInt(pathParts[1]);
                    hospitalController.updateHospital(request, response, hospitalId);
                }
                break;

            case "doctors":
                if ("GET".equals(method) && pathParts.length == 1) {
                    String hospitalIdParam = request.getParameter("hospitalId");
                    if (hospitalIdParam != null) {
                        int hospitalId = Integer.parseInt(hospitalIdParam);
                        doctorController.getDoctorsByHospital(request, response, hospitalId);
                    }
                } else if ("GET".equals(method) && pathParts.length == 2 && "specialties".equals(pathParts[1])) {
                    doctorController.getSpecialties(request, response);
                } else if ("GET".equals(method) && pathParts.length == 3 && "me".equals(pathParts[1]) && "schedule".equals(pathParts[2])) {
                    doctorController.getCurrentDoctorSchedule(request, response);
                } else if ("GET".equals(method) && pathParts.length > 1) {
                    int doctorId = Integer.parseInt(pathParts[1]);
                    if (pathParts.length > 2 && "appointments".equals(pathParts[2])) {
                        doctorController.getDoctorAppointments(request, response, doctorId);
                    } else if (pathParts.length > 2 && "appointment-slots".equals(pathParts[2])) {
                        doctorController.getAvailableAppointmentSlots(request, response, doctorId);
                    } else if (pathParts.length > 2 && "schedule".equals(pathParts[2])) {
                        doctorController.getDoctorSchedule(request, response, doctorId);
                    } else {
                        doctorController.getDoctor(request, response, doctorId);
                    }
                } else if ("POST".equals(method) && pathParts.length == 1) {
                    doctorController.createDoctor(request, response);
                } else if ("PUT".equals(method) && pathParts.length > 1) {
                    int doctorId = Integer.parseInt(pathParts[1]);
                    doctorController.updateDoctor(request, response, doctorId);
                } else if ("DELETE".equals(method) && pathParts.length > 1) {
                    int doctorId = Integer.parseInt(pathParts[1]);
                    doctorController.deleteDoctor(request, response, doctorId);
                }
                break;

            case "appointments":
                if ("GET".equals(method) && pathParts.length == 1) {
                    String patientIdParam = request.getParameter("patientId");
                    String doctorIdParam = request.getParameter("doctorId");
                    
                    if (patientIdParam != null && doctorIdParam != null) {
                        int patientId = Integer.parseInt(patientIdParam);
                        int doctorId = Integer.parseInt(doctorIdParam);
                        appointmentController.getAppointmentsByPatientAndDoctor(request, response, patientId, doctorId);
                    } else if (patientIdParam != null) {
                        int patientId = Integer.parseInt(patientIdParam);
                        appointmentController.getAppointmentsByPatient(request, response, patientId);
                    } else if (doctorIdParam != null) {
                        int doctorId = Integer.parseInt(doctorIdParam);
                        appointmentController.getAppointmentsByDoctor(request, response, doctorId);
                    }
                } else if ("GET".equals(method) && pathParts.length > 1) {
                    int appointmentId = Integer.parseInt(pathParts[1]);
                    appointmentController.getAppointment(request, response, appointmentId);
                } else if ("POST".equals(method)) {
                    appointmentController.createAppointment(request, response);
                } else if ("PUT".equals(method) && pathParts.length > 1) {
                    int appointmentId = Integer.parseInt(pathParts[1]);
                    if (pathParts.length > 2 && "status".equals(pathParts[2])) {
                        appointmentController.updateAppointmentStatus(request, response, appointmentId);
                    } else if (pathParts.length > 2 && "cancel".equals(pathParts[2])) {
                        appointmentController.cancelAppointment(request, response, appointmentId);
                    } else {
                        appointmentController.updateAppointment(request, response, appointmentId);
                    }
                } else if ("DELETE".equals(method) && pathParts.length > 1) {
                    int appointmentId = Integer.parseInt(pathParts[1]);
                    appointmentController.deleteAppointment(request, response, appointmentId);
                }
                break;

            case "treatments":
                if ("GET".equals(method)) {
                    String patientIdParam = request.getParameter("patientId");
                    String doctorIdParam = request.getParameter("doctorId");
                    
                    if (patientIdParam != null) {
                        int patientId = Integer.parseInt(patientIdParam);
                        treatmentController.getTreatmentsByPatient(request, response, patientId);
                    } else if (doctorIdParam != null) {
                        int doctorId = Integer.parseInt(doctorIdParam);
                        treatmentController.getTreatmentsByDoctor(request, response, doctorId);
                    }
                } else if ("POST".equals(method)) {
                    treatmentController.createTreatment(request, response);
                } else if ("PUT".equals(method) && pathParts.length > 1) {
                    int treatmentId = Integer.parseInt(pathParts[1]);
                    treatmentController.updateTreatment(request, response, treatmentId);
                } else if ("DELETE".equals(method) && pathParts.length > 1) {
                    int treatmentId = Integer.parseInt(pathParts[1]);
                    treatmentController.deleteTreatment(request, response, treatmentId);
                }
                break;

            default:
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Resource not found\"}");
        }
    }
}
