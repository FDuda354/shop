package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Seed obrazków produktów wprost do bazy (bytea). Migracja javowa, bo Flyway
 * nie wstawi zawartości binarnej z poziomu SQL-a; pliki źródłowe leżą na
 * classpathcie w db/seed/product-images/.
 */
public class V3__SeedProductImages extends BaseJavaMigration {

    private static final String SEED_DIR = "db/seed/product-images/";
    private static final List<String> IMAGES = List.of(
            "apple.gif", "chips.gif", "duck.gif",
            "turkey.gif", "pizza.gif", "wather.gif", "avatar.gif"
    );

    @Override
    public void migrate(Context context) throws Exception {
        try (PreparedStatement statement = context.getConnection().prepareStatement(
                "INSERT INTO product_images (name, content_type, data) VALUES (?, ?, ?)")) {
            for (String name : IMAGES) {
                statement.setString(1, name);
                statement.setString(2, "image/gif");
                statement.setBytes(3, readSeedImage(name));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private byte[] readSeedImage(String name) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(SEED_DIR + name)) {
            if (in == null) {
                throw new IllegalStateException("Missing seed image on classpath: " + SEED_DIR + name);
            }
            return in.readAllBytes();
        }
    }
}
