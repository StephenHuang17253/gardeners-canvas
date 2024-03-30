package nz.ac.canterbury.seng302.gardenersgrove;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;

/**
 * Gardener's Grove entry-point
 * Note {@link SpringBootApplication} annotation
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class GardenersGroveApplication {
	/**
	 * Main entry point, runs the Spring application
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(GardenersGroveApplication.class, args);
	}

	/**
	 * Initialises the file service for storing images
	 * 
	 * @param fileService
	 * @return
	 */
	@Bean
	CommandLineRunner init(FileService fileService) {
		return (args) -> {
			// imageService.deleteAll();
			fileService.init();
		};
	}

}
