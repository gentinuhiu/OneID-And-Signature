package lab2.controller;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lab2.model.User;
import lab2.model.exception.UserDoesNotExistException;
import lab2.repository.UserRepository;
import lab2.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordValidationService passwordValidationService;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private PasswordEncoder encoder;
    private final Map<String, String> verificationCodes = new HashMap<>();
    private Map<String, User> pendingUsers = new HashMap<>();
    private LocalTime sentTime = null;
    @Autowired
    private KeyService keyService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("bodyContent", "register");
        return "master-template";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               Model model) throws MessagingException {
        if(username == null || email == null || password == null) {
            model.addAttribute("error", "Please fill all the required fields!");
            model.addAttribute("bodyContent", "register");
            return "master-template";
        }
        if(userService.userExists(username, email)){
            model.addAttribute("error", "Username or email already exists!");
            model.addAttribute("bodyContent", "register");
            return "master-template";
        }
        if(!passwordValidationService.isPasswordStrong(password)){
            model.addAttribute("error", "Password is not strong!");
            model.addAttribute("passwordError", true);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("bodyContent", "register");
            return "master-template";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        String code = verificationCodeService.generateVerificationCode();
        verificationCodeService.sendVerificationEmail(email, code);
        verificationCodes.put(email, code);
        pendingUsers.put(email, user);
        sentTime = LocalTime.now();

        model.addAttribute("email", email);
        model.addAttribute("type", "register");
        model.addAttribute("timeleft", sentTime);
        model.addAttribute("bodyContent", "verify");
        return "master-template";
    }
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("bodyContent", "login");
        return "master-template";
    }
    @GetMapping("/login-key")
    public String loginKey(Model model) {
        model.addAttribute("bodyContent", "login-key");
        return "master-template";
    }
    @PostMapping("/login")
    public String authenticate(@RequestParam String username,
                               @RequestParam String password,
                               Model model, HttpServletRequest request) throws UserDoesNotExistException, MessagingException {

        if(username == null || password == null) {
            model.addAttribute("error", "Please fill all the required fields!");
            model.addAttribute("bodyContent", "login");
            return "master-template";
        }

        User user = userService.findByUsername(username);

        if (!encoder.matches(password, user.getPassword())) {
            model.addAttribute("error", "Invalid username or password!");
            model.addAttribute("bodyContent", "login");
            return "master-template";
        }
        String email = user.getEmail();
        String code = verificationCodeService.generateVerificationCode();
        verificationCodeService.sendVerificationEmail(email, code);
        verificationCodes.put(email, code);
        pendingUsers.put(email, user);
        sentTime = LocalTime.now();

        request.getSession().setAttribute("user", user);
        model.addAttribute("email", email);
        model.addAttribute("timeleft", sentTime);
        model.addAttribute("type", "login");
        model.addAttribute("bodyContent", "verify");
        return "master-template";
    }
//    @PostMapping("/login-key")
//    public String authenticateKey(@RequestParam String username, @RequestParam String privateKey,
//                                  Model model, HttpServletRequest request) throws Exception {
//
//        if(username == null || privateKey == null) {
//            model.addAttribute("error", "Please fill all the required fields!");
//            model.addAttribute("bodyContent", "login-key");
//            return "master-template";
//        }
//
//        User user = userService.findByUsername(username);
//
//        if(user.getPublicKey() == null){
//            model.addAttribute("error", "The user does not have keys!");
//            model.addAttribute("bodyContent", "login-key");
//            return "master-template";
//        }
//
//        if(!keyService.verifyKeyPair(privateKey, user.getPublicKey())){
//            model.addAttribute("error", "The Private Key is incorrect!");
//            model.addAttribute("bodyContent", "login-key");
//            return "master-template";
//        }
//        request.getSession().setAttribute("user", user);
//        return "redirect:/";
//    }
@PostMapping("/login-key")
public String authenticateKey(
        @RequestBody Map<String, String> requestBody, Model model, HttpServletRequest request) {
    try {
        String username = requestBody.get("username");
        String encryptedMessage = requestBody.get("encryptedMessage");
        String originalString = requestBody.get("originalString");

        String storedPublicKey = getPublicKeyFromDatabase(username);

        if (storedPublicKey == null) {
            model.addAttribute("error", "The Private Key is incorrect!");
            model.addAttribute("bodyContent", "login-key");
            return "master-template";
        }

        PublicKey publicKey = loadPublicKey(storedPublicKey);
        boolean isVerified = verifySignature(originalString, encryptedMessage, publicKey);
        System.out.println(isVerified);
        if (isVerified) {
            request.getSession().setAttribute("user", userRepository.findByUsername(username).get());
            return "redirect:/";
        } else {
            model.addAttribute("error", "The Private Key is incorrect!");
            model.addAttribute("bodyContent", "login-key");
            return "master-template";
        }

    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        model.addAttribute("bodyContent", "login-key");
        return "master-template";
    }
}

    private String getPublicKeyFromDatabase(String username) {
        return userRepository.findByUsername(username).get().getPublicKey(); // Replace with actual DB lookup
    }

    private PublicKey loadPublicKey(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private boolean verifySignature(String originalString, String encryptedMessage, PublicKey publicKey) throws Exception {
        byte[] decodedSignature = Base64.getDecoder().decode(encryptedMessage);
        Signature signature = Signature.getInstance("SHA256withRSA/PSS");
        signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        signature.initVerify(publicKey);
        signature.update(originalString.getBytes());
        return signature.verify(decodedSignature);
    }

//    private PublicKey loadPublicKey(String base64PublicKey) throws Exception {
//        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
//
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
//
//        return keyFactory.generatePublic(spec);
//    }
//
//    private boolean verifySignature(String originalString, String encryptedMessage, PublicKey publicKey) throws Exception {
//        byte[] decodedSignature = Base64.getDecoder().decode(encryptedMessage);
//        Signature signature = Signature.getInstance("SHA256withRSA");
//        signature.initVerify(publicKey);
//        signature.update(originalString.getBytes());
//        return signature.verify(decodedSignature);
//    }
    @PostMapping("/verify")
    public String verifyCode(@RequestParam String email,
                             @RequestParam String code,
                             @RequestParam String type,
                             HttpServletRequest request,
                             Model model) throws UserDoesNotExistException, MessagingException {

        Duration duration = Duration.between(sentTime, LocalTime.now());
        if (duration.abs().toSeconds() > 60) {
            if(type.equals("login")) {
                model.addAttribute("loginType", true);
            }
            else
                model.addAttribute("loginType", false);

            model.addAttribute("error", "Verification code is expired!");
            model.addAttribute("email", email);
            model.addAttribute("timeleft", sentTime);
            model.addAttribute("bodyContent", "verify");
            return "master-template";
        }

        if (!verificationCodes.containsKey(email) || !verificationCodes.get(email).equals(code)) {
            if(type.equals("login"))
                model.addAttribute("loginType", true);
            else
                model.addAttribute("loginType", false);

            model.addAttribute("error", "Invalid verification code!");
            model.addAttribute("email", email);
            model.addAttribute("timeleft", sentTime);
            model.addAttribute("bodyContent", "verify");
            return "master-template";
        }
        boolean loginType = type.equals("login");
        System.out.println(type);
        User user = pendingUsers.get(email);
        if(!loginType) {
                user = userService.register(user.getUsername(), user.getEmail(), user.getPassword());
//                user = authService.login(user.getUsername(), user.getPassword());
//                System.out.println(user.getUsername());
            }
            else {
                //user = authService.login(user.getUsername(), user.getPassword());
                //System.out.println(user.getUsername());
            }
            pendingUsers.remove(email);


        verificationCodes.remove(email);
        request.getSession().setAttribute("user", user);
        return "redirect:/";
    }
    @PostMapping("/reverify")
    public String reverify(Model model,
                           @RequestParam String email,
                           @RequestParam boolean loginType)
            throws MessagingException, UserDoesNotExistException {
        User user = userService.findByEmail(email);

        String code = verificationCodeService.generateVerificationCode();
        verificationCodeService.sendVerificationEmail(email, code);

        verificationCodes.put(email, code);
        pendingUsers.put(email, user);

        sentTime = LocalTime.now();

        model.addAttribute("email", email);
        model.addAttribute("loginType", loginType);
        model.addAttribute("timeleft", sentTime);
        model.addAttribute("bodyContent", "verify");
        return "master-template";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }
}
