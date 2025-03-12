package lab2.controller;

import jakarta.servlet.http.HttpServletRequest;
import lab2.model.User;
import lab2.repository.UserRepository;
import lab2.service.AESUtil;
import lab2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Base64;

@Controller
public class MainController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    private String privateKey = null;

    @GetMapping
    public String welcome(Model model, HttpServletRequest request) throws Exception {
        User user = (User) request.getSession().getAttribute("user");
        model.addAttribute("bodyContent", "welcome");
        model.addAttribute("user", user);
        if(user.getCardFace() != null)
            model.addAttribute("image", Base64.getEncoder().encodeToString(user.getCardFace()));

        this.privateKey = userService.updateKeys(user.getId());
        user = userRepository.findById(user.getId());
        request.getSession().setAttribute("user", user);

        if(this.privateKey != null)
            model.addAttribute("privateKey", privateKey);
        if(user.getPublicKey() != null)
            model.addAttribute("publicKey", user.getPublicKey());

        return "master-template";
    }

    @GetMapping("/get-encrypted-private-key")
    @ResponseBody  // This makes it return raw text instead of a view
    public String encryptData() {
        try {
            String tmp = this.privateKey;
            this.privateKey = null;
            return AESUtil.encrypt(tmp);  // Encrypt data and return it as a string
        } catch (Exception e) {
            return "Encryption error";
        }
    }
}
