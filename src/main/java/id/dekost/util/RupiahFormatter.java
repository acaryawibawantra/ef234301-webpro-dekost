package id.dekost.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

public final class RupiahFormatter {

    private RupiahFormatter() {
    }

    public static String format(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount tidak boleh null");
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("id-ID"));
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        formatter.setParseBigDecimal(true);
        return "Rp" + formatter.format(amount);
    }
}
