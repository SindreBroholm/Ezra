package no.sbs.ezra.servises;

import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.repositories.UserDataRepository;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class EmailService {


    private final JavaMailSender javaMailSender;
    private final UserDataRepository userDataRepository;

    public EmailService(JavaMailSender javaMailSender, UserDataRepository userDataRepository) {
        this.javaMailSender = javaMailSender;
        this.userDataRepository = userDataRepository;
    }

    public void sendEmailInviteToBoard(String toUser, Principal principal, BoardData board){
        UserData fromUser = userDataRepository.findByEmail(principal.getName());
        String name = fromUser.getFirstname() + " " + fromUser.getLastname();
        String body = String.format("%s invited you to follow %s,\n http://localhost:8080/board/%d", name, board.getName(), board.getId());
        String topic = String.format("%s invites you to %s", name, board.getName());
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("prodject.ezra@gmail.com");
        simpleMailMessage.setTo(toUser);
        simpleMailMessage.setSubject(topic);
        simpleMailMessage.setText(body);
        try{
            javaMailSender.send(simpleMailMessage);
        } catch (MailException e){
            e.getMessage();
        }
    }

    public void sendFamilyMemberRequest(String sendTo, Principal principal) {
        UserData fromUser = userDataRepository.findByEmail(principal.getName());
        String name = fromUser.getFirstname() + " " + fromUser.getLastname();
        String body = String.format("%s says you are family and would like you to sync your calenders.\n" +
                "Go to this link to accept or register with your email first.\n" +
                "http://localhost:8080/family", name);
        String topic = String.format("Family member request from %s", name);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("prodject.ezra@gmail.com");
        simpleMailMessage.setTo(sendTo);
        simpleMailMessage.setText(body);
        simpleMailMessage.setSubject(topic);
        try{
            javaMailSender.send(simpleMailMessage);
        } catch (MailException e){
            e.getMessage();
        }
    }
}
