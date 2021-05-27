package no.sbs.ezra.servises;

import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.repositories.UserDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class EmailService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JavaMailSender javaMailSender;
    private final UserDataRepository userDataRepository;

    public EmailService(JavaMailSender javaMailSender, UserDataRepository userDataRepository) {
        this.javaMailSender = javaMailSender;
        this.userDataRepository = userDataRepository;
    }

    @Async
    public void sendEmailInviteToBoard(String toUser, Principal principal, BoardData board){
        if (userDataRepository.findByEmail(principal.getName()).isPresent()){
            UserData fromUser = userDataRepository.findByEmail(principal.getName()).get();
            String name = fromUser.getFirstname() + " " + fromUser.getLastname();
            String body = String.format("%s invited you to follow %s,\n https://prodject-ezra-sbs.herokuapp.com/board/%d", name, board.getName(), board.getId());
            String topic = String.format("%s invites you to %s", name, board.getName());
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom("prodject.ezra@gmail.com");
            simpleMailMessage.setTo(toUser);
            simpleMailMessage.setSubject(topic);
            simpleMailMessage.setText(body);
            try{
                javaMailSender.send(simpleMailMessage);
            } catch (MailException e){
                logger.error("Unable to send board invite.\n" + e.getMessage());
            }
        }
    }

    @Async
    public void sendFamilyMemberRequest(String sendTo, Principal principal) {
        if (userDataRepository.findByEmail(principal.getName()).isPresent()){
            UserData fromUser = userDataRepository.findByEmail(principal.getName()).get();
            String name = fromUser.getFirstname() + " " + fromUser.getLastname();
            String body = String.format("%s says you are family and would like you to sync your calenders.\n" +
                    "Go to this link to accept or register with your email first.\n" +
                    "https://prodject-ezra-sbs.herokuapp.com/family", name);
            String topic = String.format("Family member request from %s", name);

            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom("prodject.ezra@gmail.com");
            simpleMailMessage.setTo(sendTo);
            simpleMailMessage.setText(body);
            simpleMailMessage.setSubject(topic);
            try{
                javaMailSender.send(simpleMailMessage);
            } catch (MailException e){
                logger.error("Unable to send family request.\n" + e.getMessage());
            }
        }
    }
}
