package no.sbs.ezra.data;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@RequiredArgsConstructor
public class FamilyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private UserData user;

    @ManyToOne
    private UserData member;

    private boolean pendingRequest;

    @NonNull
    private Integer familyId;


    public FamilyData(UserData user, UserData member,
                      boolean pendingRequest,
                      @NonNull Integer familyId) {
        this.user = user;
        this.member = member;
        this.pendingRequest = pendingRequest;
        this.familyId = familyId;
    }

    public FamilyData() {

    }
}
