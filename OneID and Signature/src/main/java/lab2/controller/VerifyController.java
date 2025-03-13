package lab2.controller;

import com.lowagie.text.pdf.PdfDocument;
import lab2.model.Document;
import lab2.repository.DocumentRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;
import java.util.Optional;

@Controller
public class VerifyController {
    private final DocumentRepository documentRepository;
    public VerifyController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @GetMapping("/verify-document")
    public String verify(Model model) {
        model.addAttribute("bodyContent", "verify-document");
        return "master-template";
    }

    @PostMapping("/verify-document")
    public String post(@RequestParam("id") Integer id, Model model) {
        Document document = documentRepository.findById(Long.parseLong(String.valueOf(id))).orElse(null);
        if (document == null) {
            model.addAttribute("error", "Document not found");
            model.addAttribute("bodyContent", "verify-document");
            return "master-template";
        }

        else if(document.getConfidential()){
            String result = "Confidential Document signed by " + document.getUsername().substring(0, 3) + "*** on " + document.getCreationDate().toString();
            model.addAttribute("result", result);
            model.addAttribute("bodyContent", "verify-document");
            return "master-template";
        }

        String base64Pdf = Base64.getEncoder().encodeToString(document.getPdfData());
        model.addAttribute("pdfBase64", base64Pdf);
        model.addAttribute("pdfId", id);
        model.addAttribute("result", "Public Document signed by " + document.getUsername() + " on " + document.getCreationDate().toString());
        model.addAttribute("bodyContent", "verify-document");
        return "master-template";
    }
    @GetMapping("/pdf/download/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Optional<Document> pdfDocument = documentRepository.findById(id);

        if (pdfDocument.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document pdf = pdfDocument.get();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.pdf\"") // Default filename
                .body(pdf.getPdfData());
    }

}
