package no.sbs.ezra.data.repositories;

import no.sbs.ezra.data.FamilyData;
import no.sbs.ezra.data.UserData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface FamilyDataRepository extends CrudRepository<FamilyData, Integer> {

    @Query(value = "select fd from FamilyData fd where fd.areFamily = true and fd.userOne = ?1 or fd.userTwo = ?1")
    List<FamilyData> addAllFamilyMembersToUser(UserData user);

    @Query(value = "select fd from FamilyData fd where fd.pendingRequest = true and fd.userOne = ?1 or fd.userTwo = ?1")
    List<FamilyData> addAllPendingFamilyRequests(UserData user);

    FamilyData findByFamilyId(String familyId);

}
