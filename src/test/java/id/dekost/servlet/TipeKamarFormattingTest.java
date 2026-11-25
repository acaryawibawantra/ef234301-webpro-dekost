package id.dekost.servlet;

import id.dekost.model.TipeKamar;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipeKamarFormattingTest {

    @Test
    void buildHargaFormattedMapFormatsPricesAndSkipsNullIds() {
        TipeKamar standar = new TipeKamar(1, "Standar", new BigDecimal("1200000.00"), 9, "Kasur");
        TipeKamar gratis = new TipeKamar(2, "Promo", null, 9, null);
        TipeKamar unsaved = new TipeKamar(null, "Baru", new BigDecimal("500000"), 10, null);

        Map<Integer, String> result = TipeKamarServlet.buildHargaFormattedMap(List.of(standar, gratis, unsaved));

        assertEquals(2, result.size());
        assertEquals("Rp1.200.000", result.get(1));
        assertEquals("Rp0", result.get(2));
        assertTrue(TipeKamarServlet.buildHargaFormattedMap(List.of()).isEmpty());
    }
}
