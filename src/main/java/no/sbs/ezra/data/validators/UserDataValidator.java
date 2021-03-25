package no.sbs.ezra.data.validators;

import lombok.NonNull;
import no.sbs.ezra.data.UserData;

import no.sbs.ezra.repositories.UserDataRepository;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UserDataValidator implements Validator {

    private final UserDataRepository repository;

    public UserDataValidator(UserDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UserData.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "email.empty", "Pleas enter a e-mail");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstname", "firstname.empty", "Pleas enter a firstname");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastname", "lastname.empty", "Pleas enter a lastname");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.empty", "Pleas enter a password");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone_number", "phone_number.empty", "Pleas enter a phone number");

        UserData user = (UserData) o;
        if (user.getFirstname().length() < 2) {
            errors.rejectValue("firstname", "firstname.error", "firstname to short");
            errors.rejectValue("firstname", "firstname.error", "Firstname is to short");
        }
        if (user.getFirstname().length() > 150) {
            errors.rejectValue("firstname", "firstname.error", "Firstname is to long");
        }
        if (user.getLastname().length() < 2) {
            errors.rejectValue("lastname", "lastname.error", "Lastname is to short");
        }
        if (user.getLastname().length() > 150) {
            errors.rejectValue("lastname", "lastname.error", "Lastname is to long");
        }
        if (user.getPassword().length() < 6) {
            errors.rejectValue("password", "password.error", "Password is to short");
        }
        if (user.getPassword().length() > 300) {
            errors.rejectValue("password", "password.error", "Password is to long");
        }
        if (user.getPhone_number().length() < 6) {
            errors.rejectValue("phone_number", "phone_number.error", "Phone number is to short");
        }
        if (user.getPhone_number().length() > 15) {
            errors.rejectValue("phone_number", "phone_number.error", "Phone number is to long");
        }

        if (repository.findByEmail(user.getEmail()) != null) {
            errors.rejectValue("email", "email.error", "E-mail not available, forgot password?");
        }
    }
}
