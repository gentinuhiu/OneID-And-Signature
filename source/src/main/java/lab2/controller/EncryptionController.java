package lab2.controller;

import lab2.service.AESUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EncryptionController {

    @GetMapping("/encrypt")
    public String encryptData(@RequestParam String data) {
        try {
            return AESUtil.encrypt(data);
        } catch (Exception e) {
            return "Encryption error";
        }
    }
}
