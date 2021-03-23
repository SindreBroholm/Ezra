package no.sbs.ezra.servises;

import no.sbs.ezra.data.UserData;
import no.sbs.ezra.repositories.UserDataRepository;
import no.sbs.ezra.security.PasswordConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InitContoller {

    private final UserDataRepository userDataRepository;
    private final PasswordConfig passwordConfig;


    public InitContoller(UserDataRepository userDataRepository, PasswordConfig passwordConfig) {
        this.userDataRepository = userDataRepository;
        this.passwordConfig = passwordConfig;
    }

    @GetMapping("/init")
    public void init(){
        UserData user = new UserData("test@t.t", "sindre", "Sæther", passwordConfig.passwordEncoder().encode("sindre"),
                "93071137");
        userDataRepository.save(user);
    }
}
