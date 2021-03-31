package no.sbs.ezra.data.validators;

import lombok.NonNull;
import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.repositories.BoardDataRepository;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class BoardDataValidator implements Validator {

    private final BoardDataRepository repository;

    public BoardDataValidator(BoardDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return BoardData.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"name", "name.empty", "The board must have a name");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"contactName", "contactName.empty", "Pleas enter the admins name");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"contactNumber", "contactNumber.empty", "contact number cant be empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"contactEmail", "contactEmail.empty", "contact e-mail cant be empty");

        BoardData board = (BoardData) o;
        if (repository.findByName(board.getName()) != null){
            errors.rejectValue("name", "name.error", "That name is already taken");
        }
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
    }
}
