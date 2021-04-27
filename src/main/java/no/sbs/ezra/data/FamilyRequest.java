package no.sbs.ezra.data;


import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@RequiredArgsConstructor
public class FamilyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private UserData user;

    @NonNull
    private String memberEmail;

    private boolean haveJoined;

    public FamilyRequest() {
    }

    public FamilyRequest(UserData user, @NonNull String memberEmail, boolean haveJoined) {
        this.user = user;
        this.memberEmail = memberEmail;
        this.haveJoined = haveJoined;
    }
}
