package no.sbs.ezra.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.sbs.ezra.security.UserPermission;

import javax.persistence.*;

@Entity
@Data
@IdClass(UserRoleId.class)
@AllArgsConstructor
public class UserRole {


    @Id
    @JoinColumn(insertable = false, updatable = false)
    @ManyToOne(targetEntity = UserData.class)
    private UserData user;

    @Id
    @JoinColumn(insertable = false, updatable = false)
    @ManyToOne(targetEntity = BoardData.class)
    private BoardData board;

    @Enumerated(EnumType.STRING)
    private UserPermission membershipType;

    private boolean pendingMember = false;

    public UserRole() {

    }
}
