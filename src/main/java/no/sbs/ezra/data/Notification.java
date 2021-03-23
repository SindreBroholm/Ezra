package no.sbs.ezra.data;

import lombok.Data;
import no.sbs.ezra.security.UserPermission;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private BoardData board;

    private String message;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime datetimeCreated;

    @Enumerated(EnumType.STRING)
    private UserPermission notificationType;

}
