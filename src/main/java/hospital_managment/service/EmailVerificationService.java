package hospital_managment.service;

import hospital_managment.domain.EmailVerificationToken;
import hospital_managment.domain.User;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.repository.EmailVerificationTokenRepository;
import java.time.LocalDateTime;

public class EmailVerificationService {

    public EmailVerificationToken createToken(User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        
        UnitOfWorkContext.getCurrent().registerNew(token);
        return token;
    }

    public EmailVerificationToken findByToken(String tokenString) {
        EmailVerificationTokenRepository repo = (EmailVerificationTokenRepository) UnitOfWorkContext.getRegistry().getRepository(EmailVerificationToken.class);
        return repo.findByToken(tokenString, UnitOfWorkContext.getConnection());
    }

    public boolean validateAndUseToken(String tokenString) {
        EmailVerificationToken token = findByToken(tokenString);
        
        if (token == null) {
            return false;
        }
        
        if (token.isUsed()) {
            return false;
        }
        
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        EmailVerificationTokenRepository repo = (EmailVerificationTokenRepository) UnitOfWorkContext.getRegistry().getRepository(EmailVerificationToken.class);
        repo.markAsUsed(token, UnitOfWorkContext.getConnection());
        
        return true;
    }
}
