package com.development.travellerhost.dao;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;

@Repository
public interface TravellerRepository extends JpaRepository<Traveller, Long> {
    Optional<Traveller> findByEmailAndMobileNumber(String email, String mobileNumber);
    boolean existsByEmailAndMobileNumberAndDocumentsId(String email, String mobileNumber, Long documentId);
    @Query("SELECT t FROM Traveller t LEFT JOIN FETCH t.documents d " +
            "WHERE (:email IS NULL OR t.email = :email) " +
            "AND (:mobile IS NULL OR t.mobileNumber = :mobile) " +
            "AND (:documentType IS NULL OR d.documentType = :documentType) " +
            "AND (:documentNumber IS NULL OR d.documentNumber = :documentNumber) " +
            "AND (:issuingCountry IS NULL OR d.issuingCountry = :issuingCountry) AND d.active = true" )
    Traveller searchActiveTravellers(
                    @Param("email") String email,
                    @Param("mobile") String mobile,
                    @Param("documentType") DocumentType documentType,
                    @Param("documentNumber") String documentNumber,
                    @Param("issuingCountry") String issuingCountry);
	List<Traveller> findByFirstNameOrLastNameOrDateOfBirthOrEmailAndMobileNumber(String firstName, String lastName,
			String dateOfBirth, String email, String mobileNumber);
    
}
    
