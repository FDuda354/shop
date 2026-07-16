package pl.dudios.shop.admin.product.service;

import org.apache.commons.io.FilenameUtils;

import java.util.function.Predicate;

class ExistingFileRenameUtils {

    private ExistingFileRenameUtils() {
    }

    /**
     * Dokleja (lub inkrementuje) sufiks "-N" tak długo, aż nazwa przestanie
     * kolidować z już istniejącą — kolizje zgłasza przekazany predykat,
     * więc logika nie zależy od tego, gdzie obrazy faktycznie leżą.
     */
    public static String renameFileIfExists(Predicate<String> nameExists, String fileName) {
        String candidate = fileName;
        while (nameExists.test(candidate)) {
            candidate = renameFileName(candidate);
        }
        return candidate;
    }

    private static String renameFileName(String fileName) {
        String name = FilenameUtils.getBaseName(fileName);
        String[] split = name.split("-(?=[0-9]+$)");
        int counter = split.length > 1 ? Integer.parseInt(split[1]) + 1 : 1;
        return split[0] + "-" + counter + "." + FilenameUtils.getExtension(fileName);
    }

}
