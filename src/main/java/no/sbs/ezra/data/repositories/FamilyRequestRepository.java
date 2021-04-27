package no.sbs.ezra.data.repositories;

import no.sbs.ezra.data.FamilyRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FamilyRequestRepository extends CrudRepository<FamilyRequest, Integer> {

    Optional<FamilyRequest> findByMemberEmail(String email);
    Optional<FamilyRequest> findByUserIdAndMemberEmail(Integer userId, String email);
}
