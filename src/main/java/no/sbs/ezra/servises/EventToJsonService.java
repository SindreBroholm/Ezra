package no.sbs.ezra.servises;

import no.sbs.ezra.data.EventData;
import no.sbs.ezra.data.UserRole;
import no.sbs.ezra.data.repositories.BoardDataRepository;
import no.sbs.ezra.data.repositories.UserRoleRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventToJsonService {

    private final UserRoleRepository userRoleRepository;
    private final EventPermissionService eventPermissionService;
    private final BoardDataRepository boardDataRepository;


    public EventToJsonService(UserRoleRepository userRoleRepository, EventPermissionService eventPermissionService,
                              BoardDataRepository boardDataRepository) {
        this.userRoleRepository = userRoleRepository;
        this.eventPermissionService = eventPermissionService;
        this.boardDataRepository = boardDataRepository;
    }


    /*
    * Required format =
    * {
    *   title: 'my event',
    *   start: 'YYYY-MM-DDTHH:MM:SS',
    *   end: 'YYYY-MM-DDTHH:MM:SS',
    *   display: 'list-item'
    * }
    *  */

    public JSONArray getAllEventsToUser(int userDataId){
        List<UserRole> listOfBoards = userRoleRepository.findAllByUserId(userDataId);
        List<EventData> events = new ArrayList<>();
        JSONArray array = new JSONArray();

        for (UserRole ur :
                listOfBoards) {
            events.addAll(
                    eventPermissionService.getAllEventsFromBoardByUserPermission(
                            boardDataRepository.findById(ur.getBoardId()).get(),
                            userRoleRepository.findByBoardIdAndUserId(ur.getBoardId(), userDataId).getMembershipType()));
        }

        for (EventData ed :
                events) {
            JSONObject object = new JSONObject();
                object.put("title", ed.getEventName());
                object.put("id", ed.getId());
                object.put("start", ed.getDatetime_from().toString());
                object.put("end", ed.getDatetime_to().toString());
                object.put("location", ed.getLocation());
                object.put("description", ed.getMessage());
                object.put("boardName", ed.getBoard().getName());
                object.put("display", "list-item");
                array.add(object);
        }
        return array;
    }


}
