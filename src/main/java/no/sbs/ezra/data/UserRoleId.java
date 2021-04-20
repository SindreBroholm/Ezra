package no.sbs.ezra.data;


import java.io.Serializable;
import java.util.Objects;


public class UserRoleId implements Serializable {

    //This class handles the composite key of user_role in the database


    private Integer user;
    private Integer board;

    public UserRoleId() {
    }

    public UserRoleId(Integer user, Integer board) {
        this.user = user;
        this.board = board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleId userRoleId = (UserRoleId) o;
        return user.equals(userRoleId.user) &&
                board.equals(userRoleId.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, board);
    }
}

