package hospital_managment.security;

import jakarta.servlet.http.HttpServletRequest;

public class AuthContext {
    private static final ThreadLocal<Integer> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserRole = new ThreadLocal<>();

    public static void setCurrentUser(int userId, String role) {
        currentUserId.set(userId);
        currentUserRole.set(role);
    }

    public static Integer getCurrentUserId() {
        return currentUserId.get();
    }

    public static String getCurrentUserRole() {
        return currentUserRole.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentUserRole.remove();
    }

    public static String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
