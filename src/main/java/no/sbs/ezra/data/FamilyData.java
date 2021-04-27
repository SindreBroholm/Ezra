package no.sbs.ezra.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@RequiredArgsConstructor
public class FamilyData {



    @JoinColumn(name = "user_one", referencedColumnName = "id")
    @ManyToOne(targetEntity = UserData.class)
    private UserData userOne;


    @JoinColumn(name = "user_two", referencedColumnName = "id")
    @ManyToOne(targetEntity = UserData.class)
    private UserData userTwo;

    @Id
    private String familyId;

    private boolean pendingRequest;

    private boolean areFamily;


    /*
    * The user with the lowest ID will be set to userOne
    * the familyId will be set to "userOneId_userTwoId" = "1_2" <-- example.
    * Always get familyData based on familyId
    * */
    public FamilyData(UserData userOne, UserData userTwo, boolean pendingRequest, boolean areFamily) {
        this.pendingRequest = pendingRequest;
        this.areFamily = areFamily;
        this.userOne = userOne;
        this.userTwo = userTwo;

        if (userOne.getId() > userTwo.getId()){
            this.familyId = userTwo.getId() + "_" + userOne.getId();
        } else {
            this.familyId = userOne.getId() + "_" + userTwo.getId();
        }
    }

}
