package lab2.service;

import lab2.model.User;
import lab2.repository.UserRepository;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;


@Service
public class OCRService {
    @Autowired
    private UserRepository userRepository;

    public void validateUserData(User user) {
        if(user.getCard() == null)
            return;

        ITesseract tesseract = new Tesseract();

        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata/");
        tesseract.setLanguage("eng");

        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(user.getCard()));
            String result = tesseract.doOCR(img);

            compareData(result, user);

        } catch (TesseractException | IOException e) {
            e.printStackTrace();
        }
    }
    public void compareData(String result, User user) {
        if(result == null || result.isEmpty() || user.getName() == null || user.getName().isEmpty()
        || user.getSurname() == null || user.getSurname().isEmpty() || user.getSsn() == null || user.getSsn().isEmpty()) {
            return;
        }

        user.setNameVerified(false);
        user.setSurnameVerified(false);
        user.setSsnVerified(false);

        System.out.println(result);

        String tmp = result.toLowerCase().replace("/", " ")
                .replace("\\", " ")
                .replace(".", " ")
                .replace(",", " ")
                .replace("-", " ")
                .replace("~", " ")
                .replace("!", " ");
        String[] parts = tmp.split("\\s+");

        for(String part: parts){
            if(!part.isEmpty() && part.equals(user.getName().toLowerCase())){
                user.setNameVerified(true);
                break;
            }
        }
        for(String part: parts){
            if(!part.isEmpty() && part.equals(user.getSurname().toLowerCase())){
                user.setSurnameVerified(true);
                break;
            }
        }
        for(String part: parts){
            if(!part.isEmpty() && part.equals(user.getSsn().toLowerCase())){
                user.setSsnVerified(true);
                break;
            }
        }
        userRepository.save(user);
    }
}
