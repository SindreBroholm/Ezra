package no.sbs.ezra.data;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class ResetPasswordData {
    private static final int EXPIRATION = 60*26;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = UserData.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserData user;

    private Date expiryDate;



}
