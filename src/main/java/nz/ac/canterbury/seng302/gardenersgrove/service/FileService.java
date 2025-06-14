package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.slf4j.LoggerFactory;
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
import java.util.stream.Stream;

import org.slf4j.Logger;

/**
 * Service class for handling file operations such as saving, loading, and
 * deleting files
 */
@Service
public class FileService {
    Logger logger = LoggerFactory.getLogger(FileService.class);

    private final Path rootLocation;

    /**
     * Constructor for FileService which initialises the rootLocation directory
     */
    public FileService() {
        String uploadDir = "./uploads";
        this.rootLocation = Paths.get(uploadDir);
    }

    /**
     * Creates a Resource object from the given file path
     *
     * @param file Path to the file
     * @return Resource object representing the file
     * @throws MalformedURLException exception when cannot turn file into a resource
     */
    public Resource createResource(Path file) throws MalformedURLException {
        try {
            return new UrlResource(file.toUri());
        } catch (MalformedURLException error) {
            throw new MalformedURLException("Could not turn file into a resource");
        }
    }

    /**
     * Returns the rootLocation directory as a Path
     *
     * @return rootLocation directory
     */
    public Path getRootLocation() {
        return rootLocation;
    }

    /**
     * Loads a file with the given fileName from the rootLocation directory
     *
     * @param fileName Name of the file to load
     * @return Resource object representing the file
     * @throws MalformedURLException if the file cannot be read
     */
    public Resource loadFile(String fileName) throws MalformedURLException {
        try {
            Path file = getRootLocation().resolve(fileName);
            Resource resource = createResource(file);
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new MalformedURLException("Could not read the file");
            }
        } catch (MalformedURLException error) {
            throw new MalformedURLException("Could not read the file");
        }
    }

    /**
     * Saves a file with the given fileName to the rootLocation directory
     *
     * @param fileName Name to save the file as
     * @param file     The file to save
     * @throws IOException exception when could not store the file
     */
    public void saveFile(String fileName, MultipartFile file) throws IOException {
        try {
            Path destinationFile = getRootLocation().resolve(Paths.get(fileName)).normalize().toAbsolutePath();
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            throw new IOException("Could not store the file");
        }
    }

    /**
     * Saves a file with the given fileName to the rootLocation directory
     * Overloaded method to copy pictures
     *
     * @param newFile      Name to save the file as
     * @param originalFile Name of the file to save
     * @throws IOException exception when could not store the file
     */
    public void saveFile(String newFile, String originalFile) throws IOException {
        try {
            Path destinationFile = getRootLocation().resolve(Paths.get(newFile)).normalize().toAbsolutePath();
            Path toCopy = getRootLocation().resolve(Paths.get(originalFile)).normalize().toAbsolutePath();
            Files.copy(toCopy, destinationFile, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException error) {
            logger.error(error.toString());
            throw new IOException("Could not store the file");
        }
    }

    /**
     * Initializes the storage directory by creating the root directory if it does
     * not exist
     *
     * @throws IOException if the directory cannot be created
     */
    public void init() throws IOException {
        try {
            Files.createDirectories(getRootLocation());
        } catch (IOException error) {
            throw new IOException("Could not initialize storage");
        }
    }

    /**
     * Returns a list of all files in the rootLocation directory
     *
     * @return String[] list of all files in the rootLocation directory
     * @throws IOException if the files cannot read from the file system
     */
    public String[] getAllFiles() throws IOException {
        try (Stream<Path> paths = Files.walk(getRootLocation(), 1)) {
            return paths
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toArray(String[]::new);
        } catch (IOException error) {
            throw new IOException("Could not list the files");
        }
    }

    /**
     * Deletes a file with the given fileName from the rootLocation directory
     *
     * @param fileName name of file to delete
     * @throws IOException if the file cannot be deleted
     */
    public void deleteFile(String fileName) throws IOException {
        try {
            Path file = getRootLocation().resolve(Paths.get(fileName)).normalize().toAbsolutePath();
            Files.delete(file);
        } catch (IOException error) {
            logger.error(error.getMessage());
            throw new IOException("Could not delete the file");
        }
    }

    /**
     * Takes String of an image filename and returns what extension type is in
     * header format.
     * 
     * @param filename Name of File
     * @return String of image type in the format image/*extentiontype*. Used for
     *         content type headers.
     */
    public String getImageFileType(String filename) {
        if (filename.endsWith(".svg") || filename.endsWith(".SVG")) {
            return "image/svg+xml";
        } else if (filename.endsWith(".png") || filename.endsWith(".PNG")) {
            return "image/png";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".JPG") || filename.endsWith(".jpeg")
                || filename.endsWith(".JPEG")) {
            return "image/jpeg";
        } else {
            return "unknown";
        }
    }
}
