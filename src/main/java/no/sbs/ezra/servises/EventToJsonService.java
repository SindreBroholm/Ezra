package no.sbs.ezra.servises;

import no.sbs.ezra.data.EventData;
import no.sbs.ezra.data.UserRole;
import no.sbs.ezra.data.repositories.EventDataRepository;
import no.sbs.ezra.data.repositories.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventToJsonService {

    private final EventDataRepository eventDataRepository;
    private final UserRoleRepository userRoleRepository;


    public EventToJsonService(EventDataRepository eventDataRepository, UserRoleRepository userRoleRepository) {
        this.eventDataRepository = eventDataRepository;
        this.userRoleRepository = userRoleRepository;
    }


    /*
    * Required format =
    * {
    *   title: 'my event',
    *   description: 'my event',
    *   start: 'YYYY-MM-DDTHH:MM:SS',
    *   end: 'YYYY-MM-DDTHH:MM:SS',
    *   display: 'list-item'
    * }
    *  */

    public String getAllEventsToUser(int userDataId){
        StringBuilder object = new StringBuilder();
        List<StringBuilder> allEventsToUser = new ArrayList<>();
        List<UserRole> listOfBoards = userRoleRepository.findAllByUserId(userDataId);
        List<EventData> events = new ArrayList<>();

        for (UserRole ur :
                listOfBoards) {
            events.addAll(
                    eventDataRepository.findAllByBoardIdAndAndMembershipType(
                            ur.getBoardId(),
                            ur.getMembershipType().getPermission().toString()
                    ));
        }

        for (int i = 0; i < events.size(); i++) {
            if ( i < events.size() -1){
                allEventsToUser.add(object
                        .append("{\n")
                        .append("title: '").append(events.get(i).getName()).append("',\n")
                        .append("description: '").append(events.get(i).getMessage()).append("',\n")
                        .append("start: '").append(events.get(i).getDatetime_from().toString()).append("',\n")
                        .append("end: '").append(events.get(i).getDatetime_to().toString()).append("',\n")
                        .append("display: 'list-item'\n")
                        .append("},\n"));
            } else {
                allEventsToUser.add(object
                        .append("{\n")
                        .append("title: '").append(events.get(i).getName()).append("',\n")
                        .append("description: '").append(events.get(i).getMessage()).append("',\n")
                        .append("start: '").append(events.get(i).getDatetime_from().toString()).append("',\n")
                        .append("end: '").append(events.get(i).getDatetime_to().toString()).append("',\n")
                        .append("display: 'list-item'\n")
                        .append("}\n"));
            }
        }
        return allEventsToUser.toString();
    }


}
