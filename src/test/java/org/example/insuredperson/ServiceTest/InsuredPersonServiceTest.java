package org.example.insuredperson.ServiceTest;

import org.example.insuredperson.DTO.InsuredPersonRequest;
import org.example.insuredperson.DTO.LoginRequest;
import org.example.insuredperson.Entity.InsuredPerson;
import org.example.insuredperson.Exception.CustomExceptions;
import org.example.insuredperson.Repo.DocumentRepository;
import org.example.insuredperson.Repo.InsuredPersonRepository;
import org.example.insuredperson.Service.InsuredPersonService;
import org.example.insuredperson.Service.JwtService;
import org.example.insuredperson.Service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InsuredPersonServiceTest {

    @InjectMocks
    private InsuredPersonService insuredPersonService;

    @Mock
    private InsuredPersonRepository repository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private ValidationService validationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        insuredPersonService = new InsuredPersonService(
                repository,
                documentRepository,
                jwtService,
                validationService,
                passwordEncoder
        );

        // Inject mailSender manually if it's not in the constructor
        ReflectionTestUtils.setField(insuredPersonService, "mailSender", mailSender);
        ReflectionTestUtils.setField(insuredPersonService, "fromEmail", "noreply@example.com"); // Or mock value
    }

    @Test
    public void testCreateInsuredPerson_Success() {
        // Arrange
        InsuredPersonRequest request = new InsuredPersonRequest();
        request.setPolicyNumber("PA123456");
        request.setUserId("User@123");
        request.setPassword("Strong@123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@gmail.com");

        when(repository.existsById("PA123456")).thenReturn(false);
        when(repository.existsByUserId("User@123")).thenReturn(false);
        when(passwordEncoder.encode("Strong@123")).thenReturn("encodedPassword");

        InsuredPerson savedPerson = new InsuredPerson();
        savedPerson.setPolicyNumber("PA123456");
        when(repository.save(any(InsuredPerson.class))).thenReturn(savedPerson);

        //don't try to send an email...
        doNothing().when(mailSender).send((SimpleMailMessage) any());

        // Act
        InsuredPerson result = insuredPersonService.createInsuredPerson(request);

        // Assert
        assertNotNull(result);
        assertEquals("PA123456", result.getPolicyNumber());
        verify(validationService).validateUserId("User@123");
        verify(validationService).validatePassword("Strong@123");
        verify(validationService).validatePolicyNumber("PA123456");
        verify(validationService).validateEmail("john@gmail.com");
        verify(repository).save(any(InsuredPerson.class));
    }

    @Test
    void testLoginInsuredPerson_Success(){
        String userId = "Test@123";
        String password = "Test@123";
        String encodedPassword = "encodedPassword";

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserId(userId);
        loginRequest.setPassword(password);

        InsuredPerson person = new InsuredPerson();
        person.setUserId(userId);
        person.setPassword(encodedPassword);

        when(repository.findByUserId(userId)).thenReturn(person);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        //Act
        InsuredPerson result  = insuredPersonService.login(loginRequest);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(repository).findByUserId(userId);
        verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    void testLogin_InvalidPassword_ThrowsUnauthorizedException() {
        // Arrange
        String userId = "User@123";
        String password = "WrongPassword";

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserId(userId);
        loginRequest.setPassword(password);

        InsuredPerson user = new InsuredPerson();
        user.setUserId(userId);
        user.setPassword("encodedPassword");

        when(repository.findByUserId(userId)).thenReturn(user);
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        // Act & Assert
        CustomExceptions.UnauthorizedException exception = assertThrows(
                CustomExceptions.UnauthorizedException.class,
                () -> insuredPersonService.login(loginRequest)
        );

        assertEquals("Invalid credentials!!! Please try again.", exception.getMessage());
        verify(repository).findByUserId(userId);
        verify(passwordEncoder).matches(password, user.getPassword());
    }

    @Test
    void testLogin_UserNotFound_ThrowsUnauthorizedException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserId("User@123");
        loginRequest.setPassword("Password@123");

        when(repository.findByUserId("User@123")).thenReturn(null);

        // Act & Assert
        CustomExceptions.UnauthorizedException exception = assertThrows(
                CustomExceptions.UnauthorizedException.class,
                () -> insuredPersonService.login(loginRequest)
        );

        assertEquals("Invalid credentials!!! Please try again.", exception.getMessage());
        verify(repository).findByUserId("User@123");
    }

    @Test
    void testGetAllInsuredPersonsList_Success(){
        int offSet =0;
        int pageSize = 3;

        InsuredPerson person1 = new InsuredPerson();
        person1.setPolicyNumber("PA12243242");
        InsuredPerson person2 = new InsuredPerson();
        person2.setPolicyNumber("PA14324532");
        InsuredPerson person3 = new InsuredPerson();
        person3.setPolicyNumber("PA8856564");

        List<InsuredPerson> insuredPersonList = Arrays.asList(person1, person2, person3);
        Page<InsuredPerson> page = new PageImpl<>(insuredPersonList);

        when(repository.findAll(PageRequest.of(offSet, pageSize))).thenReturn(page);

        Page<InsuredPerson> result = insuredPersonService.getAllInsuredList(offSet, pageSize);

        assertNotNull(result);
        assertEquals("PA12243242", result.getContent().get(0).getPolicyNumber());
        assertEquals("PA14324532", result.getContent().get(1).getPolicyNumber());
        assertEquals("PA8856564", result.getContent().get(2).getPolicyNumber());

        verify(repository).findAll(PageRequest.of(offSet, pageSize));
    }

    @Test
    void testGetAllInsuredPersonsList_EmptyResult(){
        int offSet=0;
        int pageSize=3;

        Page<InsuredPerson> emptyPage = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(PageRequest.of(offSet, pageSize))).thenReturn(emptyPage);
        Page<InsuredPerson> result = insuredPersonService.getAllInsuredList(offSet, pageSize);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll(PageRequest.of(offSet,pageSize));
    }

    @Test
    void testFinByPolicyNumber_Success(){
        String policyNumber = "PA3432343";

        InsuredPerson insuredPerson = new InsuredPerson();
        insuredPerson.setPolicyNumber(policyNumber);
        when(repository.findById("PA3432343")).thenReturn(Optional.of(insuredPerson));

        InsuredPerson result = insuredPersonService.findById(policyNumber);
        assertNotNull(result);
        assertEquals(policyNumber, result.getPolicyNumber());
        verify(repository).findById(policyNumber);
    }

    @Test
    void testFindByPolicyNumber_Failure(){
        String policyNumber = "PA00000";
        when(repository.findById("PA00000")).thenReturn(Optional.empty());
        assertThrows(CustomExceptions.ResourceNotFoundException.class, () -> {
            insuredPersonService.findById(policyNumber);
        });
        verify(repository).findById(policyNumber);
    }

    @Test
    void testFindByFirstName_Success(){
        String firstName = "Chandramouli";
        InsuredPerson insuredPerson = new InsuredPerson();
        insuredPerson.setFirstName(firstName);

        List<InsuredPerson> mockList = Collections.singletonList(insuredPerson);

        when(repository.findByFirstName("Chandramouli")).thenReturn((mockList));

        List<InsuredPerson> result = insuredPersonService.findByFirstName(firstName);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(firstName, result.get(0).getFirstName());
        verify(repository).findByFirstName(firstName);
    }

    @Test
    void testFindByFirstName_Unknown(){
        String firstName = "Unknown";
        when(repository.findByFirstName(firstName)).thenReturn(Collections.emptyList());
        assertThrows(CustomExceptions.ResourceNotFoundException.class, ()->{
            insuredPersonService.findByFirstName(firstName);
        });

        verify(repository).findByFirstName(firstName);
    }

    @Test
    void testFindByLastName_Success(){
        String lastName = "Pyla";
        InsuredPerson insuredPerson = new InsuredPerson();
        insuredPerson.setLastName(lastName);

        List<InsuredPerson> mockList = Collections.singletonList(insuredPerson);

        when(repository.findByLastName("Pyla")).thenReturn((mockList));

        List<InsuredPerson> result = insuredPersonService.findByLastName(lastName);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(lastName, result.get(0).getLastName());
        verify(repository).findByLastName(lastName);
    }

    @Test
    void testFindByLastName_Unknown(){
        String lastName = "Unknown";
        when(repository.findByLastName(lastName)).thenReturn(Collections.emptyList());
        assertThrows(CustomExceptions.ResourceNotFoundException.class, ()->{
            insuredPersonService.findByLastName(lastName);
        });

        verify(repository).findByLastName(lastName);
    }

    @Test
    void testFindByFirstNameFirstChar_Success(){
        String firstName = "Chandramouli";
        String firstChar = "C";
        InsuredPerson insuredPerson = new InsuredPerson();
        insuredPerson.setFirstName(firstName);

        List<InsuredPerson> mockList = Collections.singletonList(insuredPerson);

        when(repository.findByFirstNameStartsWith("C")).thenReturn((mockList));

        List<InsuredPerson> result = insuredPersonService.findByFirstCharOfFirstName(firstChar);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(true, result.get(0).getFirstName().startsWith(firstChar));
        verify(repository).findByFirstNameStartsWith(firstChar);
    }

    @Test
    void testFindByFirstNameFirstChar_failure(){
        String firstName = "Unknown";
        when(repository.findByFirstNameStartsWith("U")).thenReturn(Collections.emptyList());
        assertThrows(CustomExceptions.ResourceNotFoundException.class, ()->{
            insuredPersonService.findByFirstCharOfFirstName(String.valueOf(firstName.charAt(0)));
        });

        verify(repository).findByFirstNameStartsWith(String.valueOf(firstName.charAt(0)));
    }

    @Test
    void testFindByEmailId_Success(){
        String email = "abc@gmail.com";
        InsuredPerson insuredPerson = new InsuredPerson();
        insuredPerson.setEmail(email);
        List<InsuredPerson> mockList = Collections.singletonList(insuredPerson);
        when(repository.findByEmail(email)).thenReturn(mockList);

        List<InsuredPerson> result = insuredPersonService.findByEmail(email);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(email, result.get(0).getEmail());
        verify(repository).findByEmail(email);
    }

    @Test
    void testFindByEmailId_Failure(){
        String email = "abc@gmail.com";
        when(repository.findByEmail(email)).thenReturn(Collections.emptyList());

        assertThrows(CustomExceptions.ResourceNotFoundException.class, ()->{
            insuredPersonService.findByEmail(email);
        });
        verify(repository).findByEmail(email);
    }

    @Test
    void testPhoneNumber_Success(){
        String phoneNumber = "1234567890";
        InsuredPerson insuredPerson = new InsuredPerson();
        insuredPerson.setPhoneNumber(phoneNumber);

        List<InsuredPerson> mockList = Collections.singletonList(insuredPerson);
        when(repository.findByPhoneNumber(phoneNumber)).thenReturn(mockList);

        List<InsuredPerson> result = insuredPersonService.findByPhoneNumber(phoneNumber);
        assertNotNull(result);
        assertEquals(phoneNumber, result.get(0).getPhoneNumber());
        verify(repository).findByPhoneNumber(phoneNumber);
    }

    @Test
    void testPhoneNumber_NotFound() {
        String phoneNumber = "0000000000";

        // Mock repository to return empty list
        when(repository.findByPhoneNumber(phoneNumber)).thenReturn(Collections.emptyList());

        // Act & Assert
        CustomExceptions.ResourceNotFoundException exception =
                assertThrows(CustomExceptions.ResourceNotFoundException.class,
                        () -> insuredPersonService.findByPhoneNumber(phoneNumber));

        assertEquals("No InsuredPerson found with phone number: " + phoneNumber, exception.getMessage());
        verify(repository).findByPhoneNumber(phoneNumber);
    }

    @Test
    void testUserId_Success(){
        String userId = "abcdef@1234";
        InsuredPerson insuredPerson = new InsuredPerson();
        insuredPerson.setUserId(userId);
        when(repository.findByUserId(userId)).thenReturn(insuredPerson);

        InsuredPerson result = insuredPersonService.findByUserId(userId);
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(repository).findByUserId(userId);
    }

    @Test
    void testUserId_NotExist(){
        String userId = "abcdef@1234";
        when(repository.findByUserId(userId)).thenReturn(null);
        CustomExceptions.ResourceNotFoundException exception = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> insuredPersonService.findByUserId(userId)
        );

        assertEquals("No InsuredPerson found with userId: "+userId, exception.getMessage());
        verify(repository).findByUserId(userId);

    }

    @Test
    void testProfilePictureUpload_Success() throws IOException {
        String policyNumber = "PA2342213";
        InsuredPerson insuredPerson = new InsuredPerson();
        insuredPerson.setPolicyNumber(policyNumber);
        byte[] profilePictureBytes = "fake-image-bytes".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile(
                "profilePicture", "photo.jpg", "image/jpeg", profilePictureBytes);

        when(repository.findById(policyNumber)).thenReturn(Optional.of(insuredPerson));
        when(repository.save(any(InsuredPerson.class))).thenReturn(insuredPerson);
        insuredPersonService.saveProfilePicture(policyNumber, mockFile);
        assertArrayEquals(profilePictureBytes, insuredPerson.getProfilePicture());
        verify(repository).findById(policyNumber);
        verify(repository).save(insuredPerson);
    }

    @Test
    void testProfilePictureUpload_Failure_PolicyNumberNotFound(){
        String policyNumber = "PA0000000";
        InsuredPerson insuredPerson = new InsuredPerson();
        byte[] profilePictureBytes = "fake-image-bytes".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile(
                "profilePicture", "photo.jpg", "image/jpeg", profilePictureBytes);
        when(repository.findById(policyNumber)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, ()->{
            insuredPersonService.saveProfilePicture(policyNumber, mockFile);
        });

        assertEquals("Policy number not found", exception.getMessage());
        verify(repository).findById(policyNumber);
    }

    @Test
    void testUpdateInsuredPerson_Success() {
        String policyNumber = "PA123456";
        InsuredPerson existingUser = new InsuredPerson();
        existingUser.setPolicyNumber(policyNumber);
        existingUser.setUserId("OldUser@123");
        existingUser.setEmail("old@example.com");
        InsuredPersonRequest dto = new InsuredPersonRequest();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setUserId("NewUser@123");
        dto.setEmail("john@example.com");
        when(repository.findById(policyNumber)).thenReturn(Optional.of(existingUser));
        when(repository.findByUserId("NewUser@123")).thenReturn(null);
        when(repository.save(any(InsuredPerson.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        InsuredPerson updated = insuredPersonService.updateInsuredPerson(policyNumber, dto);
        assertNotNull(updated);
        assertEquals("John", updated.getFirstName());
        assertEquals("Doe", updated.getLastName());
        assertEquals("NewUser@123", updated.getUserId());
        assertEquals("john@example.com", updated.getEmail());

        verify(repository).findById(policyNumber);
        verify(repository).findByUserId("NewUser@123");
        verify(repository).save(any(InsuredPerson.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testUpdateInsuredPerson_Failure_PolicyNotFound() {
        String policyNumber = "PA000000";
        InsuredPersonRequest dto = new InsuredPersonRequest();
        when(repository.findById(policyNumber)).thenReturn(Optional.empty());
        CustomExceptions.ResourceNotFoundException ex = assertThrows(
                CustomExceptions.ResourceNotFoundException.class,
                () -> insuredPersonService.updateInsuredPerson(policyNumber, dto)
        );
        assertEquals("InsuredPerson not found with policyNumber: " + policyNumber, ex.getMessage());
        verify(repository).findById(policyNumber);
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateInsuredPerson_Failure_DuplicateUserId() {
        String policyNumber = "PA123456";
        InsuredPerson existingEntity = new InsuredPerson();
        existingEntity.setPolicyNumber(policyNumber);
        existingEntity.setUserId("User@123");

        InsuredPersonRequest dto = new InsuredPersonRequest();
        dto.setUserId("User@123");

        // Simulate another user already using the same userId
        InsuredPerson otherUser = new InsuredPerson();
        otherUser.setPolicyNumber("PA654321"); // different policy number with new UserId
        otherUser.setUserId("User@123");

        when(repository.findById(policyNumber)).thenReturn(Optional.of(existingEntity));
        when(repository.findByUserId("User@123")).thenReturn(otherUser);

        CustomExceptions.DuplicateUserIdException ex = assertThrows(
                CustomExceptions.DuplicateUserIdException.class,
                () -> insuredPersonService.updateInsuredPerson(policyNumber, dto)
        );

        assertEquals("UserId already exists: "+existingEntity.getUserId(), ex.getMessage());

        verify(repository).findById(policyNumber);
        verify(repository).findByUserId(otherUser.getUserId());
        verify(repository, never()).save(any());
    }

    @Test
    void testDeleteInsuredPerson_ByExistingPolicyNumber() {
        String policyNumber = "PA2324334";
        when(repository.existsById(policyNumber)).thenReturn(true);
        doNothing().when(repository).deleteById(policyNumber);
        insuredPersonService.deleteInsuredPerson(policyNumber);
        verify(repository).existsById(policyNumber);
        verify(repository).deleteById(policyNumber);
    }

    @Test
    void testDeleteInsuredPerson_WithoutExistingPolicyNumber() {
        String policyNumber = "PA00000";
        when(repository.existsById(policyNumber)).thenReturn(false);
        CustomExceptions.ResourceNotFoundException exception = assertThrows(CustomExceptions.ResourceNotFoundException.class,
                ()->{insuredPersonService.deleteInsuredPerson(policyNumber);});
        assertEquals("InsuredPerson not found with policyNumber: "+policyNumber, exception.getMessage());
        verify(repository).existsById(policyNumber);
        verify(repository, never()).deleteById(policyNumber);
    }

    @Test
    void testForgotPassword_Success() {
        String userId = "TestUser@123";
        InsuredPerson user = new InsuredPerson();
        user.setUserId(userId);
        user.setEmail("test@gmail.com");

        when(repository.findByUserId(userId)).thenReturn(user);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String result = insuredPersonService.forgotPassword(userId);

        assertTrue(result.contains("Reset OTP sent successfully"));
        verify(repository).findByUserId(userId);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testForgotPassword_UserNotFound() {
        String userId = "UnknownUser";
        when(repository.findByUserId(userId)).thenReturn(null);

        CustomExceptions.ResourceNotFoundException exception = assertThrows(CustomExceptions.ResourceNotFoundException.class, () -> {
            insuredPersonService.forgotPassword(userId);
        });
        assertEquals("User not found", exception.getMessage());
        verify(repository).findByUserId(userId);
    }

}


