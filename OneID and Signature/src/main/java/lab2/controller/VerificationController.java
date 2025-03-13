package lab2.controller;

import jakarta.servlet.http.HttpSession;
import lab2.model.User;
import lab2.model.exception.UserDoesNotExistException;
import lab2.service.UserService2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;
import java.util.Map;

@Controller  // ✅ Make it a normal Spring MVC Controller (not RestController)
@RequestMapping("/oneid")
public class VerificationController {

    private final UserService2 userService;

    public VerificationController(UserService2 userService) {
        this.userService = userService;
    }

    @PostMapping("/verifyLogin")
    public String verifyLogin(@RequestBody Map<String, String> requestData, HttpSession session, RedirectAttributes redirectAttributes, Model model) throws UserDoesNotExistException {
        String username = requestData.get("username");
        String encryptedData = requestData.get("encryptedData");
        String randomString = requestData.get("randomString");
        User user = userService.findByUsername(username);

        byte[] signature = Base64.getDecoder().decode(encryptedData);

        if (user != null && userService.authenticateUserFromLogin(signature, randomString, user.getPublicKey())) {
            session.setAttribute("user", user);
            System.out.println("SUCCESS VERIFYING PRIVATE KEY");
            return "redirect:/";  // ✅ Redirects to "/main" after successful login
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid credentials");
            System.out.println("INCORRECT PRIVATE KEY");
            return "redirect:/login-key";  // ✅ Redirects to login page on failure
        }
    }
}
