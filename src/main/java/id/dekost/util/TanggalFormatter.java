package id.dekost.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public final class TanggalFormatter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.forLanguageTag("id-ID"));

    private TanggalFormatter() {
    }

    public static String format(LocalDate date) {
        return DATE_FORMAT.format(Objects.requireNonNull(date, "date tidak boleh null"));
    }
}
