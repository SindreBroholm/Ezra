package no.sbs.ezra.data;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Entity
@Data
@RequiredArgsConstructor
public class BoardData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    @Size(min = 3, max = 100)
    private String name;

    @NonNull
    @Size(min = 2, max = 100)
    private String contactName;


    @Size(max = 15, min = 6)
    private String contactNumber;


    @NonNull
    @Size(min = 5, max = 320)
    @Email
    private String contactEmail;

    @Size(max = 100)
    private String homepage;

    public BoardData() {
    }

    public BoardData(@NonNull String name, @NonNull String contactName,
                     String contactNumber, @NonNull String contactEmail,
                     String homepage) {
        this.name = name;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.contactEmail = contactEmail;
        this.homepage = homepage;
    }
}
