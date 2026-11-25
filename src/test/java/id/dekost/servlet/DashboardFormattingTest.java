package id.dekost.servlet;

import id.dekost.util.RupiahFormatter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardFormattingTest {

    @Test
    void formatCompactHandlesNullZeroBelowMillionAndMillions() {
        assertEquals("Rp0", RupiahFormatter.formatCompact(null));
        assertEquals("Rp0", RupiahFormatter.formatCompact(BigDecimal.ZERO));
        assertEquals("Rp500.000", RupiahFormatter.formatCompact(BigDecimal.valueOf(500_000)));
        assertEquals("Rp 1 jt", RupiahFormatter.formatCompact(BigDecimal.valueOf(1_000_000)));
        assertEquals("Rp 1,2 jt", RupiahFormatter.formatCompact(BigDecimal.valueOf(1_200_000)));
        assertEquals("Rp 25,4 jt", RupiahFormatter.formatCompact(BigDecimal.valueOf(25_400_000)));
    }

    @Test
    void extractInitialsHandlesStandardEdgePunctuationNbspAndEmojiNames() {
        assertEquals("?", DashboardServlet.extractInitials(null));
        assertEquals("?", DashboardServlet.extractInitials("   "));
        assertEquals("?", DashboardServlet.extractInitials("\u00A0"));
        assertEquals("?", DashboardServlet.extractInitials("-"));
        assertEquals("?", DashboardServlet.extractInitials("---"));
        assertEquals("?", DashboardServlet.extractInitials("."));
        assertEquals("?", DashboardServlet.extractInitials("😀"));
        assertEquals("BU", DashboardServlet.extractInitials("Budi"));
        assertEquals("BS", DashboardServlet.extractInitials("Budi Santoso"));
        assertEquals("BS", DashboardServlet.extractInitials("Budi (VIP) Santoso"));
        assertEquals("BU", DashboardServlet.extractInitials("(Budi)"));
        assertEquals("BU", DashboardServlet.extractInitials("Budi -"));
        assertEquals("BS", DashboardServlet.extractInitials("Budi\u00A0Santoso"));
    }

    @Test
    void buildGreetingForHourUsesIndonesianTimeOfDay() {
        assertEquals("Selamat pagi, Pengelola", DashboardServlet.buildGreetingForHour("Pengelola Kos", 8));
        assertEquals("Selamat siang, Pengelola", DashboardServlet.buildGreetingForHour(null, 12));
        assertEquals("Selamat sore, Atma", DashboardServlet.buildGreetingForHour("Atma", 16));
        assertEquals("Selamat malam, Pengelola", DashboardServlet.buildGreetingForHour("", 22));
    }
}
