package nz.ac.canterbury.seng302.gardenersgrove;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.ValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputValidatorTest {


    @Test
    public void InputValidator_compText_validString_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.compulsoryAlphaPlusTextField("Hello"));
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

}
