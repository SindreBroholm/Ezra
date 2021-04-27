package no.sbs.ezra.data.repositories;

import no.sbs.ezra.data.FamilyData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface FamilyDataRepository extends CrudRepository<FamilyData, Integer> {



    List<FamilyData> findAllByUserOneIdOrUserTwoIdAndAreFamily(int userOneId, int userTwoId, boolean areFamily);

    List<FamilyData> findAllByUserOneIdOrUserTwoIdAndPendingRequest(int userOneId, int userTwoId, boolean pendingRequest);

    FamilyData findByFamilyId(String familyId);

}
