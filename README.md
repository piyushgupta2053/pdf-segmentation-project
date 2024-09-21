
# PDF Segmentation Assignment

## Project Overview

This project is a **Spring Boot** application that segments a PDF into distinct sections based on whitespace gaps along the Y-axis. It uses **Apache PDFBox** to analyze and detect gaps between blocks of text and split the PDF into segments based on a user-defined number of cuts. The application exposes a REST API to handle file uploads, segmentation, and metadata retrieval.

## Features

- Segments a PDF file into distinct sections based on vertical whitespace gaps.
- Exposes REST API endpoints to upload, segment, retrieve, update, and delete PDF files.
- Uses **Apache PDFBox** for PDF processing and **Spring Boot** for REST API functionality.

## Technologies Used

- **Java 17**
- **Spring Boot 3.3.3**
- **Apache PDFBox 3.0.3**
- **Maven** for dependency and build management

## Prerequisites

Before running the application, ensure that you have the following installed:
- **Java 17** or higher
- **Maven** (to build and run the project)

## Setup Instructions

### 1. Clone the repository

Clone the project from the version control system to your local machine:

```bash
git clone <repository-url>
```

### 2. Navigate to the project directory

Go to the root of the project folder:

```bash
cd pdf-segmentation
```

### 3. Build the project using Maven

Run the following Maven command to build the project and install dependencies:

```bash
mvn clean install
```

### 4. Run the Spring Boot application

Start the application using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

The application will now be running on `http://localhost:8080`.

## API Documentation

The following REST API endpoints are available:

### 1. POST /api/pdf/segment-pdf

**Description**: Uploads a PDF and segments it into the specified number of cuts.

- **Parameters**:
  - `file`: The PDF file to be segmented (multipart/form-data).
  - `cuts`: The number of cuts to be made (integer).

**Response**: A ZIP file containing the segmented PDF files.

**Example**:
```bash
curl -F "file=@sample.pdf" -F "cuts=3" http://localhost:8080/api/pdf/segment-pdf --output segmented_pdfs.zip
```

### 2. GET /api/pdf/pdf-metadata/{id}

**Description**: Retrieves metadata for a previously processed PDF, such as the number of segments and their cut positions.

- **Path Parameter**:
  - `id`: The unique identifier of the processed PDF.

**Response**: JSON object containing the metadata for the segmented PDF.

**Example**:
```bash
curl http://localhost:8080/api/pdf/pdf-metadata/{id}
```

### 3. PUT /api/pdf/update-segmentation/{id}

**Description**: Updates the segmentation details of a previously processed PDF by changing the number of cuts.

- **Path Parameter**:
  - `id`: The unique identifier for the processed PDF.
- **Query Parameter**:
  - `cuts`: The new number of cuts to be applied.

**Response**: A confirmation message upon successful update.

**Example**:
```bash
curl -X PUT "http://localhost:8080/api/pdf/update-segmentation/{id}?cuts=5"
```

### 4. PATCH /api/pdf/modify-segmentation/{id}

**Description**: Partially updates segmentation details, such as modifying specific cut positions without reprocessing the entire PDF.

- **Path Parameter**:
  - `id`: The unique identifier for the processed PDF.
- **Request Body**: JSON body with fields to modify (e.g., specific segment positions).

**Response**: A confirmation message upon successful update.

**Example**:
```bash
curl -X PATCH -H "Content-Type: application/json" -d '{"cuts": 4}' http://localhost:8080/api/pdf/modify-segmentation/{id}
```

### 5. DELETE /api/pdf/delete-pdf/{id}

**Description**: Deletes a processed PDF and all its associated segments.

- **Path Parameter**:
  - `id`: The unique identifier for the processed PDF.

**Response**: A confirmation message upon successful deletion.

**Example**:
```bash
curl -X DELETE http://localhost:8080/api/pdf/delete-pdf/{id}
```

## Project Structure

- `src/main/java/com/pdfprocessor/pdf_segmentation/`: Contains the main Spring Boot application and service files.
- `src/main/resources/`: Contains configuration files for the application.
- `pom.xml`: Project Object Model (POM) file, which contains dependencies and build configuration.

## Building and Running Tests

To run tests, execute the following Maven command:
```bash
mvn test
```

Make sure the application passes the tests before deploying or further development.

## Known Issues and Limitations

- **Cutting through paragraphs**: The current logic cuts PDFs based on vertical whitespace, but additional refinements may be needed to avoid cutting through paragraphs in complex documents.
- **Memory Usage**: Processing very large PDFs may require additional memory optimizations.

## Future Improvements

- Implement authentication for the REST API (e.g., API keys or OAuth).
- Add more robust error handling for file uploads and invalid inputs.
- Refine the segmentation logic to handle different types of documents (e.g., those with complex layouts or images).

## License

This project is licensed under the MIT License. See the LICENSE file for more details.
