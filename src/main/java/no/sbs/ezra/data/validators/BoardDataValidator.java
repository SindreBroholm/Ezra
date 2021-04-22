package no.sbs.ezra.data.validators;

import lombok.NonNull;
import no.sbs.ezra.data.BoardData;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class BoardDataValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return BoardData.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {

        BoardData board = (BoardData) o;

        if (board.getName().length() < 3){
            errors.rejectValue("name", "name.error", "The name is to short");
        }
        if (board.getName().length() > 100){
            errors.rejectValue("name", "name.error", "The name is to long");
        }
        if (board.getContactName().length() < 2){
            errors.rejectValue("contactName", "contactName.error", "Contact name is to short");
        }
        if (board.getContactName().length() > 100){
            errors.rejectValue("contactName", "contactName.error", "Contact name is to long");
        }
        if (board.getContactNumber().length() < 6){
            errors.rejectValue("contactNumber", "contactNumber.error", "Contact number is to short");
        }
        if (board.getContactNumber().length() > 15){
            errors.rejectValue("contactNumber", "contactNumber.error", "Contact number is to long");
        }
        if (!board.getContactNumber().matches("^[0-9]*$")){
            errors.rejectValue("contactNumber", "contactNumber.error", "Contact number must be a number");
        }
        if (board.getContactEmail().length() < 5){
            errors.rejectValue("contactEmail", "contactEmail.error", "Contact e-mail is to short");
        }
        if (board.getContactEmail().length() > 320){
            errors.rejectValue("contactEmail", "contactEmail.error", "Contact e-mail is to long");
        }
        if (board.getHomepage().length() > 100){
            errors.rejectValue("homepage", "homepage.error", "Homepage address is to long");
        }
        if (board.getDescription() != null){
            if (board.getDescription().length() > 5000){
                errors.rejectValue("description", "description.error", "Description is to long");
            }
        }
    }
}
