package no.sbs.ezra.servises;

import no.sbs.ezra.data.EventData;
import no.sbs.ezra.data.UserRole;
import no.sbs.ezra.data.repositories.BoardDataRepository;
import no.sbs.ezra.data.repositories.UserRoleRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventToJsonService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRoleRepository userRoleRepository;
    private final PermissionService permissionService;
    private final BoardDataRepository boardDataRepository;


    public EventToJsonService(UserRoleRepository userRoleRepository, PermissionService permissionService,
                              BoardDataRepository boardDataRepository) {
        this.userRoleRepository = userRoleRepository;
        this.permissionService = permissionService;
        this.boardDataRepository = boardDataRepository;
    }


    /*
    * document: https://fullcalendar.io/docs/event-object
    * note extendedProps -> "Receives properties in the explicitly given extendedProps hash as well as other non-standard properties."
    *  */

    public JSONArray getAllEventsToUser(int userDataId){
        List<UserRole> listOfBoards = userRoleRepository.findAllByUserId(userDataId);
        List<EventData> events = new ArrayList<>();
        JSONArray array = new JSONArray();
        for (UserRole ur :
                listOfBoards) {
            try{
            events.addAll(
                    permissionService.getAllEventsFromBoardByUserPermission(
                            boardDataRepository.findById(ur.getBoard().getId()).get(),
                            userRoleRepository.findByBoardIdAndUserId(ur.getBoard().getId(), userDataId).getMembershipType()));

                array.addAll(setEventDataToJSONObject(events));
            } catch (Exception Ignore){
            }
        }
        return array;
    }

    public JSONArray getAllEventsToUsersFamilyMembers(List<Integer> familyMembersId){
        List<EventData> events = new ArrayList<>();
        List<UserRole> familyUserRoles = new ArrayList<>();

        for (Integer i :
                familyMembersId) {
            familyUserRoles.addAll(userRoleRepository.findAllByUserId(i));
        }
        JSONArray array = new JSONArray();
        for (UserRole ur :
                familyUserRoles) {
            try{
                if (!boardDataRepository.findById(ur.getBoard().getId()).get().isPrivateBoard()){
                    events.addAll(

                            permissionService.getAllEventsFromBoardByUserPermission(
                                    boardDataRepository.findById(ur.getBoard().getId()).get(),
                                    userRoleRepository.findByBoardIdAndUserId(ur.getBoard().getId(), ur.getUser().getId()).getMembershipType()));

                    array.addAll(SetEventDataToJSONObjectForFamilyMembers(events, ur));
                }
            } catch (Exception Ignore) {
            }
        }
        return array;
    }



    private JSONArray setEventDataToJSONObject(List<EventData> events) {
        JSONArray array = new JSONArray();
        for (EventData ed :
                events) {
            JSONObject object = new JSONObject();
            putEventInfo(ed, object);
            object.put("familyMember", "you");
            try{
                array.add(object);
            } catch (Exception Ignore){
            }
        }
        return array;
    }

    private JSONArray SetEventDataToJSONObjectForFamilyMembers(List<EventData> events, UserRole ur) {
        JSONArray array = new JSONArray();
        for (EventData ed :
                events) {
            JSONObject object = new JSONObject();
            putEventInfo(ed, object);
            object.put("familyMember", ur.getUser().getFirstname() + " " + ur.getUser().getLastname());
            object.put("textColor", "red");
            try{
                array.add(object);
            } catch (Exception e){
                logger.error("EventToJsonService\n" + e.getMessage());
            }
        }
        return array;
    }

    private void putEventInfo(EventData ed, JSONObject object) {
        object.put("title", ed.getEventName());
        /*object.put("id", ed.getId());*/
        object.put("start", ed.getDatetime_from().toString());
        object.put("end", ed.getDatetime_to().toString());
        object.put("location", ed.getLocation());
        object.put("description", ed.getMessage());
        object.put("boardName", ed.getBoard().getName());
        object.put("display", "list-item");
    }

}
