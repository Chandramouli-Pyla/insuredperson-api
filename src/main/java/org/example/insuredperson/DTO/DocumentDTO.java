package org.example.insuredperson.DTO;

import org.example.insuredperson.Entity.InsuredPerson;

public class DocumentDTO {
    private String fileName;
    private String fileType;
    private long size;
    private String policyNumber;
    private String firstName;
    private String lastName;

    public DocumentDTO(String fileName, String fileType, long size, String policyNumber, String firstName, String lastName) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
        this.policyNumber = policyNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters & setters
    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public long getSize() {
        return size;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}

