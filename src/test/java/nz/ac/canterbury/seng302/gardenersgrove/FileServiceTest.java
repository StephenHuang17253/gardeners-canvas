package nz.ac.canterbury.seng302.gardenersgrove;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;

public class FileServiceTest {

    private Path tempDir;

    private static FileService fileService;
    public static String[] mockFilenames = { "test1.img", "test2.txt", "test3.pdf" };
    public static String[] mockFileContents = { "Hello, World!", "ABCDEF", "Content" };

    @BeforeAll
    public static void setup() throws IOException {
        fileService = spy(new FileService());
    }

    /**
     * Initialises the given number of files in tempDir from the mock lists
     * 
     * @param numFiles number of files from mock lists to initialise
     */
    public void initDir(int numFiles) throws IOException {
        for (int i = 0; i < numFiles; i++) {
            Path testFile = tempDir.resolve(mockFilenames[i]);
            Files.write(testFile, mockFileContents[i].getBytes());
        }
    }

    @BeforeEach
    public void setupDirectory(@TempDir Path tempDir) throws IOException {
        this.tempDir = tempDir;

        // Reset root location to tempDir
        when(fileService.getRootLocation()).thenReturn(tempDir);
    }

    @Test
    public void testLoadFile_FileExists_ReturnsResource() throws IOException {
        int i = 0;

        initDir(1);

        Resource resource = fileService.loadFile(mockFilenames[i]);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(mockFilenames[i], resource.getFilename());
        assertEquals(mockFileContents[i], new String(resource.getInputStream().readAllBytes()));
    }

    @Test
    public void testLoadFile_FileDoesNotExist_ThrowsMalformedURLException() {

        Exception exception = assertThrows(MalformedURLException.class, () -> {
            fileService.loadFile("nonExistentFile.txt");
        });
        assertEquals("Could not read the file", exception.getMessage());
    }

    @Test
    public void testLoadFile_FileNotReadable_ThrowsMalformedURLException() throws IOException {

        String nonReadableFilename = "nonReadableFile.txt";

        Path nonReadableFile = tempDir.resolve(nonReadableFilename);
        Files.createFile(nonReadableFile);
        nonReadableFile.toFile().setReadable(false);

        Exception exception = assertThrows(MalformedURLException.class, () -> {
            fileService.loadFile(nonReadableFilename);
        });
        assertEquals("Could not read the file", exception.getMessage());
    }

    @Test
    public void testSaveFile_ValidFile_FileSavedSuccessfully() throws IOException {
        int i = 0;
        MultipartFile file = new MockMultipartFile(mockFilenames[i], mockFileContents[i].getBytes());

        fileService.saveFile(mockFilenames[i], file);

        Path savedFile = tempDir.resolve(mockFilenames[i]);
        assertTrue(Files.exists(savedFile));
    }

    @Test
    public void testSaveFile_InvalidFile_ThrowsIOException() throws IOException {
        String invalidFileName = "invalidFile.txt";

        MultipartFile invalidFile = mock(MultipartFile.class);
        when(invalidFile.getInputStream()).thenThrow(IOException.class);

        Exception exception = assertThrows(IOException.class, () -> {
            fileService.saveFile(invalidFileName, invalidFile);
        });

        assertEquals("Could not store the file", exception.getMessage());
    }

    @Test
    public void testInit_DirectoryDoesNotExist_DirectoryCreatedSuccessfully() throws IOException {
        Path nonExistentDir = tempDir.resolve("nonExistentDir");
        when(fileService.getRootLocation()).thenReturn(nonExistentDir);

        Files.deleteIfExists(nonExistentDir);

        fileService.init();

        assertTrue(Files.exists(nonExistentDir));
    }

    @Test
    public void testInit_DirectoryExists_NoExceptionThrown() throws IOException {
        Path existingDir = tempDir.resolve("existingDir");
        when(fileService.getRootLocation()).thenReturn(existingDir);

        Files.createDirectories(existingDir);

        fileService.init();

        assertTrue(Files.exists(existingDir));
    }

    @Test
    public void testGetAllFiles_DirectoryHasFiles_ReturnsListOfFiles() throws IOException {

        initDir(3);

        String[] actualFiles = fileService.getAllFiles();

        // Junit creates a config file in the dir by default so expect 1 file more
        assertEquals(mockFilenames.length + 1, actualFiles.length);
    }

    @Test
    public void testGetAllFiles_DirectoryIsEmpty_ReturnsEmptyList() throws Exception {

        initDir(0);

        String[] actualFiles = fileService.getAllFiles();

        // Junit creates a config file in the dir by default so expect 1 file
        assertEquals(1, actualFiles.length);
    }

    @Test
    public void testDeleteFile_FileExists_FileDeletedSuccessfully() throws Exception {
        int i = 0;

        initDir(1);

        fileService.deleteFile(mockFilenames[i]);

        Path testFile = tempDir.resolve(mockFilenames[i]);
        assertFalse(Files.exists(testFile));
    }

    @Test
    public void testDeleteFile_FileDoesNotExist_ThrowsIOException() throws Exception {
        String nonExistentFileName = "nonExistentFile.txt";

        Exception exception = assertThrows(IOException.class, () -> {
            fileService.deleteFile(nonExistentFileName);
        });
        assertEquals("Could not delete the file", exception.getMessage());
    }
}
