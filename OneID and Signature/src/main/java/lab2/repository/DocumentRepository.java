package lab2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import lab2.model.Document;
public interface DocumentRepository extends JpaRepository<Document, Long> {
}
