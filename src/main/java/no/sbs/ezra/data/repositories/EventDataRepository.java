package no.sbs.ezra.data.repositories;

import no.sbs.ezra.data.EventData;
import no.sbs.ezra.security.UserPermission;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventDataRepository extends CrudRepository<EventData, Integer> {

    List<EventData> findAllByBoardIdAndAndMembershipType(int boardDataId, UserPermission membershipType);

    List<EventData> findAllByBoardId(Integer boardId);
}
