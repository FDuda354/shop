package pl.dudios.shop.admin.common.utils;

import com.github.slugify.Slugify;
import org.apache.commons.io.FilenameUtils;

import java.util.Locale;

public class SlugifyUtils {

    private SlugifyUtils() {
    }

    private static final Slugify SLUGIFY = Slugify.builder()
            .customReplacement("_", "-")
            .build();

    public static String slugifyFileName(String fileName) {
        String name = FilenameUtils.getBaseName(fileName);
        return SLUGIFY.slugify(name) + "." + FilenameUtils.getExtension(fileName).toLowerCase(Locale.ROOT);
    }

    public static String slugifySlug(String slug) {
        return SLUGIFY.slugify(slug);
    }
}
