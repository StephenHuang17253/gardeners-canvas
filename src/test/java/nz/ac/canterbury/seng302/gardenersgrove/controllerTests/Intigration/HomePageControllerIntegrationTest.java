package nz.ac.canterbury.seng302.gardenersgrove.controllerTests.Intigration;


import nz.ac.canterbury.seng302.gardenersgrove.controller.HomePageController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
public class HomePageControllerIntegrationTest {

    @Autowired
    private HomePageController homePageController;

    @Autowired
    private MockMvc mockMvc;

//    private static MockedStatic<SecurityContextHolder> securityContextHolderMock;
    private static SecurityContext securityContextMock;
    private static Authentication authenticationMock;

    @BeforeAll
    public static void setup() {
//        securityContextHolderMock = Mockito.mockStatic(SecurityContextHolder.class);
        authenticationMock = Mockito.mock(Authentication.class);
        securityContextMock = Mockito.spy(SecurityContext.class);


        SecurityContextHolder.setContext(securityContextMock);


    }
//
//    @AfterEach
//    public void tearDown() {
//        securityContextHolderMock.close();
//    }

    @Test
    public void controllerLoads()
    {
        assertNotNull(homePageController);
    }

    @Test
    public void mvcMockIsAlive() throws Exception
    {
        this.mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void getMapping_root_redirectsToHomePage() throws Exception
    {
        this.mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("./home"));
    }

    @Test
    public void getMappingNotLoggedIn_Invalid_requestForbidden() throws Exception
    {
        this.mockMvc.perform(get("/asdassdas"))
                .andDo(print())
                .andExpect(status().isForbidden()); //Should be 403 as the user is not logged in
    }


    @Test
    public void getMappingLoggedIn_Invalid_redirectsToError() throws Exception
    {
        this.mockMvc.perform(get("/asdassdas"))
                .andDo(print())
                .andExpect(status().isForbidden()); //Should be 403 as the user is not logged in
        Assertions.fail(); // Todo add logged in condition
    };

    @Test
    public void getProfilePictureString_Null_DefaultPath() throws Exception
    {
        String pictureUrl = homePageController.getProfilePictureString(null);
        assertEquals("/Images/default_profile_picture.png", pictureUrl);
    }

    // Todo Check if this test is needed (is the function expected to recover from errors?)
    // checks if the function can recover from missing images
//    @ParameterizedTest
//    @ValueSource( strings = {"", "definitely a valid user image","definitely a valid user image.png","myImage","myImage.png"})
//    public void getProfilePictureString_NoSuchImage_DefaultPath(String url) throws Exception
//    {
//        String pictureUrl = homePageController.getProfilePictureString(url);
//        assertEquals("/Images/default_profile_picture.png", pictureUrl);
//    }
    @Test
    public void getMappingNotLoggedIn_home_containsNoNames() throws Exception
    {
        this.mockMvc.perform(get("/home"))
                .andDo(print())
                .andExpect(status().isOk()) //Should be 403 as the user is not logged in
                .andExpect(model().attribute("profilePicture",is("")))
                .andExpect(model().attribute("username", is("")));
    };

    // Todo figure out how to mock a user being logged in
    @Test
    public void getMappingLoggedIn_home_containsNames() throws Exception
    {
        Mockito.when(authenticationMock.getName()).thenReturn("johndoe@email.com");
        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);
        assertEquals(securityContextMock.getAuthentication().getName(), "johndoe@email.com");
        assertEquals(SecurityContextHolder.getContext().getAuthentication().getName(), "johndoe@email.com");


//        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContextMock);

        this.mockMvc.perform(get("/home"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("username",is("John")))
                .andExpect(model().attribute("profilePicture", is("/Images/default_profile_picture.png")));
    };



}
