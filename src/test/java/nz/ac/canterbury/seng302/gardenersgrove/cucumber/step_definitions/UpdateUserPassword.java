package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.RegistrationFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
public class UpdateUserPassword {

    public AuthenticationManager authenticationManager;
    public static MockMvc MOCK_MVC;
    public UserRepository userRepository;
    public static UserService userService;
    public PasswordEncoder passwordEncoder;
    public FileService fileService;

    String currentPassword;
    String newPassword;
    String retypePassword;

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        ProfileController profileController = new ProfileController(userService, authenticationManager, fileService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(profileController).build();
    }

    //AC1
    @io.cucumber.java.en.Given("I am on the edit profile form")
    public void iAmOnTheEditProfileForm() throws Exception {
        MOCK_MVC.perform(MockMvcRequestBuilders.get("/edit-profile"))
                .andExpect(MockMvcResultMatchers.view().name("edit-profile-form"));
    }
    //AC1
    @io.cucumber.java.en.When("I hit the change password button")
    public void iHitTheChangePasswordButton() throws Exception {
        MOCK_MVC.perform(MockMvcRequestBuilders.get("/editPassword"));
    }
    //AC1
    @io.cucumber.java.en.Then("a dedicated form is shown with three text fields: “old password”, “new password”, and “retype password”")
    public void aDedicatedFormIsShownWithThreeTextFieldsOldPasswordNewPasswordAndRetypePassword() throws Exception {
        MOCK_MVC.perform(MockMvcRequestBuilders.get("/profile/editPassword"));
        String responseContent = MOCK_MVC.perform(MockMvcRequestBuilders.get("/profile/editPassword")).andReturn().getResponse().getContentAsString();

        Assert.hasText(responseContent, "<input type=\"password\" id=\"currentPassword\" name=\"currentPassword\" />");

    }
    //AC2
    @io.cucumber.java.en.When("I enter an old password that does not match the password in file")
    public void iEnterAnOldPasswordThatDoesNotMatchThePasswordInFile() throws Exception {
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post("/editPassword")
                        .param("currentPassword",currentPassword)
                        .param("newPassword", newPassword)
                        .param("retypePassword", retypePassword)
        );
    }
    //AC2
    @io.cucumber.java.en.Then("an error message tells me “Your old password is incorrect")
    public void anErrorMessageTellsMeYourOldPasswordIsIncorrect() {
    }
    //AC3
    @io.cucumber.java.en.And("I enter two different passwords in “new”")
    public void iEnterTwoDifferentPasswordsInNew() {
    }
    //
    @io.cucumber.java.en.And("“retype password” fields, when I hit the save button")
    public void retypePasswordFieldsWhenIHitTheSaveButton() {
    }

    @io.cucumber.java.en.Then("an error message tells me “The new passwords do not match”")
    public void anErrorMessageTellsMeTheNewPasswordsDoNotMatch() {
    }

    @io.cucumber.java.en.When("I enter a weak password")
    public void iEnterAWeakPasswordEGContainsAnyOtherFieldsFromTheUserProfileFormIsBelowCharLongDoesNotContainAVariationOfDifferentTypesOfCharactersWithOneLowercaseLetterOneUppercaseLetterOneDigitOneSpecialCharacter(int arg0) {
    }

    @io.cucumber.java.en.And("^I hit the save button$")
    public void iHitTheSaveButton() {
    }

    @io.cucumber.java.en.Then("an error message tells “Your password must be at least eight characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character\\.”$")
    public void anErrorMessageTellsYourPasswordMustBeAtLeastEightCharactersLongAndIncludeAtLeastOneUppercaseLetterOneLowercaseLetterOneNumberAndOneSpecialCharacter(int arg0) {
    }

    @io.cucumber.java.en.When("^I enter fully compliant details$")
    public void iEnterFullyCompliantDetails() {
    }

    @io.cucumber.java.en.And("^I click the “Submit” button$")
    public void iClickTheSubmitButton() {
    }

    @io.cucumber.java.en.Then("^my password is updated, and an email is sent to my email address to confirm that my password was updated$")
    public void myPasswordIsUpdatedAndAnEmailIsSentToMyEmailAddressToConfirmThatMyPasswordWasUpdated() {
    }

    @io.cucumber.java.en.When("^I hit the cancel button$")
    public void iHitTheCancelButton() {
    }

    @io.cucumber.java.en.Then("^I am sent back to my view details page, and no changes have been made to my password\\.$")
    public void iAmSentBackToMyViewDetailsPageAndNoChangesHaveBeenMadeToMyPassword() {
    }
}
