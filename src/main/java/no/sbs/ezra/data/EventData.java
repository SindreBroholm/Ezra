package no.sbs.ezra.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class EventData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @NotNull
    private BoardData board;

    @Size(max = 5000)
    @NotNull
    private String message;

    @NotNull
    @FutureOrPresent
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime datetime_from;

    @NotNull
    @FutureOrPresent
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime datetime_to;

    @NotNull
    private LocalDateTime datetime_created = LocalDateTime.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserPermission membershipType;

    private String location;

    @NotEmpty
    @Size(max = 150)
    @NotNull
    private String name;

    public EventData(@NotNull BoardData board,
                     @Size(max = 5000) @NotNull String message,
                     @NotNull @FutureOrPresent LocalDateTime datetime_from,
                     @NotNull @FutureOrPresent LocalDateTime datetime_to,
                     @NotNull LocalDateTime datetime_created,
                     @NotNull UserPermission membershipType,
                     String location,
                     @NotEmpty @Size(max = 150) @NotNull String name) {
        this.board = board;
        this.message = message;
        this.datetime_from = datetime_from;
        this.datetime_to = datetime_to;
        this.datetime_created = datetime_created;
        this.membershipType = membershipType;
        this.location = location;
        this.name = name;
    }
}
