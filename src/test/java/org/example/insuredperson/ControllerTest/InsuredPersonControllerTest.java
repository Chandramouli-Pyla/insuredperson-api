package org.example.insuredperson.ControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.insuredperson.DTO.LoginRequest;
import org.example.insuredperson.DTO.InsuredPersonRequest;
import org.example.insuredperson.Entity.InsuranceType;
import org.example.insuredperson.Entity.InsuredPerson;
import org.example.insuredperson.Repo.InsuredPersonRepository;
import org.example.insuredperson.Service.InsuredPersonService;
import org.example.insuredperson.Service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InsuredPersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InsuredPersonRepository repository;

    @Mock
    private InsuredPersonService insuredPersonService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;

    @MockitoBean  // MockitoBean to mock the service in Spring context
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtToken = "Bearer 325834heftih5894urhfn3t94y5.34984u53jo34tot.345u9834u3rj34itj0t3403i4984ut3t";

        // Mock the JwtService behavior
        when(jwtService.generateToken(org.mockito.Mockito.any()))
                .thenReturn(jwtToken);

        when(jwtService.validateTokenAndGetUserId(org.mockito.Mockito.anyString()))
                .thenReturn(String.valueOf(true));

    }



    private static InsuredPersonRequest getInsuredPersonRequest() {
        InsuredPersonRequest request = new InsuredPersonRequest();
        request.setPolicyNumber("PA7876543");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAge(30);
        request.setUserId("Johndoe@1210");
        request.setPassword("StrongP@ssw0rd");
        request.setEmail("john.doe@gmail.com");
        request.setRole("Admin");
        request.setPhoneNumber("2134567890");
        request.setStreet("123 Main St");
        request.setApartment("Apt 4B");
        request.setCity("New York");
        request.setState("NY");
        request.setCountry("USA");
        request.setZipcode("10001");
        request.setTypeOfInsurance(InsuranceType.HEALTH_INSURANCE);
        return request;
    }

    @Test
    void createInsuredPerson_Success() throws Exception {
        //Build DTO
        InsuredPersonRequest request = getInsuredPersonRequest();

        // Convert to JSON for `info` part
        MockMultipartFile infoPart = new MockMultipartFile(
                "info",
                "info.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        //Adding a document
        MockMultipartFile document = new MockMultipartFile(
                "documents",
                "id-proof.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Dummy file content".getBytes()
        );

        //Perform the request
        mockMvc.perform(multipart("/api/insuredpersons")
                        .file(infoPart)
                        .file(document)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("InsuredPerson created successfully"))
                .andExpect(jsonPath("$.data.policyNumber").value(request.getPolicyNumber()))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }


    @Test
    void testLogin_Success() throws Exception {
        InsuredPerson person = new InsuredPerson();
        person.setUserId("TestUser@123");
        person.setPassword(passwordEncoder.encode("Password@123"));
        person.setPolicyNumber("POL1234");
        person.setEmail("test@gmail.com");
        person.setRole("User");
        repository.save(person);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserId("TestUser@123");
        loginRequest.setPassword("Password@123");

        mockMvc.perform(post("/api/insuredpersons/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Yes, you are in! Here is your policy number: POL1234"))
                .andExpect(jsonPath("$.data.user.policyNumber").value("POL1234"))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    void testLogin_Failure_InvalidCredentials() throws Exception {
        InsuredPerson person = new InsuredPerson();
        person.setUserId("TestUser@123");
        person.setPassword(passwordEncoder.encode("Password@123"));

        LoginRequest invalidLogin = new LoginRequest();
        invalidLogin.setUserId("TestUser@459");
        invalidLogin.setPassword("Password@123");

        mockMvc.perform(post("/api/insuredpersons/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials!!! Please try again."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testGetByPolicyNumber_Unauthorized() throws Exception {
        String invalidToken = "invalidToken";

        // Make JwtService return false for invalid token
        when(jwtService.validateTokenAndGetUserId(invalidToken)).thenReturn(null); // or throw exception depending on your filter

        mockMvc.perform(get("/api/insuredpersons/PA7876543")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }



    @Test
    void deleteInsuredPerson_Success() throws Exception {
        String policyNumber = "POL1234";

        InsuredPerson person = new InsuredPerson();
        person.setPolicyNumber(policyNumber);
        person.setUserId("TestUser@123");
        person.setPassword(passwordEncoder.encode("Password@123"));
        repository.save(person);


        String rawToken = jwtToken.substring(7);

        when(jwtService.validateTokenAndGetUserId(rawToken)).thenReturn(String.valueOf(true));
        when(jwtService.extractUserRole(rawToken)).thenReturn("Admin");

        mockMvc.perform(delete("/api/insuredpersons/{policyNumber}", policyNumber)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("InsuredPerson deleted successfully"));
    }


    @Test
    void deleteInsuredPerson_NotFound() throws Exception {
        String policyNumber = "NON_EXISTENT";
        String rawToken = "your-valid-admin-jwt";

        when(jwtService.validateTokenAndGetUserId(rawToken)).thenReturn(String.valueOf(true));
        when(jwtService.extractUserRole(rawToken)).thenReturn("Admin");

        mockMvc.perform(delete("/api/insuredpersons/{policyNumber}", policyNumber)
                        .header("Authorization", "Bearer " + rawToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("InsuredPerson not found with policyNumber: NON_EXISTENT"));
    }


    @Test
    void getInsuredPersonById_Success() throws Exception {
        // Given: a saved insured person in DB
        String policyNumber = "PA123456";
        String userId = "Johndoe@123";
        String token = "valid.jwt.token";

        InsuredPerson person = new InsuredPerson();
        person.setPolicyNumber(policyNumber);
        person.setUserId(userId);
        person.setPassword(passwordEncoder.encode("StrongP@ssw0rd"));
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setEmail("john.doe@example.com");
        person.setRole("Admin");

        repository.save(person);

        // When: mocking JWT validation
        when(jwtService.validateTokenAndGetUserId(token)).thenReturn(String.valueOf(true));
        when(jwtService.extractUsername(token)).thenReturn(userId);
        when(jwtService.extractUserRole(token)).thenReturn("Admin");

        // Then: perform GET request
        mockMvc.perform(get("/api/insuredpersons/{policyNumber}", policyNumber)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("InsuredPerson retrieved successfully"))
                .andExpect(jsonPath("$.data.policyNumber").value(policyNumber))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }

    @Test
    void getInsuredPersonById_Unauthorized() throws Exception {
        String policyNumber = "PA123456";
        String invalidToken = "invalid.jwt.token";

        when(jwtService.validateTokenAndGetUserId(invalidToken)).thenReturn(String.valueOf(false));

        mockMvc.perform(get("/api/insuredpersons/{policyNumber}", policyNumber)
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid role"));
    }


    @Test
    void uploadProfilePicture_Success() throws Exception {
        String policyNumber = "PA123456";
        InsuredPerson person = new InsuredPerson();
        person.setPolicyNumber(policyNumber);
        person.setUserId("JohnDoe@123");
        person.setPassword(passwordEncoder.encode("StrongP@ssw0rd"));
        person.setFirstName("John");
        person.setEmail("john.doe@example.com");
        person.setRole("User");
        repository.save(person);

        MockMultipartFile profilePicture = new MockMultipartFile(
                "profilePicture",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/insuredpersons/profile-picture/{policyNumber}", policyNumber)
                        .file(profilePicture)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Profile picture uploaded successfully"));
    }

    @Test
    void uploadProfilePicture_NotFound() throws Exception {
        String policyNumber = "NON_EXISTENT";

        MockMultipartFile profilePicture = new MockMultipartFile(
                "profilePicture",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/insuredpersons/profile-picture/{policyNumber}", policyNumber)
                        .file(profilePicture)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Policy number not found"));
    }

    @Test
    void getProfilePicture_Success() throws Exception {
        String policyNumber = "PA123456";
        byte[] imageBytes = "fake-image-content".getBytes();

        InsuredPerson person = new InsuredPerson();
        person.setPolicyNumber(policyNumber);
        person.setUserId("JohnDoe@123");
        person.setPassword(passwordEncoder.encode("StrongP@ssw0rd"));
        person.setFirstName("John");
        person.setEmail("john.doe@example.com");
        person.setRole("User");
        person.setProfilePicture(imageBytes);
        repository.save(person);

        mockMvc.perform(get("/api/insuredpersons/profile-picture/{policyNumber}", policyNumber))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageBytes));
    }

    @Test
    void getProfilePicture_NotFound_PolicyNumberMissing() throws Exception {
        String policyNumber = "NON_EXISTENT";

        mockMvc.perform(get("/api/insuredpersons/profile-picture/{policyNumber}", policyNumber))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getProfilePicture_NotFound_ImageMissing() throws Exception {
        String policyNumber = "PA000001";

        InsuredPerson person = new InsuredPerson();
        person.setPolicyNumber(policyNumber);
        person.setUserId("JaneDoe@123");
        person.setPassword(passwordEncoder.encode("StrongP@ssw0rd"));
        person.setFirstName("Jane");
        person.setEmail("jane.doe@example.com");
        person.setRole("User");
        person.setProfilePicture(null);
        repository.save(person);

        mockMvc.perform(get("/api/insuredpersons/profile-picture/{policyNumber}", policyNumber))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllInsuredPersons_Success() throws Exception {
        // Arrange
        InsuredPerson person1 = new InsuredPerson();
        person1.setPolicyNumber("PA1001");
        person1.setUserId("Alice@123");
        person1.setPassword(passwordEncoder.encode("Pass123"));
        person1.setFirstName("Alice");
        person1.setEmail("alice@example.com");
        person1.setRole("Admin");

        InsuredPerson person2 = new InsuredPerson();
        person2.setPolicyNumber("PA1002");
        person2.setUserId("Bob@123");
        person2.setPassword(passwordEncoder.encode("Pass123"));
        person2.setFirstName("Bob");
        person2.setEmail("bob@example.com");
        person2.setRole("Admin");

        repository.saveAll(List.of(person1, person2));

        // Simulate a valid admin token
        String adminToken = "VALID_ADMIN_TOKEN";
        when(jwtService.validateTokenAndGetUserId(adminToken)).thenReturn("SomeUserId");
        when(jwtService.extractUserRole(adminToken)).thenReturn("Admin");
        // Act & Assert
        mockMvc.perform(get("/api/insuredpersons")
                        .header("Authorization","Bearer "+ adminToken)
                        .param("offSet", "0")
                        .param("pageSize", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("All InsuredPersons retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.currentPage").value(0));
    }


}
