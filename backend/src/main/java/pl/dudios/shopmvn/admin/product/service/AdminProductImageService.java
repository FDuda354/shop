package pl.dudios.shopmvn.admin.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.admin.common.utils.SlugifyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AdminProductImageService {

    @Value("${app.uploadDir}")
    private String UPLOAD_DIR;
     // private String UPLOAD_DIR = "./data/productImages/";

    public String uploadImage(String fileName, InputStream inputStream) {
        String newFileName = SlugifyUtils.slugifyFileName(fileName);
        newFileName = ExistingFileRenameUtils.renameFileIfExists(Path.of("/opt/app"+UPLOAD_DIR), newFileName);
        Path filePath = Paths.get("/opt/app"+UPLOAD_DIR).resolve(newFileName);
        try (OutputStream outputStream = Files.newOutputStream(filePath)) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Cant save file", e);
        }
        return newFileName;
    }

    public Resource serveFiles(String fileName) {
        FileSystemResourceLoader resourceLoader = new FileSystemResourceLoader();
        return resourceLoader.getResource(UPLOAD_DIR + fileName);
    }
}
