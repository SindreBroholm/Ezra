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
    @Column(insertable = false, updatable = false)
    private Integer userId;

    @Id
    @Column(insertable = false, updatable = false)
    private Integer boardId;

    @Enumerated(EnumType.STRING)
    private UserPermission membershipType;

    private boolean pendingMember = false;

    public UserRole() {

    }
}
