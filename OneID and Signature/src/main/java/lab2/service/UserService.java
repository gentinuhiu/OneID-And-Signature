package lab2.service;

import lab2.model.User;
import lab2.model.exception.InvalidArgumentsException;
import lab2.model.exception.UserDoesNotExistException;
import lab2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private KeyService keyService;

    public boolean userExists(String username, String email) {
        return userRepository.findByUsername(username).isPresent() ||
                userRepository.findByEmail(email).isPresent();
    }
    public User findByUsername(String username) throws UserDoesNotExistException {
        return userRepository.findByUsername(username).orElseThrow(UserDoesNotExistException::new);
    }
    public User findByEmail(String email) throws UserDoesNotExistException {
        return userRepository.findByEmail(email).orElseThrow(UserDoesNotExistException::new);
    }
    public User register(String username, String email, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new InvalidArgumentsException();
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }
    public User updateCard(Long id, byte[] bytes) {
        User user = userRepository.findById(id);
        user.setCard(bytes);
        return userRepository.save(user);
    }
    public User updateFace(Long id, byte[] bytes){
        User user = userRepository.findById(id);
        user.setFace(bytes);
        return userRepository.save(user);
    }

    public User update(Long id, String username, String email, String name, String surname, String ssn) {
        User user = userRepository.findById(id);

        if(user.getUsername() != null && user.getUsername().equals(username) && user.getEmail()!= null && user.getEmail().equals(email)
        && user.getName() != null && user.getName().equals(name) && user.getSurname() != null && user.getSurname().equals(surname)
                && user.getSsn() != null && user.getSsn().equals(ssn)) {
            return user;
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setName(name);
        user.setSurname(surname);
        user.setSsn(ssn);
        return userRepository.save(user);
    }

    public User validateUser(Long id) {
        User user = userRepository.findById(id);
        if(user.isNameVerified() && user.isSurnameVerified() && user.isSsnVerified()
        && user.isFaceVerified())
            user.setAccountVerified(true);
        else
            user.setAccountVerified(false);

        return userRepository.save(user);
    }

    public String updateKeys(Long id) throws Exception {
        User user = userRepository.findById(id);

        if(!user.isAccountVerified()) {
            user.setPublicKey(null);
            userRepository.save(user);
            return null;
        }
        if(user.getPublicKey() != null)
            return null;

        String keys = keyService.generateKeys();
        String[] parts = keys.split("\n");
        if(parts.length != 2){
            System.out.println("Error splitting keys at UserService updateKeys()");
            return null;
        }
        user.setPublicKey(parts[1]);
        userRepository.save(user);
        return parts[0];
    }
}
