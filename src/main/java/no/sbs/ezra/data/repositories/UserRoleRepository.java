package no.sbs.ezra.data.repositories;

import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.UserRole;
import no.sbs.ezra.data.UserRoleId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRoleRepository extends CrudRepository<UserRole, UserRoleId> {

    List<UserRole> findAllByUserId(int userDataId);

}