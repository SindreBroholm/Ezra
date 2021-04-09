package no.sbs.ezra.data.validators;

import lombok.NonNull;
import no.sbs.ezra.data.EventData;
import no.sbs.ezra.data.repositories.EventDataRepository;
import no.sbs.ezra.security.UserPermission;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.util.Locale;

public class EventDataValidator implements Validator {


    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return EventData.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {
        EventData event = (EventData) o;

        if (event.getBoard() == null){
            errors.rejectValue("board", "board.error", "Invalid value");
        }


        if (event.getMessage().length() > 5000) {
            errors.rejectValue("message", "message.error", "Event description is to long");
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "message", "message.error", "Event description is empty");


        if (event.getDatetime_from() != null && event.getDatetime_to() != null){
            if (event.getDatetime_from().isBefore(LocalDateTime.now())){
                errors.rejectValue("datetime_from", "datetime_from.error", "Event start is set in the past");
            }
            if (event.getDatetime_to().isBefore(event.getDatetime_from())){
                errors.rejectValue("datetime_to", "datetime_to.error", "Event cant end before it has started");
            }
        }


        if (event.getMembershipType() != null){
            /*if (event.getMembershipType()){
                errors.rejectValue("membershipType", "membershipType.error", "Invalid value");
            }*/
            if (event.getMembershipType().getPermission().length() > 150){
                errors.rejectValue("membershipType", "membershipType.error", "Invalid value");
            }
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "membershipType", "membershipType.error", "Invalid value");
        }

        if (event.getLocation().length() > 150){
            errors.rejectValue("eventName", "eventName.error", "Event name is to long");
        }

        if (event.getEventName().length() > 150){
            errors.rejectValue("eventName", "eventName.error", "Event name is to long");
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "eventName", "eventName.error", "Event name is empty");



    }
}
