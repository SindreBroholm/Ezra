package no.sbs.ezra.data;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;

@Entity
@Data
@RequiredArgsConstructor
public class BoardData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    private String name;

    @NonNull
    private String contactName;

    private String contactNumber;

    @NonNull
    @Email
    private String contactEmail;

    private String homepage;

    private String description;

    public BoardData() {
    }

    public BoardData(@NonNull String name, @NonNull String contactName,
                     String contactNumber, @NonNull String contactEmail,
                     String homepage, String description) {
        this.name = name;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.contactEmail = contactEmail;
        this.homepage = homepage;
        this.description = description;
    }

}
