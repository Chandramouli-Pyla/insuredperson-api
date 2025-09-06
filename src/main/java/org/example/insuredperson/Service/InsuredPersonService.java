package org.example.insuredperson.Service;

import org.example.insuredperson.DTO.APIResponse;
import org.example.insuredperson.DTO.InsuredPersonRequest;
import org.example.insuredperson.DTO.LoginRequest;
import org.example.insuredperson.Entity.InsuredPerson;
import org.example.insuredperson.Exception.CustomExceptions;
import org.example.insuredperson.Repo.InsuredPersonRepository;
import org.springframework.stereotype.Service;
import org.example.insuredperson.Service.JwtService;

import java.util.List;

@Service
public class    InsuredPersonService {
    private final InsuredPersonRepository repository;
    private final JwtService jwtService;
    private ValidationService validationService;

    //constructor where it will initialize the obj
    public InsuredPersonService(InsuredPersonRepository repository,  JwtService jwtService, ValidationService validationService) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.validationService = validationService;
    }


    //getting all isnured persons data from the data base
    public List<InsuredPerson> getAllInsuredList() {
        return repository.findAll();
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
        entity.setPassword(dto.getPassword());
        entity.setEmail(dto.getEmail());
        entity.setRole(dto.getRole());
        return repository.save(entity);
    }

public InsuredPerson updateInsuredPerson(String pathPolicyNumber, InsuredPersonRequest dto) {
    //Fetch the record to update using the path parameter
    InsuredPerson entity = repository.findById(pathPolicyNumber)
            .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException(
                    "InsuredPerson not found with policyNumber: " + pathPolicyNumber));

    //Update fields
    if (dto.getFirstName() != null) entity.setFirstName(dto.getFirstName());
    if (dto.getLastName() != null) entity.setLastName(dto.getLastName());
    if (dto.getAge() != null) entity.setAge(dto.getAge());
    if(dto.getEmail() != null) entity.setEmail(dto.getEmail());
    if(dto.getRole() !=null) entity.setRole(dto.getRole());

    //UserId check
    if (dto.getUserId() != null) {
        InsuredPerson existingUser = repository.findByUserId(dto.getUserId());
        if (existingUser != null && !existingUser.getPolicyNumber().equals(pathPolicyNumber)) {
            throw new CustomExceptions.DuplicateUserIdException(
                    "UserId already exists: " + dto.getUserId());
        }
        entity.setUserId(dto.getUserId());
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
        // generate JWT using userId as subject
        return jwtService.generateToken(user);
    }

    //Login credentials service
    public InsuredPerson login(LoginRequest loginRequest) {
        InsuredPerson user = repository.findByUserId(loginRequest.getUserId());

        if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
            throw new CustomExceptions.UnauthorizedException("Invalid credentials!!! Please try again.");
        }

        return user;
    }

}
