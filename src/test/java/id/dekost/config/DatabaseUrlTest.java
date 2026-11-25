package id.dekost.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseUrlTest {

    @Test
    void keepsJdbcUrlUnchanged() {
        String url = "jdbc:postgresql://localhost:5432/dekost";

        assertEquals(url, Database.normalizeJdbcUrl(url));
    }

    @Test
    void convertsRenderConnectionStringAndRemovesEmbeddedCredentials() {
        String url = "postgres://service:secret@db.example.com:5433/dekost?sslmode=require";

        assertEquals(
                "jdbc:postgresql://db.example.com:5433/dekost?sslmode=require",
                Database.normalizeJdbcUrl(url)
        );
    }

    @Test
    void rejectsUnsupportedUrlsWithoutReturningTheirContents() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> Database.normalizeJdbcUrl("mysql://user:secret@db.example.com/dekost")
        );

        assertTrue(exception.getMessage().contains("PostgreSQL"));
        assertFalse(exception.getMessage().contains("secret"));
        assertNull(exception.getCause());
    }

    @Test
    void doesNotExposeMalformedUrlDetails() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> Database.normalizeJdbcUrl("postgres://user:secret@db host/dekost")
        );

        assertNull(exception.getCause());
    }
}
