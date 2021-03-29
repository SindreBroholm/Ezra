package no.sbs.ezra.data.repositories;

import no.sbs.ezra.data.UserData;
import no.sbs.ezra.security.auth.CustomUserDetails;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserDataRepository extends CrudRepository<UserData, Integer> {

    UserData findByEmail(String username);
}
