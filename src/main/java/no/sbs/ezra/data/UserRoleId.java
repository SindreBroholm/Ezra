package no.sbs.ezra.data;

import java.io.Serializable;
import java.util.Objects;

public class UserRoleId implements Serializable {

    //This class handles the composite key of user_role in the database

    private Integer userId;
    private Integer boardId;

    public UserRoleId() {
    }

    public UserRoleId(Integer userId, Integer boardId) {
        this.userId = userId;
        this.boardId = boardId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleId userRoleId = (UserRoleId) o;
        return userId.equals(userRoleId.userId) &&
                boardId.equals(userRoleId.boardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, boardId);
    }
}

