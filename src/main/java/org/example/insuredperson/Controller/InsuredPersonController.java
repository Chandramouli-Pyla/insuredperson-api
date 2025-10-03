package org.example.insuredperson.Controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.insuredperson.DTO.*;
import org.example.insuredperson.Entity.InsuredPerson;
import org.example.insuredperson.Exception.CustomExceptions;
import org.example.insuredperson.Service.InsuredPersonService;
import org.example.insuredperson.Service.JwtService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "https://insuredperson-api-ui-458668609912.us-central1.run.app")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("api/insuredpersons")
public class InsuredPersonController {

    public final InsuredPersonService insuredPersonService;
    public final JwtService jwtService;

    public InsuredPersonController(InsuredPersonService insuredPersonService, JwtService jwtService) {
        this.insuredPersonService = insuredPersonService;
        this.jwtService = jwtService;
    }

    // Create new InsuredPerson
    @PostMapping
    public ResponseEntity<APIResponse<InsuredPersonResponse>> createInsuredPerson(
            @RequestBody InsuredPersonRequest requestDto) {

        InsuredPerson savedEntity = insuredPersonService.createInsuredPerson(requestDto);
        InsuredPersonResponse response = mapToResponse(savedEntity);

        return ResponseEntity.status(201)
                .body(new APIResponse<>(201, "InsuredPerson created successfully", response));
    }


    @PostMapping("/login")
    public ResponseEntity<APIResponse<Map<String, Object>>> login(@RequestBody LoginRequest loginRequest) {
        InsuredPerson user = insuredPersonService.login(loginRequest);
        String token = jwtService.generateToken(user);
        Map<String,Object> responseData = new HashMap<>();
        responseData.put("user", mapToResponse(user));
        responseData.put("token", token);
        return ResponseEntity.ok(new APIResponse<>(
                200,
                "Yes, you are in! Here is your policy number: " + user.getPolicyNumber(),
                responseData)
        );
    }
    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "0") int offSet,
            @RequestParam(defaultValue = "3") int pageSize) {

        String token = auth.substring(7);
        checkAdmin(token);

        Page<InsuredPerson> allEntries = insuredPersonService.getAllInsuredList(offSet, pageSize);
        List<InsuredPersonResponse> responseList = allEntries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "All InsuredPersons retrieved successfully");
        response.put("data", responseList);
        response.put("hasNext", allEntries.hasNext());
        response.put("hasPrevious", allEntries.hasPrevious());
        response.put("totalPages", allEntries.getTotalPages());
        response.put("totalElements", allEntries.getTotalElements());
        response.put("currentPage", allEntries.getNumber());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/policySearch")
    public ResponseEntity<APIResponse<List<InsuredPersonResponse>>> searchPolicies(
            @RequestParam String query,
            @RequestHeader("Authorization") String auth) {

        String token = auth.substring(7);

        List<InsuredPerson> persons = new ArrayList<>();

        // Case 1: Try as Policy Number first → User can access own, Admin can access anyone
        try {
            InsuredPerson entity = insuredPersonService.findById(query);
            checkUserOrAdminForPolicy(token, entity);

            return ResponseEntity.ok(
                    new APIResponse<>(
                            200,
                            "Policy retrieved successfully",
                            List.of(mapToResponse(entity))
                    )
            );
        } catch (CustomExceptions.ResourceNotFoundException e) {
            // Not a policy number → continue to search by other fields (Admin only)
        }

        // Only Admin can search by other fields
        checkAdmin(token);

        try {
            // Try each field one by one; first match wins
            try { persons = insuredPersonService.findByFirstName(query); } catch (CustomExceptions.ResourceNotFoundException ignored) {}
            if (persons.isEmpty()) try { persons = insuredPersonService.findByLastName(query); } catch (CustomExceptions.ResourceNotFoundException ignored) {}
            if (persons.isEmpty()) try { persons = insuredPersonService.findByFirstCharOfFirstName(query); } catch (CustomExceptions.ResourceNotFoundException ignored) {}
            if (persons.isEmpty()) try { persons = insuredPersonService.findByEmail(query); } catch (CustomExceptions.ResourceNotFoundException ignored) {}
            if (persons.isEmpty()) try { persons = insuredPersonService.findByPhoneNumber(query); } catch (CustomExceptions.ResourceNotFoundException ignored) {}
            if (persons.isEmpty()) try { persons = Collections.singletonList(insuredPersonService.findByUserId(query)); } catch (CustomExceptions.ResourceNotFoundException ignored) {}

            if (persons.isEmpty()) {
                throw new CustomExceptions.ResourceNotFoundException("No InsuredPerson found with query: " + query);
            }

            List<InsuredPersonResponse> responseList = persons.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new APIResponse<>(
                            200,
                            "Records retrieved successfully",
                            responseList
                    )
            );
        } catch (CustomExceptions.ResourceNotFoundException e) {
            throw e; // propagate original exception
        }
    }

    @GetMapping({"/{policyNumber}"})
    public ResponseEntity<APIResponse<InsuredPersonResponse>> findById(
                        @PathVariable String policyNumber, @RequestHeader("Authorization") String auth) {

        String token = auth.substring(7);
        InsuredPerson entity = insuredPersonService.findById(policyNumber);
        checkUserOrAdminForPolicy(token, entity);
        return ResponseEntity.ok(
                new APIResponse<>(200, "InsuredPerson retrieved successfully", mapToResponse(entity))
        );
    }

    @GetMapping("/findByFirstName")
    public ResponseEntity<APIResponse<List<InsuredPersonResponse>>> findByFirstName(@RequestParam String firstName,
                                                                                    @RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        checkAdmin(token);
        List<InsuredPerson> persons = insuredPersonService.findByFirstName(firstName);
        List<InsuredPersonResponse> responseList = persons.stream()
                                                    .map(this::mapToResponse)
                                                    .collect(Collectors.toList());
        return ResponseEntity.ok(new APIResponse<>(
                200,
                persons.isEmpty() ? "No InsuredPerson found with firstName: " + firstName
                        : "InsuredPersons retrieved successfully",
                responseList
        ));
    }

    @GetMapping("/findByLastName")
    public ResponseEntity<APIResponse<List<InsuredPersonResponse>>> findByLastName(@RequestParam String lastName,
                                                                                   @RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        checkAdmin(token);
        List<InsuredPerson> persons = insuredPersonService.findByLastName(lastName);
        List<InsuredPersonResponse> responseList = persons.stream()
                                                    .map(this::mapToResponse)
                                                    .collect(Collectors.toList());
        return ResponseEntity.ok(new APIResponse<>(
                200,
                persons.isEmpty() ? "No InsuredPerson found with lastName: " + lastName
                        : "InsuredPersons retrieved successfully",
                responseList
        ));
    }

    @GetMapping("/findByFirstChar")
    public ResponseEntity<APIResponse<List<InsuredPersonResponse>>> findByInsuredPersonFirstNameStartsWith(
            @RequestParam String firstChar,
            @RequestHeader("Authorization") String auth) {

        String token = auth.substring(7);
        checkAdmin(token);

        if (firstChar == null || firstChar.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new APIResponse<>(
                    400,
                    "First character is required",
                    List.of()
            ));
        }

        List<InsuredPerson> persons = insuredPersonService.findByFirstCharOfFirstName(firstChar);
        List<InsuredPersonResponse> responseList = persons.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new APIResponse<>(
                200,
                persons.isEmpty()
                        ? "No InsuredPerson found with firstName starting with: " + firstChar
                        : "InsuredPersons retrieved successfully!!!",
                responseList
        ));
    }


    @PatchMapping("/{policyNumber}")
    public ResponseEntity<APIResponse<InsuredPersonResponse>> updateInsuredPerson(@PathVariable String policyNumber,
            @RequestBody InsuredPersonRequest requestDto,  @RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        System.out.println(token);
        checkAdmin(token);
        requestDto.setPolicyNumber(policyNumber);
        InsuredPerson updatedPerson = insuredPersonService.updateInsuredPerson(policyNumber,requestDto);
        InsuredPersonResponse response = mapToResponse(updatedPerson);
        return ResponseEntity.ok(new APIResponse<>(
                200,
                "InsuredPerson updated successfully",
                response
        ));
    }

    @DeleteMapping("/{policyNumber}")
    public ResponseEntity<APIResponse<Void>> deleteById(@PathVariable String policyNumber,  @RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        checkAdmin(token);
        insuredPersonService.deleteInsuredPerson(policyNumber);
        return ResponseEntity.ok(new APIResponse<>(200, "InsuredPerson deleted successfully", null));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<APIResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String message = insuredPersonService.forgotPassword(request.getUserId()); //, request.getEmail()

        return ResponseEntity.ok(
                new APIResponse<>(200, "Password reset token sent successfully", message)
        );
    }


    @PostMapping("/reset-password")
    public ResponseEntity<APIResponse<InsuredPersonResponse>> resetPassword(@RequestBody ResetPasswordRequest request) {
//        InsuredPerson updatedUser = insuredPersonService.resetPassword(request.getToken(), request.getNewPassword(), request.getConfirmNewPassword());
        InsuredPerson updatedUser = insuredPersonService.resetPassword(request);

        InsuredPersonResponse response = mapToResponse(updatedUser);
        return ResponseEntity.ok(
                new APIResponse<>(200, "Password reset successful for the follwing User", response)
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<APIResponse<InsuredPersonResponse>> changePassword(@RequestBody ChangePasswordRequest request){
        InsuredPerson updateUserPassword = insuredPersonService.updatePassword(request);
        InsuredPersonResponse response = mapToResponse(updateUserPassword);
        return ResponseEntity.ok(new APIResponse<>(200, "Password changed successfully for the User", response));
    }

    // Helper method to map entity -> response DTO
    private InsuredPersonResponse mapToResponse(InsuredPerson entity) {
        InsuredPersonResponse response = new InsuredPersonResponse();
        response.setPolicyNumber(entity.getPolicyNumber());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setAge(entity.getAge());
        response.setUserId(entity.getUserId());
        response.setEmail(entity.getEmail());
        response.setRole(entity.getRole());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setStreet(entity.getStreet());
        response.setApartment(entity.getApartment());
        response.setCity(entity.getCity());
        response.setState(entity.getState());
        response.setCountry(entity.getCountry());
        response.setZipcode(entity.getZipcode());
        response.setTypeOfInsurance(entity.getTypeOfInsurance());

        return response;
    }

    // --- Helpers ---
    private void checkAdmin(String token) {
        String role = jwtService.extractUserRole(token);
        if (!"Admin".equalsIgnoreCase(role)) {
            throw new CustomExceptions.UnauthorizedException("Admins only");
        }
    }


    private void checkUserOrAdminForPolicy(String token, InsuredPerson person) {
        String role = jwtService.extractUserRole(token);
        String tokenPolicyNumber = jwtService.extractUsername(token); // subject = policyNumber

        if ("Admin".equalsIgnoreCase(role)) {
            return; // Admin can access anything
        }

        if ("User".equalsIgnoreCase(role)) {
            if (!person.getPolicyNumber().equals(tokenPolicyNumber)) {
                throw new CustomExceptions.UnauthorizedException("Access denied! You can only view your own details.");
            }
            return;
        }

        throw new CustomExceptions.UnauthorizedException("Invalid role");
    }


}
