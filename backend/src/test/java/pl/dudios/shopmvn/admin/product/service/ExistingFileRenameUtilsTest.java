package pl.dudios.shopmvn.admin.product.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExistingFileRenameUtilsTest {

    @Test
    void shouldNotRenameFile(@TempDir Path tempDir) {
        String fileName = "test.png";
        String newFileName = ExistingFileRenameUtils.renameFileIfExists(tempDir, fileName);
        assertEquals(fileName, newFileName);

    }

    @Test
    void shouldRenameFile(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("test.png"));
        String newName = ExistingFileRenameUtils.renameFileIfExists(tempDir, "test.png");
        assertEquals("test-1.png", newName);
    }


    @Test
    void shouldRenameManyFile(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("test.png"));
        Files.createFile(tempDir.resolve("test-1.png"));
        Files.createFile(tempDir.resolve("test-2.png"));
        Files.createFile(tempDir.resolve("test-3.png"));
        String newName = ExistingFileRenameUtils.renameFileIfExists(tempDir, "test.png");
        assertEquals("test-4.png", newName);
    }




}