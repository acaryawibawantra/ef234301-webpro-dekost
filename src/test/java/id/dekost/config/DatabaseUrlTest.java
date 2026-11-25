package id.dekost.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseUrlTest {

    @Test
    void keepsJdbcUrlUnchanged() {
        String url = "jdbc:postgresql://localhost:5432/dekost";

        assertEquals(url, Database.normalizeJdbcUrl(url));
    }

    @Test
    void convertsRailwayConnectionUrlAndRemovesEmbeddedCredentials() {
        String url = "postgresql://service:secret@db.example.com:5433/dekost?sslmode=require";

        assertEquals(
                "jdbc:postgresql://db.example.com:5433/dekost?sslmode=require",
                Database.normalizeJdbcUrl(url)
        );
    }

    @Test
    void rejectsUnsupportedUrlSchemesWithoutExposingConnectionDetails() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> Database.normalizeJdbcUrl("mysql://user:secret@db.example.com/dekost")
        );

        assertEquals(
                "DB_URL must be a PostgreSQL JDBC URL or PostgreSQL connection URL.",
                exception.getMessage()
        );
        assertNull(exception.getCause());
    }
}
