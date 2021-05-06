package no.sbs.ezra.security.auth;

import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.repositories.UserDataRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserDataRepository userDataRepository;

    public UserService(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!userDataRepository.findByEmail(username).isPresent()) {
            throw new UsernameNotFoundException(username);
        }
        UserData user = userDataRepository.findByEmail(username).get();
        return new CustomUserDetails(user);
    }

}
