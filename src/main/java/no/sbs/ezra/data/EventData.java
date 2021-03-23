package no.sbs.ezra.data;

import lombok.Data;
import no.sbs.ezra.security.UserPermission;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Data
public class EventData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private BoardData board;

    @Size(max = 5000, message = "Description is to long..")
    private String message;

    @NotNull
    @FutureOrPresent(message = "The event must end in the present or future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime datetime_from;

    @NotNull
    @FutureOrPresent(message = "The event must end in the present or future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime datetime_to;

    private LocalDateTime datetime_created = LocalDateTime.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserPermission membershipType;

    private String location;

    @NotEmpty
    @Size(max = 150)
    private String name;
}
