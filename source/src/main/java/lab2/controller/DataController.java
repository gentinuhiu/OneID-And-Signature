package lab2.controller;

import jakarta.servlet.http.HttpServletRequest;
import lab2.model.User;
import lab2.repository.UserRepository;
import lab2.service.ImageService;
import lab2.service.KeyService;
import lab2.service.OCRService;
import lab2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/data")
public class DataController {
    @Autowired
    private UserService userService;
    @Autowired
    private OCRService ocrService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/upload-data")
    public String uploadData(@RequestParam String username, @RequestParam String email,
                             @RequestParam String name, @RequestParam String surname,
                             @RequestParam String ssn, HttpServletRequest request, Model model) throws Exception {

        User user = (User) request.getSession().getAttribute("user");
        user = userService.update(user.getId(), username, email, name, surname, ssn);
        request.getSession().setAttribute("user", user);

        ocrService.validateUserData(user);
        user = userService.validateUser(user.getId());
        request.getSession().setAttribute("user", user);

        return "redirect:/";
    }
    @PostMapping("/upload-live-photo")
    public String uploadLivePhoto(@RequestParam("livePhoto") MultipartFile file, HttpServletRequest request) throws Exception {
        User user = (User) request.getSession().getAttribute("user");
        user = userService.updateFace(user.getId(), file.getBytes());
        request.getSession().setAttribute("user", user);

        imageService.validateUserData(user);
        user = userService.validateUser(user.getId());
        request.getSession().setAttribute("user", user);

        return "redirect:/";
    }
    @PostMapping(value = "/upload-id", consumes = "multipart/form-data")
    public String uploadID(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        User user = (User) request.getSession().getAttribute("user");
        user = userService.updateCard(user.getId(), file.getBytes());
        request.getSession().setAttribute("user", user);

        ocrService.validateUserData(user);
        imageService.validateUserData(user);
        user = userService.validateUser(user.getId());
        request.getSession().setAttribute("user", user);

        return "redirect:/";
    }
    @GetMapping("/generate-keys")
    public String generateKeys(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        user.setPublicKey(null);
        user = userRepository.save(user);
        request.getSession().setAttribute("user", user);
        return "redirect:/";
    }
}
