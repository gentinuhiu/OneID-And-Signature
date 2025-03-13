package lab2.model;

import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Boolean confidential;
    private LocalDateTime creationDate;
    @Lob
    @Column(columnDefinition = "LONGBLOB") // Use "BLOB" for smaller PDFs
    private byte[] pdfData;

    public Document(){}

    public Document(String username, Boolean confidential, LocalDateTime creationDate, byte[] pdfData) {
        this.username = username;
        this.confidential = confidential;
        this.creationDate = creationDate;
        this.pdfData = pdfData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getConfidential() {
        return confidential;
    }

    public void setConfidential(Boolean confidential) {
        this.confidential = confidential;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public byte[] getPdfData() {
        return pdfData;
    }

    public void setPdfData(byte[] pdfData) {
        this.pdfData = pdfData;
    }
}
