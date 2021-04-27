package no.sbs.ezra.data;


import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Data
@RequiredArgsConstructor
public class UserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    @Email(message = "Please enter a valid e-mail address")
    private String email;

    @NonNull
    private String firstname;

    @NonNull
    private String lastname;

    @NonNull
    private String password;

    private String phone_number;

    public UserData(@NonNull String email, @NonNull String firstname,
                    @NonNull String lastname, @NonNull String password,
                    String phone_number) {
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.phone_number = phone_number;
    }

    public UserData() {}
}
