package lab2.service;

import lab2.model.User;
import lab2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class UserService2 {
    @Autowired
    private UserRepository userRepository;
    public User findByUsername(String username){
        return userRepository.findByUsername(username).get();
    }

public boolean authenticateUserFromLogin(byte[] encryptedData, String randomString, String publicKey) {
    try {

        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        PublicKey publicKey1=keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance("RSASSA-PSS");
        signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        signature.initVerify(publicKey1);
        signature.update(randomString.getBytes(StandardCharsets.UTF_8));

        boolean verified=signature.verify(encryptedData);

        return verified;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}
