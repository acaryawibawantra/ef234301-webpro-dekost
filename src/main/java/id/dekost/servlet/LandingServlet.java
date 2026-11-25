package id.dekost.servlet;

import id.dekost.dao.KamarDao;
import id.dekost.util.RupiahFormatter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "LandingServlet", urlPatterns = "")
public class LandingServlet extends HttpServlet {

    private final KamarDao kamarDao = new KamarDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<KamarDao.TypeAvailability> typeAvailability = List.of();
        long totalRooms = 0;
        long availableRooms = 0;
        long occupiedRooms = 0;
        BigDecimal startingPrice = null;

        try {
            typeAvailability = kamarDao.findAvailabilityByType();
        } catch (SQLException e) {
            getServletContext().log("Gagal memuat ketersediaan kamar untuk landing page.", e);
        }

        Map<Integer, String> typeAvailabilityPrices = new LinkedHashMap<>();
        for (KamarDao.TypeAvailability type : typeAvailability) {
            totalRooms += type.getTotalKamar();
            availableRooms += type.getKamarTersedia();
            occupiedRooms += type.getKamarTerisi();
            typeAvailabilityPrices.put(type.getTipeKamarId(),
                    RupiahFormatter.format(type.getHargaBulanan() == null
                            ? BigDecimal.ZERO
                            : type.getHargaBulanan()));
            if (type.getHargaBulanan() != null
                    && (startingPrice == null || type.getHargaBulanan().compareTo(startingPrice) < 0)) {
                startingPrice = type.getHargaBulanan();
            }
        }

        int occupancyPercent = totalRooms > 0
                ? BigDecimal.valueOf(occupiedRooms * 100L)
                        .divide(BigDecimal.valueOf(totalRooms), 0, RoundingMode.HALF_UP)
                        .intValue()
                : 0;

        request.setAttribute("typeAvailability", typeAvailability);
        request.setAttribute("typeAvailabilityPrices", typeAvailabilityPrices);
        request.setAttribute("totalRooms", totalRooms);
        request.setAttribute("availableRooms", availableRooms);
        request.setAttribute("occupancyPercent", occupancyPercent);
        request.setAttribute("startingPriceCompact",
                RupiahFormatter.formatCompact(startingPrice == null ? BigDecimal.ZERO : startingPrice));

        request.getRequestDispatcher("/WEB-INF/views/public/landing.jsp").forward(request, response);
    }
}
