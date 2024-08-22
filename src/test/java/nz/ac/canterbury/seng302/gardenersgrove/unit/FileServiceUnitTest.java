package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

@SpringBootTest
class FileServiceUnitTest {
    private static FileService fileService;

    @BeforeAll
    static void setup() {
        fileService = spy(new FileService());
    }
    @Test
    void testFileType_FileIsSVG_ReturnsSVG() {
        assertEquals("image/svg+xml", fileService.getImageFileType("example.svg"));
        assertEquals("image/svg+xml", fileService.getImageFileType("example.SVG"));
    }

    @Test
    void testFileType_FileIsPNG_ReturnsPNG() {
        assertEquals("image/png", fileService.getImageFileType("example.png"));
        assertEquals("image/png", fileService.getImageFileType("example.PNG"));
    }

    @Test
    void testFileType_FileIsJPG_ReturnsJPEG() {
        assertEquals("image/jpeg", fileService.getImageFileType("example.jpg"));
        assertEquals("image/jpeg", fileService.getImageFileType("example.JPG"));
    }

    @Test
    void testFileType_FileIsJPEG_ReturnsJPEG() {
        assertEquals("image/jpeg", fileService.getImageFileType("example.jpeg"));
        assertEquals("image/jpeg", fileService.getImageFileType("example.JPEG"));
    }

    @Test
    void testFileType_FileIsInvalid_ReturnsUnknown() {
        assertEquals("unknown", fileService.getImageFileType("example.txt"));
        assertEquals("unknown", fileService.getImageFileType("example.docx"));
        assertEquals("unknown", fileService.getImageFileType("example"));
        assertEquals("unknown", fileService.getImageFileType("SVGInName.butNotExtension"));
        assertEquals("unknown", fileService.getImageFileType("PNGInName.butNotExtension"));
        assertEquals("unknown", fileService.getImageFileType("JPGInName.butNotExtension"));
        assertEquals("unknown", fileService.getImageFileType("JPEGInName.butNotExtension"));
        assertEquals("unknown", fileService.getImageFileType("SVGNotFullExtension.SV"));
        assertEquals("unknown", fileService.getImageFileType("SVGNotFullExtension.VG"));
        assertEquals("unknown", fileService.getImageFileType("PNGNotFullExtension.P"));
        assertEquals("unknown", fileService.getImageFileType("PNGNotFullExtension.PN"));
        assertEquals("unknown", fileService.getImageFileType("PNGNotFullExtension.NG"));
        assertEquals("unknown", fileService.getImageFileType("JPGNotFullExtension.JP"));
        assertEquals("unknown", fileService.getImageFileType("JPGNotFullExtension.PG"));
        assertEquals("unknown", fileService.getImageFileType("JPEGNotFullExtension.PEG"));
    }
}
