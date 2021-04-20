package no.sbs.ezra.data.repositories;

import no.sbs.ezra.data.UserRole;
import no.sbs.ezra.data.UserRoleId;
import no.sbs.ezra.security.UserPermission;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRoleRepository extends CrudRepository<UserRole, UserRoleId> {


    List<UserRole> findAllByUserId(int userDataId);
    List<UserRole> findAllByBoardId(int boardId);
    List<UserRole> findAllByBoardIdAndMembershipTypeOrMembershipType(int boardId, UserPermission up1, UserPermission up2);
    List<UserRole> findAllByBoardIdAndMembershipTypeAndPendingMember(int boardId, UserPermission up1, boolean isTrue);

    UserRole findByBoardIdAndUserId(int boardId, int UserId);
}
