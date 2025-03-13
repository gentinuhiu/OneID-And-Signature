package lab2.service;

import lab2.model.User;
import lab2.repository.UserRepository;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;

@Service
public class ImageService {
    private static final String TEMP_DIR = "temp_faces/";
    private final StringHttpMessageConverter stringHttpMessageConverter;
    private final UserRepository userRepository;

    public ImageService(StringHttpMessageConverter stringHttpMessageConverter, UserRepository userRepository) {
        this.stringHttpMessageConverter = stringHttpMessageConverter;
        this.userRepository = userRepository;
    }

    public void validateUserData(User user) {
        try {
            byte[] livePhotoBytes = user.getFace();
            byte[] idCardPhotoBytes = user.getCard();

            if (livePhotoBytes == null || idCardPhotoBytes == null) {
                System.out.println("QUIT AT LINE 22");
                return;
            }

            Path livePhotoPath = saveImageBytes(livePhotoBytes, "live_photo.png");
            Path idCardPhotoPath = saveImageBytes(idCardPhotoBytes, "id_card.png");

            ProcessBuilder pb = new ProcessBuilder(
                    "python", "src/main/resources/script/face_match.py",
                    livePhotoPath.toString(), idCardPhotoPath.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            boolean read = false;
            while ((line = reader.readLine()) != null) {
                if(line.equals("RESULT")){
                    read = true;
                    continue;
                }
                if(read)
                    output.append(line).append("\n");
            }
            process.waitFor();

            if(output.toString().isEmpty() || output.toString().split("\n").length != 3){
                System.out.println("ERROR");
                user.setFaceVerified(false);
                userRepository.save(user);
                System.out.println(output.toString());
                return;
            }
            String[] parts = output.toString().split("\n");
            String result = parts[0];
            String distance = parts[1];
            String threshold = parts[2];

            Path imagePath = Paths.get("src/main/resources/static/id_card_face.png");
            user.setCardFace(Files.readAllBytes(imagePath));

            System.out.println("RESULT: " + result);
            System.out.println("DISTANCE: " + distance);
            System.out.println("THRESHOLD: " + threshold);

            if(result.split(" ").length == 2 && result.split(" ")[1].equals("True")){
                user.setFaceVerified(true);
                System.out.println("UPDATED DATABASE: TRUE");
            }
            else{
                user.setFaceVerified(false);
            }

            userRepository.save(user);

            Files.deleteIfExists(livePhotoPath);
            Files.deleteIfExists(idCardPhotoPath);

        } catch (Exception e) {
            System.out.println("ERROR at IMAGE SERVICE: " + e.getMessage());
        }
        System.out.println("FINISH");
    }

    private Path saveImageBytes(byte[] imageBytes, String fileName) throws IOException {
        Path directory = Paths.get(TEMP_DIR);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        Path filePath = directory.resolve(fileName);
        Files.write(filePath, imageBytes);
        return filePath;
    }
}
