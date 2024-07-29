//package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;
//
//import nz.ac.canterbury.seng302.gardenersgrove.controller.PublicGardensController;
//import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
//import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
//import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
//import nz.ac.canterbury.seng302.gardenersgrove.service.*;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import io.cucumber.java.Before;
//import nz.ac.canterbury.seng302.gardenersgrove.controller.HomePageController;
//import nz.ac.canterbury.seng302.gardenersgrove.controller.ManageFriendsController;
//import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
//import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
//import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
//
//import java.io.File;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//public class U25_MainPage {
//
//    @Autowired
//    public GardenRepository gardenRepository;
//
//    @Autowired
//    public PlantRepository plantRepository;
//
//    @Autowired
//    public UserRepository userRepository;
//
//    @Autowired
//    public PasswordEncoder passwordEncoder;
//
//    @Autowired
//    public FriendshipRepository friendshipRepository;
//
//    @Autowired
//    public SecurityService securityService;
//
//    public static GardenService gardenService;
//
//    public static UserService userService;
//
//    @MockBean
//    public FileService fileService;
//
//    public static PlantService plantService;
//
//    private static FriendshipService friendshipService;
//
//    public UserInteractionService userInteractionService;
//
//    public MockMvc mockMVC;
//
//    private MvcResult mvcResult;
//
//    // Setup
//    @Before
//    public void before_or_after_all() {
//
//        userService = new UserService(passwordEncoder, userRepository);
//        gardenService = new GardenService(gardenRepository, userService);
//        plantService = new PlantService(plantRepository, gardenService, fileService);
//        friendshipService = new FriendshipService(friendshipRepository, userService);
//
//        HomePageController homePageController = new HomePageController(userService, gardenService, plantService, friendshipService, securityService, userInteractionService);
//
//        // Allows us to bypass spring security
//        mockMVC = MockMvcBuilders
//                .standaloneSetup(homePageController)
//                .build();
//
//    }
//
//    @Test
//    public void goToHomePage_NoRecentInteractions_FriendsListEmpty() throws Exception {
//        mvcResult = mockMVC.perform(get("/home"))
//                .andExpect(status().isOk())
//                .andReturn();
//        List<FriendModel> recentFriends = (List<FriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("recentFriends");
//        assertTrue(recentFriends.size() == 0);
//    }
//
//    @Test
//    public void goToHomePage_OneRecentInteraction_FriendsListContainsUser() {
//
//    }
//
//}
