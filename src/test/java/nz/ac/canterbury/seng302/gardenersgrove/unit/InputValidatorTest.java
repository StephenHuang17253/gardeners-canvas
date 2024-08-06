package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputValidatorTest {

    /**
     * Setup a mock userService class for testing that email uniqueness validation
     * works
     */
    private static UserService userServiceMock;

    /**
     * Setup a mock profanityService class for testing
     */
    private static ProfanityService profanityServiceMock;

    @BeforeAll
    static void setup() {
        userServiceMock = Mockito.mock(UserService.class);
        profanityServiceMock = Mockito.mock(ProfanityService.class);
        new InputValidator(userServiceMock, profanityServiceMock);
    }

    @BeforeEach
    void resetInterface() {
        Mockito.when(userServiceMock.emailInUse(Mockito.any())).thenReturn(false);
    }

    @Test
    void InputValidator_validateUniqueEmail_uniqueEmail_return_OK() {
        Mockito.when(userServiceMock.emailInUse(Mockito.any())).thenReturn(false);
        assertEquals(ValidationResult.OK, InputValidator.validateUniqueEmail("jondoe@gmail.com"));
        Mockito.verify(userServiceMock, Mockito.atLeastOnce()).emailInUse(Mockito.any());
    }

    @Test
    void InputValidator_validateUniqueEmail_duplicatedEmail_return_NON_UNIQUE_EMAIL() {
        Mockito.when(userServiceMock.emailInUse(Mockito.any())).thenReturn(true);
        assertEquals(ValidationResult.NON_UNIQUE_EMAIL, InputValidator.validateUniqueEmail("jondoe@gmail.com"));
        Mockito.verify(userServiceMock, Mockito.atLeastOnce()).emailInUse(Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello", "Hello World", "àäèéëïĳöü", "áêéèëïíîôóúû", "êôúû", "ÆØÅæøå", "ÄÖäö",
            "ÅÄÖåäö", "ÄÖÕÜäöõü", "ÄÖÜẞäöüß", "ÇÊÎŞÛçêîşû", "ĂÂÎȘȚăâîșț", "ÂÊÎÔÛŴŶÁÉÍÏâêîôûŵŷáéíï", "ĈĜĤĴŜŬĉĝĥĵŝŭ",
            "ÇĞİÖŞÜçğıöşü", "ÁÐÉÍÓÚÝÞÆÖáðéíóúýþæö", "ÁÐÍÓÚÝÆØáðíóúýæø", "ÁÉÍÓÖŐÚÜŰáéíóöőúüű", "ÀÇÉÈÍÓÒÚÜÏàçéèíóòúüï",
            "ÀÂÆÇÉÈÊËÎÏÔŒÙÛÜŸàâæçéèêëîïôœùûüÿ", "ÁÀÇÉÈÍÓÒÚËÜÏáàçéèíóòúëüï", "ÁÉÍÑÓÚÜáéíñóúü",
            "ÀÉÈÌÒÙàéèìòù", "ćęłńóśźż ", "ćśůź ", "ãéëòôù ", "ČŠŽ",
            "अ आ इ ई उ ऊ ऋ ॠ ऌ ॡ ऍ ऎ ए ऐ ", "ਆਇਈਉਊਏਐਓਔਕਖਗਘਙਚਛਜ", "અ આ ઇ ઈ ઉ ઊ ઋ ઌ ઍ એ ઐ ", "ཀ ཁ ག ང ཅ ཆ ཇ ཉ ཏ ཐ ད ",
            "АБВГДЕЖЗИКЛМН", "ЙЩЬЮЯ", "ЁЫЭ", "ЄꙂꙀЗІЇꙈОуꙊѠ", "ΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡ", "字文化圈", "いうえおの", "アイウ", "ㄈㄉㄊㄋㄌㄍㄎㄏ ",
            "ㄪㄫㄬ", " Է Ը Թ Ժ ", " ჱ თ ი კ ლ მ ", "ⴷⴸⴹⴺⴻⴼⴽⴾⴿⵀⵁⵂⵃⵄⵅⵆⵇⵈⵉⵊⵋⵌⵍⵎ"})
    void InputValidator_compText_validString_return_OK(String input) {
        assertEquals(ValidationResult.OK, InputValidator.compulsoryAlphaPlusTextField(input));
    }

    @Test
    void InputValidator_compText_validStringWSpace_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.compulsoryAlphaPlusTextField("Hello World"));
    }

    @Test
    void InputValidator_optText_validString_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalAlphaPlusTextField("Hello"));
    }

    @Test
    void InputValidator_optText_validStringWSpace_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalAlphaPlusTextField("Hello World"));
    }

    @Test
    void InputValidator_compText_validStringwNum_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.compulsoryAlphaPlusTextField("Hello123"));
    }

    @Test
    void InputValidator_compText_validStringWSpaceWNum_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.compulsoryAlphaPlusTextField("Hello World123"));
    }

    @Test
    void InputValidator_optText_validStringWNum_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalAlphaPlusTextField("Hello123"));
    }

    @Test
    void InputValidator_optText_validStringWSpaceWNum_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalAlphaPlusTextField("Hello World123"));
    }

    @Test
    void InputValidator_compText_blank_return_blank() {
        assertEquals(ValidationResult.BLANK, InputValidator.compulsoryAlphaPlusTextField(""));
    }

    @Test
    void InputValidator_optText_blank_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalAlphaPlusTextField(""));
    }

    @Test
    void InputValidator_compText_validPunct_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.compulsoryAlphaPlusTextField("."));
    }

    @Test
    void InputValidator_optText_validPunct_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalAlphaPlusTextField("."));
    }

    @Test
    void InputValidator_compText_validPunctWText_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.compulsoryAlphaPlusTextField("Hello World."));
    }

    @Test
    void InputValidator_optText_validPunctWText_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalAlphaPlusTextField("Hello World."));
    }

    @Test
    void InputValidator_compText_invalidPunct_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.compulsoryAlphaPlusTextField("!"));
    }

    @Test
    void InputValidator_compText_Quotes_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.compulsoryAlphaPlusTextField(" \" "));
    }

    @Test
    void InputValidator_optText_invalidPunct_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.optionalAlphaPlusTextField("!"));
    }

    @Test
    void InputValidator_compText_invalidPunctWText_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.compulsoryAlphaPlusTextField("Hello World!"));
    }

    @Test
    void InputValidator_optText_invalidPunctWText_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.optionalAlphaPlusTextField("Hello World!"));
    }

    @Test
    void InputValidator_compText_NonAlphaText_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.compulsoryAlphaPlusTextField("ÙýµFB¬"));
    }

    @Test
    void InputValidator_optText_NonAlphaTextt_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.optionalAlphaPlusTextField("ÙýµFB¬"));
    }

    @Test
    void InputValidator_optText_NonAlphaText2_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.optionalAlphaPlusTextField("Seng ! "));
    }

    @Test
    void InputValidator_optText_NonAlphaText3_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.optionalAlphaPlusTextField("Henson & Hedges"));
    }

    @Test
    void InputValidator_optText_NonAlphaText4_return_NONALPHAPLUS() {
        assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.optionalAlphaPlusTextField("Water & Buffalo"));
    }


    @Test
    void InputValidator_numCommSingle_blank_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.validateGardenAreaInput(""));
    }

    @Test
    void InputValidator_numCommSingle_number_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.validateGardenAreaInput("123"));
    }

    @Test
    void InputValidator_numCommSingle_numberWComma_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.validateGardenAreaInput("123,23"));
    }

    @Test
    void InputValidator_numCommSingle_numberW2Comma_return_NONNUMERICCOMMA() {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA, InputValidator.validateGardenAreaInput("12,23,23"));
    }

    @Test
    void InputValidator_numCommSingle_letters_return_NONNUMERICCOMMA() {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA, InputValidator.validateGardenAreaInput("Abc"));
    }

    @Test
    void InputValidator_numCommSingle_numbersWletters_return_NONNUMERICCOMMA() {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA, InputValidator.validateGardenAreaInput("12JK23"));
    }

    @Test
    void InputValidator_numCommSingle_NonAlpha_return_NONNUMERICCOMMA() {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA, InputValidator.validateGardenAreaInput("~ÉI3Á┌1&"));
    }

    @Test
    void InputValidator_numCommSingle_onlyComma_return_NONNUMERICCOMMA() {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA, InputValidator.validateGardenAreaInput(","));
    }

    @Test
    void InputValidator_numCommSingle_singleNumber_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.validateGardenAreaInput("1"));
    }

    @Test
    void InputValidator_optText_TextWHyphen_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalAlphaPlusTextField("Hello-World"));
    }

    @Test
    void InputValidator_compText_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.compulsoryTextField("Hello-World#$%"));
    }

    @Test
    void InputValidator_compText_return_BLANK() {
        assertEquals(ValidationResult.BLANK, InputValidator.compulsoryTextField(""));
    }

    @Test
    void InputValidator_compTextWithLengthLimit_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.compulsoryTextField("12345", 5));
    }

    @Test
    void InputValidator_compTextWithLengthLimit_return_LENGTH_OVER_LIMIT() {
        assertEquals(ValidationResult.LENGTH_OVER_LIMIT, InputValidator.compulsoryTextField("123456789", 2));
    }

    @Test
    void InputValidator_compAlphaTestWithLengthLimit_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.compulsoryAlphaPlusTextField("12345", 5));
    }

    @Test
    void InputValidator_compAlphaTextWithLengthLimit_return_LENGTH_OVER_LIMIT() {
        assertEquals(ValidationResult.LENGTH_OVER_LIMIT, InputValidator.compulsoryAlphaPlusTextField("123456789", 2));
    }

    @Test
    void InputValidator_optTextWithLengthLimit_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalTextField("123", 4));
    }

    @Test
    void InputValidator_optTextWithLengthLimit_return_LENGTH_OVER_LIMIT() {
        assertEquals(ValidationResult.LENGTH_OVER_LIMIT, InputValidator.optionalTextField("123456789", 2));
    }

    @Test
    void InputValidator_optTextWithLengthLimit_blankInput_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.optionalTextField("", 5));
    }

    @Test
    void InputValidator_numCommSingle_1comma0_return_OK() {
        assertEquals(ValidationResult.OK, InputValidator.validateGardenAreaInput("1,0"));
    }

    @Test
    void InputValidator_numCommSingle_negative1comma0_return_NON_NUMERIC_COMMA() {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA, InputValidator.validateGardenAreaInput("-1,0"));
    }

    @Test
    void InputValidator_numCommSingle_InvalidFormat1_return_NON_NUMERIC_COMMA() {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA, InputValidator.validateGardenAreaInput("1-0,0"));
    }

    @Test
    void InputValidator_numCommSingle_InvalidFormat2_return_NON_NUMERIC_COMMA() {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA, InputValidator.validateGardenAreaInput("!9,0"));
    }

    @Test
    void InputValidator_numCommSingle_InvalidFormat3_return_NON_NUMERIC_COMMA() {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA, InputValidator.validateGardenAreaInput("1 ! 0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.00000000023", "1", "8000000.00", "1E-1", "1.1E4", "1,1E-1", "8E6", "1.4E-1", "0.01"})
    void InputValidator_validateGardenAreaInput_ValidArea_return_OK(String input) {
        assertEquals(ValidationResult.OK, InputValidator.validateGardenAreaInput(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0000001", "0.009", "0"})
    void InputValidator_validateGardenAreaInput_SmallArea_return_AREA_TOO_SMALL(String input) {
        assertEquals(ValidationResult.AREA_TOO_SMALL, InputValidator.validateGardenAreaInput(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"8000000.001", "8000000.01", "340300000000000000000000000000000000000"})
    void InputValidator_validateGardenAreaInput_LargeArea_return_AREA_TOO_LARGE(String input) {
        assertEquals(ValidationResult.AREA_TOO_LARGE, InputValidator.validateGardenAreaInput(input));
    }

    /**
     * Test for valid names
     */
    @ParameterizedTest
    @ValueSource(strings = {"John Doe", "John-Doe", "John Doe's"})
    void InputValidator_validateName_ValidName_return_OK(String name) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validateName(name));
    }

    /**
     * Test for invalid names
     *
     * @param name
     */
    @ParameterizedTest
    @ValueSource(strings = {"John1", "John>", "~John"})
    void InputValidator_validateName_InvalidName_return_INVALID_USERNAME(String name) {
        Assertions.assertEquals(ValidationResult.INVALID_USERNAME, InputValidator.validateName(name));
    }

    /**
     * Test for valid emails
     *
     * @param email
     */
    @ParameterizedTest
    @ValueSource(strings = {"test-test@example.com", "user_123@gmail.co.nz", "john.doe@hotmail.com",
            "phlddzoxuomhdkclzinbsqhutjqhzodonrbgyxibpkutddaovmxifypmeksvhkts@mwbmmvndbnvfdskmrmmropbvhdgegssqcengjnfj" +
                    "oavhccefauucivfpthrucoyhlxfgkcdurlffpoacnhhysprommslgxmusevvpxdgkkifsgpbpiljrcxjwejestmgvnsevszck" +
                    "ujiglsrihnpblwmiculgtxodopsthkdzzpgjhznkcsarvzvubnyhutxhyyecsvjjykzxhdqlaxooxqnfbuewmajwlmvhklhzy" +
                    "wxuhsxwtnshoxuw.com"})
    void InputValidator_validateUniqueEmail_ValidEmail_return_OK(String email) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validateUniqueEmail(email));
    }

    // Todo check if there is an email already in persistence <-- need to mock persistence for this.
    // Todo add foreign scripts


    @ParameterizedTest
    @ValueSource(strings = {" ", "user_123gmail.co.nz", "john.doe@h.", "test@test.c", "test@.com", "@test.com",
            "abc-@mail.com", "abc..def@mail.com", ".abc@mail.com", "abc.def@mail#archive.com", "abc.def@mail..com"})
    void InputValidator_validateUniqueEmail_InvalidEmail_return_INVALID_EMAIL(String email) {
        Assertions.assertEquals(ValidationResult.INVALID_EMAIL, InputValidator.validateUniqueEmail(email));
    }

    /**
     * Test for valid passwords
     *
     * @param password
     */
    @ParameterizedTest
    @ValueSource(strings = {"aB0!bbba", "##aBB0hhhhhhhhhh", "Passw0rd!", "Pass word1!"})
    void InputValidator_validatePassword_ValidPassword_return_OK(String password) {
        String firstName = "John";
        String lastName = "";
        boolean noLastName = true;
        String emailAddress = "johndoe@gmail.com";
        List<String> otherFields = new ArrayList<>();
        otherFields.add(firstName);
        if (noLastName == false) {
            otherFields.add(lastName);
        }
        otherFields.add(emailAddress);
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validatePassword(password, otherFields));
    }

    /**
     * Test for invalid passwords
     *
     * @param password
     */
    @ParameterizedTest
    @CsvSource({"aaa", "aaaaaaaa", "000!0000", "password1!", "Password123", "Password!@#", "PASSWORD1!",
            "1D!0", "D!1", "aA!0", "Pa!0AAA", "John12345!", "Doe12345!", "Johndoe@gmail.com", "2024-01-01Aa"})
    void InputValidator_validatePassword_InvalidPassword_return_INVALID_PASSWORD(String password) {
        String firstName = "John";
        String lastName = "Doe";
        boolean noLastName = false;
        LocalDate dateOfBirth = LocalDate.parse("2024-01-01");
        String emailAddress = "johndoe@gmail.com";
        List<String> otherFields = new ArrayList<>();
        otherFields.add(firstName);
        if (!noLastName) {
            otherFields.add(lastName);
        }
        if (!(dateOfBirth == null)) {
            otherFields.add(dateOfBirth.toString());
        }
        otherFields.add(emailAddress);
        Assertions.assertEquals(ValidationResult.INVALID_PASSWORD, InputValidator.validatePassword(password, otherFields));
    }

    /**
     * Test for valid DOB
     *
     * @param dob string date
     */
    @ParameterizedTest
    @ValueSource(strings = {"01/01/2000", "01/12/1999", "31/12/2000"})
    void InputValidator_isValidDOB_ValidDate_return_OK(String dob) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validateDOB(dob));
    }

    /**
     * Test for invalid DOB age below 13
     */
    @Test
    void InputValidator_isValidDOB_AgeBelow13_return_AGE_BELOW_13() {
        String dob = LocalDate.now().minusYears(13).plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Assertions.assertEquals(ValidationResult.AGE_BELOW_13, InputValidator.validateDOB(dob));
    }

    /**
     * Test for invalid DOB age above 120
     */
    @Test
    void InputValidator_isValidDOB_AgeAbove120_return_AGE_ABOVE_120() {
        String dob = LocalDate.now().minusYears(121).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Assertions.assertEquals(ValidationResult.AGE_ABOVE_120, InputValidator.validateDOB(dob));
    }

    /**
     * Test for invalid DOB format
     *
     * @param dob string date
     */
    @ParameterizedTest
    @ValueSource(strings = {"1960/3/2", "Steve", "12122013", "12:12:2014", "12-12-2014", "31/02/2003", "234/03/0000", "01/01/11111", "01/021/2000", "31/04/2002", "02/13/2001", "04/00/2001", "00/12/2004"})
    void InputValidator_isValidDOB_invalidFormat_return_INVALID_DATE_FORMAT(String dob) {
        Assertions.assertEquals(ValidationResult.INVALID_DATE_FORMAT, InputValidator.validateDOB(dob));
    }


    /**
     * Test for valid DOB
     *
     * @param date
     */
    @ParameterizedTest
    @ValueSource(strings = {"01/01/2000", "01/12/1999", "31/12/2000"})
    void InputValidator_isValidDate_ValidDate_return_OK(String date) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validateDate(date));
    }

    /**
     * Test for invalid DOB format
     *
     * @param date
     */
    @ParameterizedTest
    @ValueSource(strings = {"1960/3/2", "Steve", "12122013", "12:12:2014", "12-12-2014", "29/02/2001", "31/04/2002", "02/13/2001", "04/00/2001", "00/12/2004", "\""})
    void InputValidator_isValidDate_invalidFormat_return_INVALID_DATE_FORMAT(String date) {
        Assertions.assertEquals(ValidationResult.INVALID_DATE_FORMAT, InputValidator.validateDate(date));
    }

    /**
     * Test for valid garden street address
     *
     * @param streetAddress string input for a garden's street address
     */
    @ParameterizedTest
    @ValueSource(strings = {"20 Kirkwood Avenue", "139 Greers Road", "116 Riccarton Road"})
    void InputValidator_isValidStreetAddress_validStreetAddress_return_OK(String streetAddress) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.optionalAlphaPlusTextField(streetAddress));
    }

    /**
     * Test for invalid garden street address
     *
     * @param streetAddress string input for a garden's street address
     */
    @ParameterizedTest
    @ValueSource(strings = {"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenea"})
    void InputValidator_isValidStreetAddress_invalidStreetAddress_return_LENGTH_OVER_LIMIT(String streetAddress) {
        Assertions.assertEquals(ValidationResult.LENGTH_OVER_LIMIT, InputValidator.optionalAlphaPlusTextField(streetAddress, 96));
    }

    @ParameterizedTest
    @ValueSource(strings = {"116 !@#$%^&*()_+-=[]{};:',.<>/?| Road", "\""})
    void InputValidator_isValidStreetAddress_invalidStreetAddress_return_NON_ALPHA_PLUS(String streetAddress) {
        Assertions.assertEquals(ValidationResult.NON_ALPHA_PLUS, InputValidator.optionalAlphaPlusTextField(streetAddress, 96));
    }

    /**
     * Test for valid postcode
     *
     * @param postcode string input for a garden's postcode
     */
    @ParameterizedTest
    @ValueSource(strings = {"8041", "23020392", "SN6 8TL"})
    void InputValidator_isValidPostcode_validPostcode_return_OK(String postcode) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validatePostcodeInput(postcode));
    }

    /**
     * Test for invalid postcode
     *
     * @param postcode string input for a garden's postcode
     */
    @ParameterizedTest
    @ValueSource(strings = {"THIS IS NOT A POSTC*DE", "8041!@#$", "\""})
    void InputValidator_isValidPostcode_invalidPostcode_return_INVALID_POSTCODE(String postcode) {
        Assertions.assertEquals(ValidationResult.INVALID_POSTCODE, InputValidator.validatePostcodeInput(postcode));
    }
}
