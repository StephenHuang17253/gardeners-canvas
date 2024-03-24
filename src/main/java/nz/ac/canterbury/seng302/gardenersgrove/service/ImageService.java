package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;

@Service
public class ImageService {
    Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final Path rootLocation;

    @Autowired
    public ImageService() {

        String uploadDir = "./uploads";

        this.rootLocation = Paths.get(uploadDir);
    }

    public Resource loadImage(String fileName) throws Exception {
        try {
            Path file = rootLocation.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new Exception("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new Exception("Error: ", e);
        }
    }

    public void saveImage(String fileName, MultipartFile imageFile) throws Exception {
        try {
            Path destinationFile = this.rootLocation.resolve(Paths.get(fileName)).normalize().toAbsolutePath();
            Files.copy(imageFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception("Could not store the file. Error: " + e.getMessage());
        }
    }

    public void init() throws Exception {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new Exception("Could not initialize storage", e);
        }

    }

    public String[] getAllImages() throws Exception {
        try {
            return Files.walk(rootLocation, 1)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toArray(String[]::new);
        } catch (IOException e) {
            throw new Exception("Could not list the files. Error: " + e.getMessage());
        }
    }

    public void deleteImage(String fileName) throws Exception {
        try {
            Path file = rootLocation.resolve(fileName);
            Files.delete(file);
        } catch (IOException e) {
            throw new Exception("Could not delete the file. Error: " + e.getMessage());
        }
    }

}
