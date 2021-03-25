package no.sbs.ezra.data;


import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.*;

@Entity
@Data
@RequiredArgsConstructor
public class UserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Email
    @NotEmpty(message = "")
    @NonNull
    private String email;

    @NotEmpty(message = "")
    @Size(min = 2, max = 150)
    @NonNull
    private String firstname;

    @NotEmpty(message = "")
    @Size(min = 2, max = 150)
    @NonNull
    private String lastname;

    @NotEmpty(message = "")
    @Size(min = 6, max = 300)
    @NonNull
    private String password;

    @NotEmpty(message = "")
    @Size(max = 15, min = 6)
    private String phone_number;

    public UserData(@Email @NotEmpty @NonNull String email,
                    @NotEmpty @Size(min = 2, max = 150) @NonNull String firstname,
                    @NotEmpty @Size(min = 2, max = 150) @NonNull String lastname,
                    @NotEmpty @Size(min = 6, max = 300) @NonNull String password,
                    @NotEmpty @Size(max = 15, min = 6) String phone_number) {
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.phone_number = phone_number;
    }

    public UserData() {

    }
}
