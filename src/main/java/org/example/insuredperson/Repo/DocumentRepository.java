package org.example.insuredperson.Repo;

import org.example.insuredperson.Entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Find documents by insured person's policy number
    List<Document> findByInsuredPersonPolicyNumber(String policyNumber);

    Optional<Document> findByInsuredPersonPolicyNumberAndFileName(String policyNumber, String fileName);
}
