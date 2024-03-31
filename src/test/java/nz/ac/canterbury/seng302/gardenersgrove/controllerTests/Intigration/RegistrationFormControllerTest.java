package nz.ac.canterbury.seng302.gardenersgrove.controllerTests.Intigration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.HomePageController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.RegistrationFormController;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static SecurityContext securityContextMock;
    @Mock
    private static AuthenticationManager authenticationManagerMock;

    @Mock
    private static UserService userServiceMock;

    @InjectMocks
    private static RegistrationFormController registrationFormController;

    @BeforeAll
    public static void setup() {
//      securityContextHolderMock = Mockito.mockStatic(SecurityContextHolder.class);
        authenticationManagerMock = Mockito.mock(AuthenticationManager.class);
        securityContextMock = Mockito.spy(SecurityContext.class);
        userServiceMock = Mockito.mock(UserService.class);
        SecurityContextHolder.setContext(securityContextMock);
    }

    @Test
    public void controllerLoads()
    {
        assertNotNull(registrationFormController);
    }

    @Test
    public void mvcMockIsAlive() throws Exception
    {
        this.mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    public void getRegistrationPage_NoParams_NonFilledPage() throws Exception
    {
        this.mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("dateOfBirth",""))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }
    @Test
    public void getRegistrationPage_FirstName_PageWFirstName() throws Exception
    {
        this.mockMvc.perform(get("/register").param("firstName","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName","123"))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("dateOfBirth",""))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }
    @Test

    public void getRegistrationPage_LastName_PageWLastName() throws Exception
    {
        this.mockMvc.perform(get("/register").param("lastName","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName","123"))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("dateOfBirth",""))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }

    @Test
    public void getRegistrationPage_NoLastName_PageWNoLastName() throws Exception
    {
        this.mockMvc.perform(get("/register").param("noLastName","true"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",true))
                .andExpect(model().attribute("dateOfBirth",""))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }

    @Test
    public void getRegistrationPage_DOB_PageWDOB() throws Exception
    {
        this.mockMvc.perform(get("/register").param("dateOfBirth","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("dateOfBirth","123"))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }

    @Test
    public void getRegistrationPage_Email_PageWEmail() throws Exception
    {
        this.mockMvc.perform(get("/register").param("emailAddress","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("dateOfBirth",""))
                .andExpect(model().attribute("emailAddress","123"))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword",""));
    }

    @Test
    public void getRegistrationPage_Password_PageWPassword() throws Exception
    {
        this.mockMvc.perform(get("/register").param("password","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("dateOfBirth",""))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password","123"))
                .andExpect(model().attribute("repeatPassword",""));
    }

    @Test
    public void getRegistrationPage_RepeatPassword_PageWRepeatPassword() throws Exception
    {
        this.mockMvc.perform(get("/register").param("repeatPassword","123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName",""))
                .andExpect(model().attribute("lastName",""))
                .andExpect(model().attribute("noLastName",false))
                .andExpect(model().attribute("dateOfBirth",""))
                .andExpect(model().attribute("emailAddress",""))
                .andExpect(model().attribute("password",""))
                .andExpect(model().attribute("repeatPassword","123"));
    }






}
