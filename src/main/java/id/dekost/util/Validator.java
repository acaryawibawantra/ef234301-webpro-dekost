package id.dekost.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{9,15}$");

    private Validator() {
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidPhoneNumber(String value) {
        return value != null && PHONE_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isPositiveInteger(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeAmount(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isValidDateRange(LocalDate start, LocalDate end) {
        return start != null && (end == null || !end.isBefore(start));
    }
}
