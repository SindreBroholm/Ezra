package no.sbs.ezra.data;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Entity
@Data
@RequiredArgsConstructor
public class BoardData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty
    @NonNull
    @Unique
    @Size(min = 3, max = 100)
    private String name;

    @NotEmpty
    @Size(min = 2, max = 100)
    private String contactName;


    @Size(max = 15, min = 6)
    private String contactNumber;

    @NotEmpty
    @NonNull
    @Size(min = 5, max = 320)
    @Email
    private String contactEmail;

    @Size(max = 100)
    private String homepage;

    public BoardData() {
    }

    public BoardData(@NotEmpty @NonNull @Unique @Size(min = 3, max = 100) String name,
                     @NotEmpty @Size(min = 2, max = 100) String contactName,
                     @Size(max = 15, min = 6) String contactNumber,
                     @NotEmpty @NonNull @Size(min = 5, max = 320) @Email String contactEmail,
                     @Size(max = 100) String homepage) {
        this.name = name;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.contactEmail = contactEmail;
        this.homepage = homepage;
    }
}
