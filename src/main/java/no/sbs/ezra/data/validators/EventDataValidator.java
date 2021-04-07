package no.sbs.ezra.data.validators;

import lombok.NonNull;
import no.sbs.ezra.data.EventData;
import no.sbs.ezra.data.repositories.EventDataRepository;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

public class EventDataValidator implements Validator {

    private final EventDataRepository eventDataRepository;

    public EventDataValidator(EventDataRepository eventDataRepository) {
        this.eventDataRepository = eventDataRepository;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return EventData.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "message", "message.empty", "Description is Empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "datetime_from", "datetime_from.empty", "An event must have a start time");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "datetime_to", "datetime_to.empty", "An event must have a ending time");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "datetime_created", "datetime_created.empty", "Failed to timestamp event");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "membershipType", "membershipType.empty", "You must specify who this event is for");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "eventName", "eventName.empty", "The event must have a name");

        EventData event = (EventData) o;

        if (event.getMessage().length() > 5000) {
            errors.rejectValue("message", "message.error", "Event description is to long");
        }
        if (event.getDatetime_from().isBefore(LocalDateTime.now())){
            errors.rejectValue("datetime_from", "datetime_from.error", "Event start is set in the past");
        }
        if (event.getDatetime_to().isBefore(event.getDatetime_from())){
            errors.rejectValue("datetime_to", "datetime_to.error", "Event cant end before it has started");
        }
        if (event.getEventName().length() > 150){
            errors.rejectValue("eventName", "eventName.error", "Event name is to long");
        }

    }
}
