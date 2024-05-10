package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.TokenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AccountControllerTest {

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @MockBean
    private UserService userServiceMock;

    @MockBean
    private EmailService emailServiceMock;

    @MockBean
    private TokenService tokenServiceMock;

    @Mock
    private static GardenService gardenServiceMock;

    private static User mockUser = new User("John", "Test", "account.user.test@accountcontroller.com", LocalDate.now());

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        gardenServiceMock = Mockito.mock(GardenService.class);
    }


    @Test
    @WithMockUser(username = "account.user.test@accountcontroller.com")
    public void mvcMockIsAlive() throws Exception
    {
        this.mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "account.user.test@accountcontroller.com")
    public void getRegistrationPage_NoParams_NonFilledPage() throws Exception
    {
        this.mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }
    @Test
    @WithMockUser(username = "account.user.test@accountcontroller.com")
    public void getRegistrationPage_FirstName_PageWFirstName() throws Exception
    {
        this.mockMvc.perform(get("/register").param("firstName","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName","123"))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }
    @Test
    @WithMockUser(username = "account.user.test@accountcontroller.com")
    public void getRegistrationPage_LastName_PageWLastName() throws Exception
    {
        this.mockMvc.perform(get("/register").param("lastName","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName","123"))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }

    @Test
    @WithMockUser(username = "account.user.test@accountcontroller.com")
    public void getRegistrationPage_NoLastName_PageWNoLastName() throws Exception
    {
        this.mockMvc.perform(get("/register").param("noLastName","true"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",true))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }

    @Test
    @WithMockUser(username = "account.user.test@accountcontroller.com")
    public void getRegistrationPage_Email_PageWEmail() throws Exception
    {
        this.mockMvc.perform(get("/register").param("emailAddress","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("emailAddress","123"))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }

    @Test
    @WithMockUser(username = "account.user.test@accountcontroller.com")
    public void getRegistrationPage_Password_PageWPassword() throws Exception
    {
        this.mockMvc.perform(get("/register").param("password","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password","123"))
                .andExpect(model().attribute("repeatPassword",""));
    }

    @Test
    @WithMockUser(username = "account.user.test@accountcontroller.com")
    public void getRegistrationPage_RepeatPassword_PageWRepeatPassword() throws Exception
    {
        this.mockMvc.perform(get("/register").param("repeatPassword","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword","123"));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "Steve:Jobs:false:steve@jobs.com:Password1!",
            "Steve::true:steve@jobs.com:Password1!"
    }, delimiter = ':')
    public void RegistrationPage_addValidUser_createsUser_returnsOK(String firstname, String lastname, String noLastName,
                                                                    String emailAddress, String password) throws Exception {
        this.mockMvc.perform(post("/register").with(csrf())
                .param("firstName",firstname)
                .param("lastName",lastname)
                .param("noLastName",noLastName)
                .param("emailAddress",emailAddress)
                .param("password",password)
                .param("repeatPassword",password))
                .andDo(print());
        Mockito.verify(userServiceMock, Mockito.times(1)).addUser(Mockito.any(),Mockito.any());
        Mockito.verify(emailServiceMock, Mockito.times(1)).sendRegistrationEmail(Mockito.any());
    }





}
