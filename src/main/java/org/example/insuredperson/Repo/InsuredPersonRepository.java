package org.example.insuredperson.Repo;

import io.swagger.models.auth.In;
import jakarta.transaction.Transactional;
import org.example.insuredperson.Entity.InsuredPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//JPA repository which is the main source to communicate our entity and service layers with DB
public interface InsuredPersonRepository extends JpaRepository<InsuredPerson, String>{

    List<InsuredPerson> findByFirstName(String firstName);
    List<InsuredPerson> findByLastName(String lastName);
    List<InsuredPerson> findByFirstNameStartsWith(String firstChar);
    List<InsuredPerson> findByEmail(String email);
    List<InsuredPerson> findByPhoneNumber(String phoneNumber);
    boolean existsByUserId(String userId);
    InsuredPerson findByUserId(String userId);

    @Modifying
    @Transactional
    @Query("UPDATE InsuredPerson i SET i.firstName = :firstName WHERE i.policyNumber = :policyNumber")
    int updateFirstName(String policyNumber, String firstName);

    @Modifying
    @Transactional
    @Query("UPDATE InsuredPerson i SET i.lastName = :lastName WHERE i.policyNumber = :policyNumber")
    int updateLastName(String policyNumber, String lastName);

    @Modifying
    @Transactional
    @Query("UPDATE InsuredPerson i SET i.age = :age WHERE i.policyNumber = :policyNumber")
    int updateAge(String policyNumber, Integer age);

    @Modifying
    @Transactional
    @Query("UPDATE InsuredPerson i SET i.userId = :userId WHERE i.policyNumber = :policyNumber")
    int updateUserId(String policyNumber, String userId);

    @Modifying
    @Transactional
    @Query("UPDATE InsuredPerson i SET i.password = :password WHERE i.policyNumber = :policyNumber")
    int updatePassword(String policyNumber, String password);
}
