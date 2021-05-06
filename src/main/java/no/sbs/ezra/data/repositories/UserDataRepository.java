package no.sbs.ezra.data.repositories;

import no.sbs.ezra.data.UserData;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserDataRepository extends CrudRepository<UserData, Integer> {

    Optional<UserData> findByEmail(String username);
}
