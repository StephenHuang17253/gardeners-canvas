package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import nz.ac.canterbury.seng302.gardenersgrove.controller.RegistrationFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
public class UpdateUserPassword {

    @io.cucumber.java.en.Given("^I am on the edit profile form,$")
    public void iAmOnTheEditProfileForm() {
    }

    @io.cucumber.java.en.When("^I hit the change password button,$")
    public void iHitTheChangePasswordButton() {
    }

    @io.cucumber.java.en.Then("^a dedicated form is shown with three text fields: “old password”, “new password”, and “retype password”$")
    public void aDedicatedFormIsShownWithThreeTextFieldsOldPasswordNewPasswordAndRetypePassword() {
    }

    @io.cucumber.java.en.Given("^I am on the change password form,$")
    public void iAmOnTheChangePasswordForm() {
    }

    @io.cucumber.java.en.When("^I enter an old password that does not match the password in file,$")
    public void iEnterAnOldPasswordThatDoesNotMatchThePasswordInFile() {
    }

    @io.cucumber.java.en.Then("^an error message tells me “Your old password is incorrect$")
    public void anErrorMessageTellsMeYourOldPasswordIsIncorrect() {
    }

    @io.cucumber.java.en.And("^I enter two different passwords in “new”$")
    public void iEnterTwoDifferentPasswordsInNew() {
    }

    @io.cucumber.java.en.And("^“retype password” fields, when I hit the save button,$")
    public void retypePasswordFieldsWhenIHitTheSaveButton() {
    }

    @io.cucumber.java.en.Then("^an error message tells me “The new passwords do not match”\\.$")
    public void anErrorMessageTellsMeTheNewPasswordsDoNotMatch() {
    }

    @io.cucumber.java.en.And("^I enter a weak password \\(e\\.g\\., contains any other fields from the user profile form, is below (\\d+) char long, does not contain a variation of different types of characters with one lowercase letter, one uppercase letter, one digit, one special character\\),$")
    public void iEnterAWeakPasswordEGContainsAnyOtherFieldsFromTheUserProfileFormIsBelowCharLongDoesNotContainAVariationOfDifferentTypesOfCharactersWithOneLowercaseLetterOneUppercaseLetterOneDigitOneSpecialCharacter(int arg0) {
    }

    @io.cucumber.java.en.When("^I hit the save button$")
    public void iHitTheSaveButton() {
    }

    @io.cucumber.java.en.Then("^an error message tells “Your password must be at least (\\d+) characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character\\.”$")
    public void anErrorMessageTellsYourPasswordMustBeAtLeastCharactersLongAndIncludeAtLeastOneUppercaseLetterOneLowercaseLetterOneNumberAndOneSpecialCharacter(int arg0) {
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
