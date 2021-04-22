package no.sbs.ezra.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.sbs.ezra.security.UserPermission;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@RequiredArgsConstructor
public class EventData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private BoardData board;


    @NotNull
    private String message;

    @NotNull(message = "Please select when the event starts")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime datetime_from;

    @NotNull(message = "Please select when the event ends")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime datetime_to;

    @NotNull
    private LocalDateTime datetime_created = LocalDateTime.now();

    @NotNull(message = "Please select who this event is for")
    @Enumerated(EnumType.STRING)
    private UserPermission membershipType;


    private String location;

    @NotNull
    private String eventName;

    public EventData(BoardData board, @NotNull String message,
                     @NotNull LocalDateTime datetime_from, @NotNull LocalDateTime datetime_to,
                     @NotNull LocalDateTime datetime_created, @NotNull UserPermission membershipType,
                     String location, @NotNull String eventName) {
        this.board = board;
        this.message = message;
        this.datetime_from = datetime_from;
        this.datetime_to = datetime_to;
        this.datetime_created = datetime_created;
        this.membershipType = membershipType;
        this.location = location;
        this.eventName = eventName;
    }
}
