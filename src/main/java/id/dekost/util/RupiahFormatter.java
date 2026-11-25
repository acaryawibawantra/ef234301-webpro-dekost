package id.dekost.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

public final class RupiahFormatter {

    private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");
    private static final BigDecimal ONE_MILLION = BigDecimal.valueOf(1_000_000L);

    private RupiahFormatter() {
    }

    public static String format(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount tidak boleh null");
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(ID_LOCALE);
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        formatter.setParseBigDecimal(true);
        return "Rp" + formatter.format(amount);
    }

    public static String formatCompact(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return format(BigDecimal.ZERO);
        }
        if (amount.compareTo(ONE_MILLION) < 0) {
            return format(amount);
        }
        BigDecimal millions = amount.divide(ONE_MILLION, 1, RoundingMode.HALF_UP);
        String formatted = String.format(ID_LOCALE, "%.1f", millions);
        if (formatted.endsWith(",0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
        }
        return "Rp " + formatted + " jt";
    }
}
