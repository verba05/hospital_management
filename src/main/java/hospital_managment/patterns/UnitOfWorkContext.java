package hospital_managment.patterns;

import java.sql.Connection;
import java.sql.SQLException;

public class UnitOfWorkContext {
    
    private static final ThreadLocal<UnitOfWork> current = new ThreadLocal<>();
    
    public static void begin(Connection connection) {
        if (current.get() != null) {
            throw new IllegalStateException("UnitOfWork already started in this thread");
        }
        UnitOfWork uow = new UnitOfWork(connection, RepositoryRegistry.getInstance());
        current.set(uow);
    }
    
    public static UnitOfWork getCurrent() {
        UnitOfWork uow = current.get();
        if (uow == null) {
            throw new IllegalStateException("No UnitOfWork in current thread. Call begin() first.");
        }
        return uow;
    }
    
    public static IdentityMap getIdentityMap() {
        return getCurrent().getIdentityMap();
    }
    
    public static RepositoryRegistry getRegistry() {
        return RepositoryRegistry.getInstance();
    }
    
    public static Connection getConnection() {
        return getCurrent().getConnection();
    }
    
    public static void commit() throws SQLException {
        UnitOfWork uow = current.get();
        if (uow != null) {
            uow.commit();
            current.remove();
        }
    }
    
    public static void rollback() {
        UnitOfWork uow = current.get();
        if (uow != null) {
            uow.rollback();
            current.remove();
        }
    }
    
    public static void end() {
        current.remove();
    }
}
