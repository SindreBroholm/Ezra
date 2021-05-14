package no.sbs.ezra.servises;

import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.EventData;
import no.sbs.ezra.data.repositories.EventDataRepository;
import no.sbs.ezra.security.UserPermission;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PermissionService {

    private final EventDataRepository eventDataRepository;

    public PermissionService(EventDataRepository eventDataRepository) {
        this.eventDataRepository = eventDataRepository;
    }

    public List<EventData> getAllEventsFromBoardByUserPermission(BoardData board, UserPermission permission) {
        List<EventData> events = new ArrayList<>();
        switch (permission.getPermission()) {
            case "master":
                events.addAll(eventDataRepository.findAllByBoardIdAndAndMembershipType(board.getId(), UserPermission.MASTER));
            case "admin":
                events.addAll(eventDataRepository.findAllByBoardIdAndAndMembershipType(board.getId(), UserPermission.ADMIN));
            case "member":
                events.addAll(eventDataRepository.findAllByBoardIdAndAndMembershipType(board.getId(), UserPermission.MEMBER));
            case "follower":
                events.addAll(eventDataRepository.findAllByBoardIdAndAndMembershipType(board.getId(), UserPermission.FOLLOWER));
            case "visitor":
                events.addAll(eventDataRepository.findAllByBoardIdAndAndMembershipType(board.getId(), UserPermission.VISITOR));
        }
        if (!events.isEmpty()) {
            events.removeIf(e -> e.getDatetime_to().isBefore(LocalDateTime.now()));

            events.sort(
                    Comparator.comparing(EventData::getDatetime_from)
                            .thenComparing(EventData::getDatetime_to)
            );
        }
        return events;
    }

    public boolean doesUserHaveAdminRights(UserPermission permission) {
        switch (permission.getPermission()) {
            case "master":
            case "admin" : {
                return true;
            }
            default: {
                return false;
            }
        }
    }
}
