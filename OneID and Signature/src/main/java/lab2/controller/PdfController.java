package lab2.controller;

import jakarta.servlet.http.HttpServletRequest;
import lab2.model.Document;
import lab2.model.User;
import lab2.repository.DocumentRepository;
import lab2.repository.UserRepository;
import lab2.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class PdfController {

    private static final String UPLOAD_DIR = "uploads/";
    private final UserService userService;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    public PdfController(UserService userService, UserRepository userRepository, DocumentRepository documentRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
    }

    @PostMapping("/upload-document")
    public String uploadFile(@RequestParam("choice") Boolean choice, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if(user == null){
            return "redirect:/login";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/";
        }

        try {
            // Ensure upload directory exists
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Save uploaded file
            String filename = file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Files.write(filePath, file.getBytes());

            // Generate a unique signature number
            Document newDocument = documentRepository.save(new Document(user.getUsername(), choice, LocalDateTime.now(), file.getBytes()));
            String signature = user.getName() + " " + user.getSurname() + ", ID:" + newDocument.getId().toString();

            // Call Python script to sign the document
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", "src/main/resources/script/sign_pdf.py", filePath.toString(), signature);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                redirectAttributes.addFlashAttribute("message", "Error signing the PDF.");
                return "redirect:/";
            }

            // Provide download link
            redirectAttributes.addFlashAttribute("message", "PDF signed successfully! Download it below.");
            redirectAttributes.addFlashAttribute("signedPdf", "/download/" + filename);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "File upload failed.");
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR + filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF) // ✅ Force PDF download
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}