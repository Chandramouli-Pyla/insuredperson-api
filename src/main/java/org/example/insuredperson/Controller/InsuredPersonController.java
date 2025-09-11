package org.example.insuredperson.Controller;

import org.example.insuredperson.DTO.*;
import org.example.insuredperson.Entity.InsuredPerson;
import org.example.insuredperson.Exception.CustomExceptions;
import org.example.insuredperson.Service.InsuredPersonService;
import org.example.insuredperson.Service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<APIResponse<List<InsuredPersonResponse>>> findAll(
                @RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        checkAdmin(token);
        List<InsuredPerson> allEntries = insuredPersonService.getAllInsuredList();
        List<InsuredPersonResponse> responseList = allEntries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new APIResponse<>(
                200,
                "All InsuredPersons retrieved successfully",
                responseList
        ));
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
    public ResponseEntity<APIResponse<List<InsuredPersonResponse>>> findByInsuredPersonFirstNameStartsWith(@RequestParam String firstChar,
                                                                                                           @RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        checkAdmin(token);
        List<InsuredPerson> persons = insuredPersonService.findByFirstCharOfFirstName(firstChar);
        List<InsuredPersonResponse> responseList = persons.stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(new APIResponse<>(
                200,
                persons.isEmpty() ? "No InsuredPerson found with firstName: " + firstChar
                        : "InsuredPersons retrieved successfully",
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
