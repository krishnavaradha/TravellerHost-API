# TravellerHost-API
API performing CRUD operations for Traveller Host

Welcome to the Traveller Data Management API! This API allows you to manage traveller information, including personal details and associated documents.
## Table of Contents
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Installation](#installation)
- [Examples](#examples)
- [Contact](#contact)

## Getting Started
Assumptions :

To get started, ensure you have:
- H2 database enabled in Server Mode and not in embedded mode . Refer to [H2 Database Tutorial](https://www.h2database.com/html/tutorial.html) for details.
- SSL certificates configured, and a client cert/server set up for mutual TLS from the client.For testing please use client.pem, server.pem from SOAPUI/Postman to test.
- H2 instances and application instances can be scaled up and load-balanced using F5.
- RestTemplate load-balancing is not required as this is a single microservice without external service calls. Feel free to extend the solution as needed.
-In all the below endpoints Document Type is restricted to 
	PASSPORT("Passport"),
    ID_CARD("ID Card"),
    DRIVER_LICENSE("Driver's License");
-Lombok is used for generating Getters and Setters.
    
## API Endpoints


### Create Traveller

Create a new traveller record.

- **Endpoint**: `POST /api/travellers/create
- **Request Body**:
  ```json
  {
  "firstName": "krishna",
  "lastName": "varadha",
  "dateOfBirth": "1990-01-15",
  "email": "krishnavara@example.com",
  "mobileNumber": "1234579",
  "documents": [
    {
      "documentType": "PASSPORT",
      "documentNumber": "AB1971",
      "issuingCountry": "Austri"
     
    },
     {
      "documentType": "ID_CARD",
      "documentNumber": "AB1971",
      "issuingCountry": "Austri"
     
    }
     ]
}

Note: Only one unique document type can be assigned to a traveller.
Combination of Email , Mobile Number, Document Type is unique.
A new traveller can  be created but not updated.
Email and Mobile number in Travellers table is unique .

###Get Traveller
Retrieve traveller information.

Endpoint: GET /api/travellers/search
Query Parameters:
email or mobile or document (optional)
Note: Only retrieves the active document for a traveller.

###Update Traveller
Update traveller information.

Endpoint: PUT /api/travellers/update
Request Body: Same as the Create Traveller request body.

- **Request Body**:
  ```json
  {
  "firstName": "krishna",
  "lastName": "varadha",
  "dateOfBirth": "1990-01-15",
  "email": "krishnavara@example.com",
  "mobileNumber": "1234579",
  "documents": [
    {
      "documentType": "PASSPORT",
      "documentNumber": "AB1971",
      "issuingCountry": "Austri"
     
    },
     {
      "documentType": "ID_CARD",
      "documentNumber": "AB1971",
      "issuingCountry": "Austri"
     
    }
     ]
}

Note :

Only existing traveller can be updated, Traveller details will be fetched in the combination of Mobile and email .
Email and Mobile are unique keys and hence cannot be modified.

###Deactivate a traveller.

Endpoint: PUT /api/travellers/deactivate

- **Endpoint**: `PUT /deactivate`
- **Request Method**: `PUT`
- **Request Body**:
  - `firstName` (optional): First name of the traveller.
  - `lastName` (optional): Last name of the traveller.
  - `dateOfBirth` (optional): Date of birth of the traveller.
  - `email` (required): Email address of the traveller.
  - `mobileNumber` (required): Mobile number of the traveller.
{
  "firstName": "krishna",
  "lastName": "varadha1",
  "dateOfBirth": "1990-01-15",
  "email": "krishnavaradh@example.com",
  "mobileNumber": "123456790",
  "active": true,
  "documents": [
    {
      "documentType": "PASSPORT",
      "documentNumber": "AB1971",
      "issuingCountry": "Austri"
     
    }
    
     ]
}

Note: Deactivated travellers cannot be retrieved through any API operation.
Email and Mobile is required to deactivate a traveller.
Record is not deleted from the Database but Active flag of the traveller is made false.