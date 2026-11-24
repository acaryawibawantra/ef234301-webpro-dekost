package id.dekost.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import id.dekost.dao.UserDao;
import id.dekost.model.User;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class AuthService {

    private final UserDao userDao;

    public AuthService() {
        this(new UserDao());
    }

    public AuthService(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
    }

    public Optional<User> authenticate(String email, String password) throws SQLException {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            return Optional.empty();
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Optional<User> userResult = userDao.findByEmail(normalizedEmail);
        if (userResult.isEmpty()) {
            return Optional.empty();
        }

        User user = userResult.get();
        char[] passwordChars = password.toCharArray();
        boolean verified;
        try {
            verified = BCrypt.verifyer()
                    .verify(passwordChars, user.getPasswordHash())
                    .verified;
        } finally {
            Arrays.fill(passwordChars, '\0');
        }

        if (!verified) {
            return Optional.empty();
        }

        user.setPasswordHash(null);
        return Optional.of(user);
    }
}
