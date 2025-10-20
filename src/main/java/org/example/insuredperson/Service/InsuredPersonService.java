
package org.example.insuredperson.Service;

import org.example.insuredperson.DTO.ChangePasswordRequest;
import org.example.insuredperson.DTO.InsuredPersonRequest;
import org.example.insuredperson.DTO.LoginRequest;
import org.example.insuredperson.DTO.ResetPasswordRequest;
import org.example.insuredperson.Entity.Document;
import org.example.insuredperson.Entity.InsuredPerson;
import org.example.insuredperson.Exception.CustomExceptions;
import org.example.insuredperson.Repo.DocumentRepository;
import org.example.insuredperson.Repo.InsuredPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class    InsuredPersonService {
    private final InsuredPersonRepository repository;
    private final JwtService jwtService;
    private ValidationService validationService;
    private final PasswordEncoder passwordEncoder;
    private final DocumentRepository documentRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;
    private final Map<String, PasswordResetOtp> tokenStore = new HashMap<>();

    //constructor where it will initialize the obj
    public InsuredPersonService(InsuredPersonRepository repository, DocumentRepository documentRepository, JwtService jwtService, ValidationService validationService, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.documentRepository = documentRepository;
        this.jwtService = jwtService;
        this.validationService = validationService;
        this.passwordEncoder = passwordEncoder;
    }


    //getting all isnured persons data from the data base
    public Page<InsuredPerson> getAllInsuredList(int offSet, int pageSize) {
        return repository.findAll(PageRequest.of(offSet, pageSize));
    }



    //getting insured person single data (record) using person id
    public InsuredPerson findById(String policyNumber){
        return repository.findById(policyNumber)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("InsuredPerson not found with policyNumber: " + policyNumber));

    }

    // find by first name
    public List<InsuredPerson> findByFirstName(String firstName) {
        List<InsuredPerson> persons = repository.findByFirstName(firstName);
        if (persons.isEmpty()) {
            throw new CustomExceptions.ResourceNotFoundException(
                    "No InsuredPerson found with firstName: " + firstName);
        }
        return persons;
    }

    // find by last name
    public List<InsuredPerson> findByLastName(String lastName) {
        List<InsuredPerson> persons = repository.findByLastName(lastName);
        if (persons.isEmpty()) {
            throw new CustomExceptions.ResourceNotFoundException(
                    "No InsuredPerson found with lastName: " + lastName);
        }
        return persons;    }

    // find by first character of first name
    public List<InsuredPerson> findByFirstCharOfFirstName(String firstChar) {
        List<InsuredPerson> persons = repository.findByFirstNameStartsWith(firstChar);
        if (persons.isEmpty()) {
            throw new CustomExceptions.ResourceNotFoundException(
                    "No InsuredPerson found with starting character: " + firstChar);
        }
        return persons;
    }

    // find by email
    public List<InsuredPerson> findByEmail(String email) {
        List<InsuredPerson> persons = repository.findByEmail(email);
        if (persons.isEmpty()) {
            throw new CustomExceptions.ResourceNotFoundException(
                    "No InsuredPerson found with email: " + email);
        }
        return persons;
    }

    public List<InsuredPerson> findByPhoneNumber(String phoneNumber) {
        List<InsuredPerson> persons = repository.findByPhoneNumber(phoneNumber);
        if (persons.isEmpty()) {
            throw new CustomExceptions.ResourceNotFoundException(
                    "No InsuredPerson found with phone number: " + phoneNumber);
        }
        return persons;
    }

    //find by userId
    public InsuredPerson findByUserId(String userId){
        InsuredPerson person = repository.findByUserId(userId);
        if(person==null){
            throw new CustomExceptions.ResourceNotFoundException(
                    "No InsuredPerson found with userId: "+userId);
        }
        return person;
    }

    //creating new record and inserting into the dto
//    public InsuredPerson createInsuredPerson(InsuredPersonRequest dto) {
//        if(repository.existsById(dto.getPolicyNumber())) {
//            throw new CustomExceptions.DuplicatePolicyException("Policy number already exists: " + dto.getPolicyNumber());
//        }
//        if(repository.existsByUserId( dto.getUserId())) {
//            throw new CustomExceptions.DuplicateUserIdException("User id already exists: " + dto.getUserId());
//
//        }
//
//        validationService.validateUserId(dto.getUserId());
//        validationService.validatePassword(dto.getPassword());
//        validationService.validatePolicyNumber(dto.getPolicyNumber());
//        validationService.validateEmail(dto.getEmail());
//
//        InsuredPerson entity = new InsuredPerson();
//        entity.setPolicyNumber(dto.getPolicyNumber());
//        entity.setFirstName(dto.getFirstName());
//        entity.setLastName(dto.getLastName());
//        entity.setAge(dto.getAge());
//        entity.setUserId(dto.getUserId());
//        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
//        entity.setEmail(dto.getEmail());
//        entity.setRole(dto.getRole());
//        entity.setPhoneNumber(dto.getPhoneNumber());
//        entity.setStreet(dto.getStreet());
//        entity.setApartment(dto.getApartment());
//        entity.setCity(dto.getCity());
//        entity.setState(dto.getState());
//        entity.setCountry(dto.getCountry());
//        entity.setZipcode(dto.getZipcode());
//        entity.setTypeOfInsurance(dto.getTypeOfInsurance());
//
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(entity.getEmail());
//            message.setSubject("Insurance Portal Credentials Created Successfully");
//
//            message.setText("Hello " + entity.getFirstName() + ",\n\n" +
//                    "Thank you for registering with our Insurance company.\n\n" +
//                    "Here are your login credentials:\n" +
//                    "Username: " + entity.getUserId() + "\n" +
//                    "To reset your password or set a new one, please visit the following link:\n" +
//                    "https://insuredperson-api-ui-458668609912.us-central1.run.app/forgot-password\n\n" +
//                    "Thanks,\n" +
//                    "SpringBoot Operations Team");
//
//            mailSender.send(message);
//        } catch (Exception e) {
//            // Wrap low-level SMTP error into your own exception
//            throw new CustomExceptions.UnauthorizedException("Failed to send reset email. Please check your email configuration.");
//        }
//        return repository.save(entity);
//    }

    //creating new record and inserting into the dto
    public InsuredPerson createInsuredPerson(InsuredPersonRequest dto) {
        if(repository.existsById(dto.getPolicyNumber())) {
            throw new CustomExceptions.DuplicatePolicyException("Policy number already exists: " + dto.getPolicyNumber());
        }
        if(repository.existsByUserId( dto.getUserId())) {
            throw new CustomExceptions.DuplicateUserIdException("User id already exists: " + dto.getUserId());

        }

        validationService.validateUserId(dto.getUserId());
        validationService.validatePassword(dto.getPassword());
        validationService.validatePolicyNumber(dto.getPolicyNumber());
        validationService.validateEmail(dto.getEmail());

        InsuredPerson entity = new InsuredPerson();
        entity.setPolicyNumber(dto.getPolicyNumber());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setAge(dto.getAge());
        entity.setUserId(dto.getUserId());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setEmail(dto.getEmail());
        entity.setRole(dto.getRole());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setStreet(dto.getStreet());
        entity.setApartment(dto.getApartment());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setCountry(dto.getCountry());
        entity.setZipcode(dto.getZipcode());
        entity.setTypeOfInsurance(dto.getTypeOfInsurance());


        // Process documents (if any)
        MultipartFile[] files = dto.getDocuments();
        if (files != null) {
            if (files.length > 5) {
                throw new IllegalArgumentException("You can upload a maximum of 5 documents.");
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Document doc = new Document();
                    doc.setFileName(file.getOriginalFilename());
                    doc.setFileType(file.getContentType());
                    try {
                        doc.setData(file.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read document", e);
                    }

                    // Associate document with the insured person
                    entity.addDocument(doc);
                }
            }
        }


        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(entity.getEmail());
            message.setSubject("Insurance Portal Credentials Created Successfully");

            message.setText("Hello " + entity.getFirstName() + ",\n\n" +
                    "Thank you for registering with our Insurance company.\n\n" +
                    "Here are your login credentials:\n" +
                    "Username: " + entity.getUserId() + "\n" +
                    "To reset your password or set a new one, please visit the following link:\n" +
                    "http://localhost:3000/forgot-password\n\n" +
                    "Thanks,\n" +
                    "SpringBoot Operations Team");

            mailSender.send(message);
        } catch (Exception e) {
            // Wrap low-level SMTP error into your own exception
            throw new CustomExceptions.UnauthorizedException("Failed to send reset email. Please check your email configuration.");
        }
        return repository.save(entity);
    }

    public void saveProfilePicture(String policyNumber, MultipartFile profilePicture) throws IOException {
        InsuredPerson insuredPerson = repository.findById(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy number not found"));

        insuredPerson.setProfilePicture(profilePicture.getBytes());
        repository.save(insuredPerson);
    }

    public InsuredPerson updateInsuredPerson(String pathPolicyNumber, InsuredPersonRequest dto) {
        //Fetch the record to update using the path parameter ie., policyNumber
        InsuredPerson entity = repository.findById(pathPolicyNumber)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException(
                        "InsuredPerson not found with policyNumber: " + pathPolicyNumber));

        //Update fields
        if (dto.getFirstName() != null) entity.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) entity.setLastName(dto.getLastName());
        if (dto.getAge() != null) entity.setAge(dto.getAge());
        if(dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if(dto.getRole() !=null) entity.setRole(dto.getRole());
        if(dto.getPhoneNumber() !=null) entity.setPhoneNumber(dto.getPhoneNumber());
        if(dto.getStreet() !=null) entity.setStreet(dto.getStreet());
        if(dto.getApartment()!=null) entity.setApartment(dto.getApartment());
        if(dto.getCity() !=null) entity.setCity(dto.getCity());
        if(dto.getState() !=null) entity.setState(dto.getState());
        if(dto.getCountry() !=null) entity.setCountry(dto.getCountry());
        if(dto.getZipcode() !=null) entity.setZipcode(dto.getZipcode());
        if(dto.getTypeOfInsurance() !=null) entity.setTypeOfInsurance(dto.getTypeOfInsurance());

        //UserId check
        if (dto.getUserId() != null) {
            InsuredPerson existingUser = repository.findByUserId(dto.getUserId());
            if (existingUser != null && !existingUser.getPolicyNumber().equals(pathPolicyNumber)) {
                throw new CustomExceptions.DuplicateUserIdException(
                        "UserId already exists: " + dto.getUserId());
            }
            entity.setUserId(dto.getUserId());
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(entity.getEmail());
            message.setSubject("Insurance Portal Detials Updated Successfully");

            message.setText("Hello " + entity.getFirstName() + ",\n\n" +
                    "Thank you for registering with our Insurance company.\n\n" +
                    "As per your request we updated your details in our portal, please check the same with your credentials. If you have any" +
                    "concerns please let our insurance company know, we will follow up the same.\n\n" +
                    "Thanks,\n" +
                    "SpringBoot Operations Team");

            mailSender.send(message);
        } catch (Exception e) {
            // Wrap low-level SMTP error into your own exception
            throw new CustomExceptions.UnauthorizedException("Failed to send reset email. Please check your email configuration.");
        }
        //Save and return
        return repository.save(entity);
    }


    //delete record with the help of policyNumber
    public void deleteInsuredPerson(String policyNumber) {
        if (!repository.existsById(policyNumber)) {
            throw new CustomExceptions.ResourceNotFoundException(
                    "InsuredPerson not found with policyNumber: " + policyNumber);
        }
        repository.deleteById(policyNumber);
    }

    public String generateTokenForUser(InsuredPerson user) {
        return jwtService.generateToken(user);
    }

    //Login credentials service
    public InsuredPerson login(LoginRequest loginRequest) {
        InsuredPerson user = repository.findByUserId(loginRequest.getUserId());
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new CustomExceptions.UnauthorizedException("Invalid credentials!!! Please try again.");
        }

        return user;
    }

        // Step 1: Generate reset token and send email
        public String forgotPassword(String userId) {
        InsuredPerson user = repository.findByUserId(userId);

            if (user == null) {
                throw new CustomExceptions.ResourceNotFoundException("User not found");
            }
            String otp = String.format("%06d", new Random().nextInt(999999));
            tokenStore.put(otp, new PasswordResetOtp(userId, LocalDateTime.now().plusMinutes(10)));
            // Send email safely
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(user.getEmail());
                message.setSubject("Password Reset OTP");
                message.setText("Hello "+user.getFirstName()+
                                "."+"\n\nAs you requested for resetting the password, " +
                                "Here is your reset OTP: " + otp+"\n\n\n\n"+
                                "Thanks,"+"\n"+
                                "SpringBoot Operations team.");
                mailSender.send(message);
            } catch (Exception e) {
                // Wrap low-level SMTP error into your own exception
                throw new CustomExceptions.UnauthorizedException("Failed to send reset email. Please check your email configuration.");
            }

            return "Reset OTP sent successfully to the following email: "+ user.getEmail();
        }

    // Step 2: Reset password with token
    public InsuredPerson resetPassword(ResetPasswordRequest resetPasswordRequest) {
        PasswordResetOtp resetOtp = tokenStore.get(resetPasswordRequest.getOtp());
        if (resetOtp == null || resetOtp.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomExceptions.UnauthorizedException("Invalid or expired OTP");
        }

        // Validate password strength
        validationService.validatePassword(resetPasswordRequest.getNewPassword());
        if(!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmNewPassword())) {
            throw new CustomExceptions.UnauthorizedException("Passwords do not match");
        }
        InsuredPerson user = repository.findByUserId(resetOtp.getUserId());
        if (user == null) {
            throw new CustomExceptions.ResourceNotFoundException("User not found");
        }

        // Encode new password
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        repository.save(user);

        // Remove token after use
        tokenStore.remove(resetPasswordRequest.getOtp());

        return user;
    }

    //updated Password /change password
    public InsuredPerson updatePassword(ChangePasswordRequest changePasswordRequest) {
        InsuredPerson user = repository.findByUserId(changePasswordRequest.getUserId());
        if (user == null || !passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new CustomExceptions.UnauthorizedException("Invalid old password credentials!!! Please try again.");
        }
        // Validate password strength
        validationService.validatePassword(changePasswordRequest.getNewPassword());
        if(!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())) {
            throw new CustomExceptions.UnauthorizedException("Passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        repository.save(user);

        return user;
    }
    private static class PasswordResetOtp {
        private String userId;
        private LocalDateTime expiry;

        public PasswordResetOtp(String userId, LocalDateTime expiry) {
            this.userId = userId;
            this.expiry = expiry;
        }

        public String getUserId() { return userId; }
        public LocalDateTime getExpiry() { return expiry; }
    }
}
