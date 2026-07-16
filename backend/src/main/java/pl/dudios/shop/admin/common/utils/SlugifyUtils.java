package pl.dudios.shop.admin.common.utils;

import com.github.slugify.Slugify;
import org.apache.commons.io.FilenameUtils;

public class SlugifyUtils {

    private SlugifyUtils() {
    }

    private static final Slugify SLUGIFY = Slugify.builder()
            .customReplacement("_", "-")
            .build();

    public static String slugifyFileName(String fileName) {
        String name = FilenameUtils.getBaseName(fileName);
        return SLUGIFY.slugify(name) + "." + FilenameUtils.getExtension(fileName);
    }

    public static String slugifySlug(String slug) {
        return SLUGIFY.slugify(slug);
    }
}
