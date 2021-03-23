package no.sbs.ezra.data;


import lombok.Data;
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
    private String email;

    @Size(min = 2, max = 150)
    @NotEmpty
    private String firstname;

    @NotEmpty
    @Size(min = 2, max = 150)
    private String lastname;

    @Size(min = 6, max = 300)
    @NotEmpty
    private String password;

    @Size(max = 15, min = 6)
    @NotEmpty
    private String phone_number;


    public UserData(@Email @NotEmpty(message = "") String email,
                    @Size(min = 2, max = 150) @NotEmpty String firstname,
                    @NotEmpty @Size(min = 2, max = 150) String lastname,
                    @Size(min = 6, max = 300) @NotEmpty String password,
                    @Size(max = 15, min = 6) @NotEmpty String phone_number) {
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.phone_number = phone_number;
    }
}
