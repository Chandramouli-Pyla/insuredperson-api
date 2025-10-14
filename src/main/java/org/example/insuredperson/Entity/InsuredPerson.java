package org.example.insuredperson.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

@Entity
public class InsuredPerson {

    @Id
    @Column(unique=true, nullable=false)
    private String policyNumber;  // Business primary key

    private String firstName;
    private String lastName;
    private Integer age;   // Use wrapper type so it can be null
    private String role;
    private String email;

    @Column(unique=true, nullable=false)
    private String userId;
    private String password;

    @Pattern(
            regexp = "^[2-9][0-9]{9}$",
            message = "Phone number must be 10 digits and start with digits 2-9"
    )    private String phoneNumber;

    private String street;
    private String apartment;
    private String city;

    @Pattern(
            regexp = "^[0-9]{5}$",
            message = "Zip code must be exactly 5 digits"
    )
    private String zipcode;

    private String state;
    private String country;

    @Enumerated(EnumType.STRING) // Stores enum name in DB
    private InsuranceType typeOfInsurance;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] profilePicture;  // Stores the image as a byte array

    @OneToMany(mappedBy = "insuredPerson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    // Method to add a document
    public void addDocument(Document document) {
        documents.add(document);
        document.setInsuredPerson(this);
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public InsuranceType getTypeOfInsurance() {
        return typeOfInsurance;
    }

    public void setTypeOfInsurance(InsuranceType typeOfInsurance) {
        this.typeOfInsurance = typeOfInsurance;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }
    public String getRole() {

        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getEmail() {

        return email;
    }
    public void setEmail(String email) {

        this.email = email;
    }
}
