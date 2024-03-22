package nz.ac.canterbury.seng302.gardenersgrove;
import jakarta.persistence.criteria.CriteriaBuilder;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.ValidationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputValidatorTest {


    @ParameterizedTest
    @ValueSource(strings = {"Hello", "Hello World","àäèéëïĳöü","áêéèëïíîôóúû","êôúû","ÆØÅæøå","ÄÖäö",
            "ÅÄÖåäö","ÄÖÕÜäöõü","ÄÖÜẞäöüß","ÇÊÎŞÛçêîşû","ĂÂÎȘȚăâîșț","ÂÊÎÔÛŴŶÁÉÍÏâêîôûŵŷáéíï","ĈĜĤĴŜŬĉĝĥĵŝŭ",
            "ÇĞİÖŞÜçğıöşü","ÁÐÉÍÓÚÝÞÆÖáðéíóúýþæö","ÁÐÍÓÚÝÆØáðíóúýæø","ÁÉÍÓÖŐÚÜŰáéíóöőúüű","ÀÇÉÈÍÓÒÚÜÏàçéèíóòúüï",
            "ÀÂÆÇÉÈÊËÎÏÔŒÙÛÜŸàâæçéèêëîïôœùûüÿ","ÁÀÇÉÈÍÓÒÚËÜÏáàçéèíóòúëüï","ÁÉÍÑÓÚÜáéíñóúü",
            "ÀÉÈÌÒÙàéèìòù","ćęłńóśźż ","ćśůź ","ãéëòôù ","ČŠŽ",
            "अ आ इ ई उ ऊ ऋ ॠ ऌ ॡ ऍ ऎ ए ऐ ","ਆਇਈਉਊਏਐਓਔਕਖਗਘਙਚਛਜ","અ આ ઇ ઈ ઉ ઊ ઋ ઌ ઍ એ ઐ ","ཀ ཁ ག ང ཅ ཆ ཇ ཉ ཏ ཐ ད ",
            "АБВГДЕЖЗИКЛМН","ЙЩЬЮЯ","ЁЫЭ","ЄꙂꙀЗІЇꙈОуꙊѠ","ΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡ","字文化圈","いうえおの","アイウ","ㄈㄉㄊㄋㄌㄍㄎㄏ ",
            "ㄪㄫㄬ"," Է Ը Թ Ժ "," ჱ თ ი კ ლ მ ","ⴷⴸⴹⴺⴻⴼⴽⴾⴿⵀⵁⵂⵃⵄⵅⵆⵇⵈⵉⵊⵋⵌⵍⵎ"})
    public void InputValidator_compText_validString_return_OK(String input)
    {
        assertEquals(ValidationResult.OK,InputValidator.compulsoryAlphaPlusTextField(input));
    }

    @Test
    public void InputValidator_compText_validStringWSpace_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.compulsoryAlphaPlusTextField("Hello World"));
    }

    @Test
    public void InputValidator_optText_validString_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalAlphaPlusTextField("Hello"));
    }

    @Test
    public void InputValidator_optText_validStringWSpace_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalAlphaPlusTextField("Hello World"));
    }

    @Test
    public void InputValidator_compText_validStringwNum_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.compulsoryAlphaPlusTextField("Hello123"));
    }

    @Test
    public void InputValidator_compText_validStringWSpaceWNum_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.compulsoryAlphaPlusTextField("Hello World123"));
    }

    @Test
    public void InputValidator_optText_validStringWNum_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalAlphaPlusTextField("Hello123"));
    }

    @Test
    public void InputValidator_optText_validStringWSpaceWNum_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalAlphaPlusTextField("Hello World123"));
    }

    @Test
    public void InputValidator_compText_blank_return_blank()
    {
        assertEquals(ValidationResult.BLANK,InputValidator.compulsoryAlphaPlusTextField(""));
    }

    @Test
    public void InputValidator_optText_blank_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalAlphaPlusTextField(""));
    }

    @Test
    public void InputValidator_compText_validPunct_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.compulsoryAlphaPlusTextField("."));
    }

    @Test
    public void InputValidator_optText_validPunct_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalAlphaPlusTextField("."));
    }

    @Test
    public void InputValidator_compText_validPunctWText_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.compulsoryAlphaPlusTextField("Hello World."));
    }

    @Test
    public void InputValidator_optText_validPunctWText_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalAlphaPlusTextField("Hello World."));
    }

    @Test
    public void InputValidator_compText_invalidPunct_return_NONALPHAPLUS()
    {
        assertEquals(ValidationResult.NON_ALPHA_PLUS,InputValidator.compulsoryAlphaPlusTextField("!"));
    }

    @Test
    public void InputValidator_optText_invalidPunct_return_NONALPHAPLUS()
    {
        assertEquals(ValidationResult.NON_ALPHA_PLUS,InputValidator.optionalAlphaPlusTextField("!"));
    }

    @Test
    public void InputValidator_compText_invalidPunctWText_return_NONALPHAPLUS()
    {
        assertEquals(ValidationResult.NON_ALPHA_PLUS,InputValidator.compulsoryAlphaPlusTextField("Hello World!"));
    }

    @Test
    public void InputValidator_optText_invalidPunctWText_return_NONALPHAPLUS()
    {
        assertEquals(ValidationResult.NON_ALPHA_PLUS,InputValidator.optionalAlphaPlusTextField("Hello World!"));
    }

    @Test
    public void InputValidator_compText_NonAlphaText_return_NONALPHAPLUS()
    {
        assertEquals(ValidationResult.NON_ALPHA_PLUS,InputValidator.compulsoryAlphaPlusTextField("ÙýµFB¬"));
    }

    @Test
    public void InputValidator_optText_NonAlphaTextt_return_NONALPHAPLUS()
    {
        assertEquals(ValidationResult.NON_ALPHA_PLUS,InputValidator.optionalAlphaPlusTextField("ÙýµFB¬"));
    }

    @Test
    public void InputValidator_optText_NonAlphaText2_return_NONALPHAPLUS()
    {
        assertEquals(ValidationResult.NON_ALPHA_PLUS,InputValidator.optionalAlphaPlusTextField("Seng ! "));
    }

    @Test
    public void InputValidator_optText_NonAlphaText3_return_NONALPHAPLUS()
    {
        assertEquals(ValidationResult.NON_ALPHA_PLUS,InputValidator.optionalAlphaPlusTextField("Henson & Hedges"));
    }

    @Test
    public void InputValidator_optText_NonAlphaText4_return_NONALPHAPLUS()
    {
        assertEquals(ValidationResult.NON_ALPHA_PLUS,InputValidator.optionalAlphaPlusTextField("Water & Buffalo"));
    }


    @Test
    public void InputValidator_numCommSingle_blank_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.numberCommaSingleTextField(""));
    }

    @Test
    public void InputValidator_numCommSingle_number_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.numberCommaSingleTextField("123"));
    }

    @Test
    public void InputValidator_numCommSingle_numberWComma_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.numberCommaSingleTextField("123,23"));
    }

    @Test
    public void InputValidator_numCommSingle_numberW2Comma_return_NONNUMERICCOMMA()
    {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA,InputValidator.numberCommaSingleTextField("12,23,23"));
    }
    @Test
    public void InputValidator_numCommSingle_letters_return_NONNUMERICCOMMA()
    {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA,InputValidator.numberCommaSingleTextField("Abc"));
    }
    @Test
    public void InputValidator_numCommSingle_numbersWletters_return_NONNUMERICCOMMA()
    {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA,InputValidator.numberCommaSingleTextField("12JK23"));
    }
    @Test
    public void InputValidator_numCommSingle_NonAlpha_return_NONNUMERICCOMMA()
    {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA,InputValidator.numberCommaSingleTextField("~ÉI3Á┌1&"));
    }
    @Test
    public void InputValidator_numCommSingle_onlyComma_return_NONNUMERICCOMMA()
    {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA,InputValidator.numberCommaSingleTextField(","));
    }
    @Test
    public void InputValidator_numCommSingle_singleNumber_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.numberCommaSingleTextField("1"));
    }

    @Test
    public void InputValidator_optText_TextWHyphen_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalAlphaPlusTextField("Hello-World"));
    }

    @Test
    public void InputValidator_compText_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.compulsoryTextField("Hello-World#$%"));
    }
    @Test
    public void InputValidator_compText_return_BLANK()
    {
        assertEquals(ValidationResult.BLANK,InputValidator.compulsoryTextField(""));
    }

    @Test
    public void InputValidator_compTextWithLengthLimit_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.compulsoryTextFieldWithLengthLimit("12345", 5));
    }

    @Test
    public void InputValidator_compTextWithLengthLimit_return_LENGTH_OVER_LIMIT()
    {
        assertEquals(ValidationResult.LENGTH_OVER_LIMIT,InputValidator.compulsoryTextFieldWithLengthLimit("123456789", 2));
    }

    @Test
    public void InputValidator_optTextWithLengthLimit_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalTextFieldWithLengthLimit("123", 4));
    }

    @Test
    public void InputValidator_optTextWithLengthLimit_return_LENGTH_OVER_LIMIT()
    {
        assertEquals(ValidationResult.LENGTH_OVER_LIMIT,InputValidator.optionalTextFieldWithLengthLimit("123456789", 2));
    }

    @Test
    public void InputValidator_optTextWithLengthLimit_blankInput_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.optionalTextFieldWithLengthLimit("", 5));
    }

    @Test
    public void InputValidator_numCommSingle_1comma0_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.numberCommaSingleTextField("1,0"));
    }

    @Test
    public void InputValidator_numCommSingle_negative1comma0_return_NON_NUMERIC_COMMA()
    {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA,InputValidator.numberCommaSingleTextField("-1,0"));
    }

    @Test
    public void InputValidator_numCommSingle_InvalidFormat1_return_NON_NUMERIC_COMMA()
    {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA,InputValidator.numberCommaSingleTextField("1-0,0"));
    }

    @Test
    public void InputValidator_numCommSingle_InvalidFormat2_return_NON_NUMERIC_COMMA()
    {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA,InputValidator.numberCommaSingleTextField("!9,0"));
    }

    @Test
    public void InputValidator_numCommSingle_InvalidFormat3_return_NON_NUMERIC_COMMA()
    {
        assertEquals(ValidationResult.NON_NUMERIC_COMMA,InputValidator.numberCommaSingleTextField("1 ! 0"));
    }


    /**
     * Test for valid names
     */
    @ParameterizedTest
    @ValueSource(strings = { "John Doe", "John-Doe", "John Doe's" })
    public void InputValidator_validateName_ValidName_return_OK(String name) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validateName(name));
    }

    /**
     * Test for invalid names
     * @param name
     */
    @ParameterizedTest
    @ValueSource(strings = { "John1", "John>", "~John" })
    public void InputValidator_validateName_InvalidName_return_INVALID_USERNAME(String name) {
        Assertions.assertEquals(ValidationResult.INVALID_USERNAME, InputValidator.validateName(name));
    }

    /**
     * Test for valid emails
     * @param email
     */
    @ParameterizedTest
    @ValueSource(strings = { "test-test@example.com", "user_123@gmail.co.nz", "john.doe@hotmail.com",
            "phlddzoxuomhdkclzinbsqhutjqhzodonrbgyxibpkutddaovmxifypmeksvhkts@mwbmmvndbnvfdskmrmmropbvhdgegssqcengjnfj" +
                    "oavhccefauucivfpthrucoyhlxfgkcdurlffpoacnhhysprommslgxmusevvpxdgkkifsgpbpiljrcxjwejestmgvnsevszck" +
                    "ujiglsrihnpblwmiculgtxodopsthkdzzpgjhznkcsarvzvubnyhutxhyyecsvjjykzxhdqlaxooxqnfbuewmajwlmvhklhzy" +
                    "wxuhsxwtnshoxuw.com" })
    public void InputValidator_validateUniqueEmail_ValidEmail_return_OK(String email) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validateUniqueEmail(email));
    }

    // Todo check if there is an email already in persistence <-- need to mock persistence for this.
    // Todo add foreign scripts

    /**
     * Test for invalid emails
     * @param email
     */
    @ParameterizedTest
    @ValueSource(strings = { " ", "user_123gmail.co.nz", "john.doe@h.","test@test.c","test@test.abcdf", "test@.com", "@test.com" })
    public void InputValidator_validateUniqueEmail_InvalidEmail_return_INVALID_EMAIL(String email) {
        Assertions.assertEquals(ValidationResult.INVALID_EMAIL, InputValidator.validateUniqueEmail(email));
    }

    /**
     * Test for valid passwords
     * @param password
     */
    @ParameterizedTest
    @ValueSource(strings = { "aB0!bbba", "##aBB0hhhhhhhhhh", "Passw0rd!","Pass word1!" })
    public void InputValidator_validatePassword_ValidPassword_return_OK(String password) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validatePassword(password));
    };

    /**
     * Test for invalid passwords
     * @param password
     */
    @ParameterizedTest
    @ValueSource(strings = { "aaa", "aaaaaaaa", "000!0000","password1!","Password123", "Password!@#", "PASSWORD1!" })
    public void InputValidator_validatePassword_InvalidPassword_return_INVALID_PASSWORD(String password){
        Assertions.assertEquals(ValidationResult.INVALID_PASSWORD, InputValidator.validatePassword(password));
    };

    /**
     * Test for valid DOB
     * @param dob
     */
    @ParameterizedTest
    @ValueSource(strings = { "01/01/2000", "01/12/1999", "31/12/2000" })
    public void InputValidator_isValidDOB_ValidDate_return_OK(String dob) {
        Assertions.assertEquals(ValidationResult.OK, InputValidator.validateDOB(dob));
    };

    /**
     * Test for invalid DOB age below 13
     * @param dob
     */
    @ParameterizedTest
    @ValueSource(strings = {"01/02/2023"})
    public void InputValidator_isValidDOB_AgeBelow13_return_AGE_BELOW_13(String dob) {
        //Todo have changing dates so test doesn't fail in 2 years
        Assertions.assertEquals(ValidationResult.AGE_BELOW_13, InputValidator.validateDOB(dob));
    }

    /**
     * Test for invalid DOB age above 120
     * @param dob
     */
    @ParameterizedTest
    @ValueSource(strings = {"01/01/1903","01/01/1850"})
    public void InputValidator_isValidDOB_AgeAbove120_return_AGE_ABOVE_120(String dob) {
        //Todo have changing dates so test doesn't fail in 2 years
        Assertions.assertEquals(ValidationResult.AGE_ABOVE_120, InputValidator.validateDOB(dob));
    }

    /**
     * Test for invalid DOB format
     * @param dob
     */
    @ParameterizedTest
    @ValueSource(strings = {"1960/3/2", "Steve","12122013","12:12:2014","12-12-2014"})
    public void InputValidator_isValidDOB_invalidFormat_return_INVALID_DATE_FORMAT(String dob) {
        //Todo have changing dates so test doesn't fail in 2 years
        Assertions.assertEquals(ValidationResult.INVALID_DATE_FORMAT, InputValidator.validateDOB(dob));
    }


}
