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
    @Query(value = "SELECT t.* FROM Traveller t JOIN traveller_documents d " +
            "ON t.id = d.traveller_id " +
            "WHERE (:email IS NULL OR t.email = :email) " +
            "AND (:mobile IS NULL OR t.mobile_Number = :mobile) " +
            "AND (:documentType IS NULL OR d.document_type = :documentType) " +
            "AND (:documentNumber IS NULL OR d.document_number = :documentNumber) " +
            "AND (:issuingCountry IS NULL OR d.issuing_country = :issuingCountry) " +
            "AND t.active = true " +
            "AND d.active = true", nativeQuery = true)
    Traveller searchActiveTravellers(
                    @Param("email") String email,
                    @Param("mobile") String mobile,
                    @Param("documentType") DocumentType documentType,
                    @Param("documentNumber") String documentNumber,
                    @Param("issuingCountry") String issuingCountry);
	List<Traveller> findByFirstNameOrLastNameOrDateOfBirthOrEmailAndMobileNumber(String firstName, String lastName,
			String dateOfBirth, String email, String mobileNumber);
    
}
    
