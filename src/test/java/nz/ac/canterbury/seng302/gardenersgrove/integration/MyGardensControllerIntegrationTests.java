package nz.ac.canterbury.seng302.gardenersgrove.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class MyGardensControllerIntegrationTests {
    private final MockMvc mockMvc;

    @Autowired
    public MyGardensControllerIntegrationTests(MockMvc mockMvc){
        this.mockMvc = mockMvc;
    }

    @Test
    public void GetMyGardens_UserNotAuthorized_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
    @Test
    public void GetMyGardens_UserAuthorized_Return200() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens"))
                .andExpect(MockMvcResultMatchers.status().isAccepted());
    }
}
