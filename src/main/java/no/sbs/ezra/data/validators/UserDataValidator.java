package no.sbs.ezra.data.validators;

import lombok.NonNull;
import no.sbs.ezra.data.UserData;

import no.sbs.ezra.data.repositories.UserDataRepository;
import no.sbs.ezra.security.PasswordConfig;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.security.Principal;

public class UserDataValidator implements Validator {

    private final UserDataRepository repository;
    private final Principal principal;
    private final PasswordConfig encoder;

    public UserDataValidator(UserDataRepository repository, Principal principal, PasswordConfig encoder) {
        this.repository = repository;
        this.principal = principal;
        this.encoder = encoder;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UserData.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {
        UserData user = (UserData) o;
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            if (principal != null){
                if (!repository.findByEmail(user.getEmail()).equals(repository.findByEmail(principal.getName()))){
                    errors.rejectValue("email", "email.error", "E-mail not available");
                }
            } else {
                errors.rejectValue("email", "email.error", "E-mail not available");
            }
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"email", "email.error", "Please enter an e-mail");

        if (user.getFirstname().length() < 2) {
            errors.rejectValue("firstname", "firstname.error", "First name is to short");
        }
        if (user.getFirstname().length() > 150) {
            errors.rejectValue("firstname", "firstname.error", "First name is to long");
        }

        if (user.getLastname().length() < 2) {
            errors.rejectValue("lastname", "lastname.error", "Last name is to short");
        }
        if (user.getLastname().length() > 150) {
            errors.rejectValue("lastname", "lastname.error", "Last name is to long");
        }


        if (user.getPassword().length() < 6) {
            if (principal == null){
                errors.rejectValue("password", "password.error", "Password is to short");
            }
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
        if (!user.getPhone_number().matches("^[0-9]*$")){
            errors.rejectValue("phone_number", "phone_number.error", "Phone number must be a number");
        }

    }
}
