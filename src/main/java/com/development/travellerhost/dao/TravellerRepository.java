package com.development.travellerhost.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.development.travellerhost.model.Traveller;
import com.development.travellerhost.model.TravellerDocument;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravellerRepository extends JpaRepository<Traveller, Long> {
	// Define a custom query method to check for duplicate email, mobile number, and documents
    @Query(value = "SELECT COUNT(*) > 0 FROM traveller t " +
            "INNER JOIN traveller_documents d ON t.id = d.traveller_id " +
            "WHERE t.email = :email AND t.mobile_number = :mobileNumber " +
            "AND d.id IN :documentIds", nativeQuery = true)
    boolean existsByEmailMobileAndDocuments(
            @Param("email") String email,
            @Param("mobileNumber") String mobileNumber,
            @Param("documentIds") List<Long> documentIds);
    Optional<Traveller> findByEmailAndMobileNumber(String email, String mobileNumber);
    boolean existsByEmailAndMobileNumberAndDocumentsId(String email, String mobileNumber, Long documentId);

    

}