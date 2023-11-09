package com.development.travellerhost.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;
import com.development.travellerhost.model.TravellerDocument;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravellerRepository extends JpaRepository<Traveller, Long> {
    Optional<Traveller> findByEmailAndMobileNumber(String email, String mobileNumber);
    boolean existsByEmailAndMobileNumberAndDocumentsId(String email, String mobileNumber, Long documentId);
        @Query("SELECT DISTINCT t FROM Traveller t JOIN t.documents d " +
                "WHERE (:email IS NULL OR t.email = :email) " +
                "AND (:mobile IS NULL OR t.mobileNumber = :mobile) " +
                "AND (:documentType IS NULL OR d.documentType = :documentType) " +
                "AND (:documentNumber IS NULL OR d.documentNumber = :documentNumber) " +
                "AND (:issuingCountry IS NULL OR d.issuingCountry = :issuingCountry) " +
                "AND d.active = true" )
        List<Traveller> searchActiveTravellers(
                @Param("email") String email,
                @Param("mobile") String mobile,
                @Param("documentType") DocumentType documentType,
                @Param("documentNumber") String documentNumber,
                @Param("issuingCountry") String issuingCountry);
    
}
    
