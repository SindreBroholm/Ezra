package no.sbs.ezra.servises;

import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.EventData;
import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.UserRole;
import no.sbs.ezra.data.repositories.BoardDataRepository;
import no.sbs.ezra.data.repositories.UserRoleRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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

    public JSONArray getAllEventsToUser(List<UserData> fam, UserData user){
        List<UserRole> listOfBoards = userRoleRepository.findAllByUserId(user.getId());
        List<EventData> events = new ArrayList<>();
        JSONArray array = new JSONArray();
        HashMap<Integer, Boolean> familyMembersPersonalBoards = getFamilyMembersPersonalBoards(fam);

        for (UserRole ur :
                listOfBoards) {
            try{
                if (!familyMembersPersonalBoards.containsKey(ur.getBoard().getId())){
                    if (boardDataRepository.findById(ur.getBoard().getId()).isPresent()){
                        events.addAll(
                                permissionService.getAllEventsFromBoardByUserPermission(
                                        boardDataRepository.findById(ur.getBoard().getId()).get(),
                                        userRoleRepository.findByBoardIdAndUserId(ur.getBoard().getId(), user.getId()).getMembershipType()));
                    }
                }
            } catch (Exception Ignore){}
        }
        array.addAll(setEventDataToJSONObject(events));
        return array;
    }

    public JSONArray getAllEventsToUsersFamilyMembers(List<UserData> fam, UserData user){
        JSONArray array = new JSONArray();
        HashMap<Integer, Boolean> familyMembersBoard = getFamilyMembersPersonalBoards(fam);
        List<UserRole> familyUserRoles = getFamilyUserRoles(fam);

        for (UserRole ur :
                familyUserRoles) {
            try{
                if (boardDataRepository.findById(ur.getBoard().getId()).isPresent()){
                    if (ur.getUser() != user){
                        if (!boardDataRepository.findById(ur.getBoard().getId()).get().isPrivateBoard() || familyMembersBoard.containsKey(ur.getBoard().getId())){
                            UserData famMember = ur.getUser();
                            BoardData board = ur.getBoard();
                            List<EventData> famEvents = permissionService.getAllEventsFromBoardByUserPermission(board, ur.getMembershipType());
                            array.addAll(getFamilyMembersEvents(famEvents, famMember));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return array;
    }

    private List<UserRole> getFamilyUserRoles(List<UserData> fam) {
        List<UserRole> familyUserRoles = new ArrayList<>();
        for (UserData ud :
                fam) {
            familyUserRoles = userRoleRepository.findAllByUserId(ud.getId());
        }
        return familyUserRoles;
    }
    private HashMap<Integer, Boolean> getFamilyMembersPersonalBoards(List<UserData> fam) {
        HashMap<Integer, Boolean> familyMembersPersonalBoards = new HashMap<>();
        for (UserData ud :
                fam) {
            familyMembersPersonalBoards.put(ud.getMyBoardId(), true);
        }
        return familyMembersPersonalBoards;
    }
    private JSONArray getFamilyMembersEvents(List<EventData> famEvents, UserData famMember) {
        JSONArray array = new JSONArray();
        for (EventData ed :
                famEvents) {
            JSONObject object = new JSONObject();
            object.put("familyMember", famMember.getFirstname() + " " + famMember.getLastname());
            object.put("textColor", "red");
            putEventInfo(ed, object);
            array.add(object);
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
            } catch (Exception Ignore){}
        }
        return array;
    }
    private void putEventInfo(EventData ed, JSONObject object) {
        object.put("title", ed.getEventName());
        object.put("start", ed.getDatetime_from().toString());
        object.put("end", ed.getDatetime_to().toString());
        object.put("location", ed.getLocation());
        object.put("description", ed.getMessage());
        object.put("boardName", ed.getBoard().getName());
        object.put("display", "list-item");
    }

}
