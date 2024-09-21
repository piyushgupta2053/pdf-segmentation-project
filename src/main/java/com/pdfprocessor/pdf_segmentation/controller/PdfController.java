package com.pdfprocessor.pdf_segmentation.controller;

import com.pdfprocessor.pdf_segmentation.service.PdfSegmentationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfSegmentationService pdfSegmentationService;

    private final Map<String, PdfMetadata> pdfMetadataStorage = new HashMap<>();

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB file size limit

    // POST /segment-pdf: Segment PDF and return the segmented sections as a ZIP file
    @Operation(summary = "Segment PDF", description = "Segments a PDF file into specified parts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully segmented PDF"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/segment-pdf")
    public ResponseEntity<byte[]> segmentPdf(@RequestParam("file") MultipartFile file,
                                             @RequestParam("cuts") Integer cuts) {
        // Input Validation

        // Validate file type (only PDF files allowed)
        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Invalid file type. Only PDF files are accepted.").getBytes());
        }

        // Validate file size (limit to 10 MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("File size exceeds the limit of 10 MB.").getBytes());
        }

        // Validate number of cuts (must be a positive integer)
        if (cuts == null || cuts <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("The number of cuts must be a positive integer.").getBytes());
        }

        try {
            // Convert MultipartFile to File
            File pdfFile = convertMultipartFileToFile(file);

            // Call the service to segment the PDF
            List<File> segmentedFiles = pdfSegmentationService.segmentPdf(pdfFile, cuts);

            // Store metadata (dummy storage in this example)
            String pdfId = pdfFile.getName(); // Using file name as ID
            PdfMetadata metadata = new PdfMetadata(pdfId, segmentedFiles.size(), cuts);
            pdfMetadataStorage.put(pdfId, metadata);

            // Create a ZIP file containing all segmented PDFs
            byte[] zipFile = createZipFromFiles(segmentedFiles);

            // Prepare the HTTP headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "segmented_pdfs.zip");

            // Return the ZIP file as a response with 201 status (Created)
            return new ResponseEntity<>(zipFile, headers, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error processing PDF file: " + e.getMessage()).getBytes());
        }
    }

    // GET /pdf-metadata/{id}: Retrieve metadata for the processed PDF
    @Operation(summary = "Retrieve PDF Metadata", description = "Get metadata for the processed PDF by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved metadata"),
            @ApiResponse(responseCode = "404", description = "PDF metadata not found")
    })
    @GetMapping("/pdf-metadata/{id}")
    public ResponseEntity<PdfMetadata> getPdfMetadata(@PathVariable("id") String pdfId) {
        PdfMetadata metadata = pdfMetadataStorage.get(pdfId);
        if (metadata == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(metadata, HttpStatus.OK);
    }

    // PUT /update-segmentation/{id}: Update segmentation details (e.g., new number of cuts)
    @Operation(summary = "Update Segmentation", description = "Update the number of cuts for the segmented PDF.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Segmentation updated successfully"),
            @ApiResponse(responseCode = "404", description = "PDF metadata not found")
    })
    @PutMapping("/update-segmentation/{id}")
    public ResponseEntity<String> updateSegmentation(@PathVariable("id") String pdfId,
                                                     @RequestParam("cuts") int newCuts) {
        PdfMetadata metadata = pdfMetadataStorage.get(pdfId);
        if (metadata == null) {
            return new ResponseEntity<>("PDF metadata not found", HttpStatus.NOT_FOUND);
        }

        // Simulate updating segmentation by calling the segmentation service again
        metadata.setCuts(newCuts);
        return new ResponseEntity<>("Segmentation updated successfully", HttpStatus.OK);
    }

    // PATCH /modify-segmentation/{id}: Partially modify segmentation details (e.g., specific cuts)
    @Operation(summary = "Modify Segmentation", description = "Partially modify segmentation details for the specified PDF.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Segmentation details modified successfully"),
            @ApiResponse(responseCode = "404", description = "PDF metadata not found")
    })
    @PatchMapping("/modify-segmentation/{id}")
    public ResponseEntity<String> modifySegmentation(@PathVariable("id") String pdfId,
                                                     @RequestBody Map<String, Object> updates) {
        PdfMetadata metadata = pdfMetadataStorage.get(pdfId);
        if (metadata == null) {
            return new ResponseEntity<>("PDF metadata not found", HttpStatus.NOT_FOUND);
        }

        // Apply partial updates (for simplicity, only cuts)
        if (updates.containsKey("cuts")) {
            int newCuts = (int) updates.get("cuts");
            metadata.setCuts(newCuts);
        }

        return new ResponseEntity<>("Segmentation details modified successfully", HttpStatus.OK);
    }

    // DELETE /delete-pdf/{id}: Delete the processed PDF and its segments
    @Operation(summary = "Delete PDF", description = "Delete the processed PDF and its associated segments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "PDF and its segments deleted successfully"),
            @ApiResponse(responseCode = "404", description = "PDF metadata not found")
    })
    @DeleteMapping("/delete-pdf/{id}")
    public ResponseEntity<Void> deletePdf(@PathVariable("id") String pdfId) {
        PdfMetadata metadata = pdfMetadataStorage.remove(pdfId);
        if (metadata == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Simulate deletion (in a real application, delete files from storage)
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Return 204 No Content for successful deletion
    }

    // Utility method to convert MultipartFile to File
    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        file.transferTo(convertedFile);
        return convertedFile;
    }

    // Utility method to create a ZIP archive from a list of files
    private byte[] createZipFromFiles(List<File> files) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (File file : files) {
                // Add each file to the ZIP archive
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                zos.write(fileBytes, 0, fileBytes.length);
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        }
    }

    // Dummy class to represent PDF metadata
    static class PdfMetadata {
        private String pdfId;
        private int segmentCount;
        private int cuts;

        public PdfMetadata(String pdfId, int segmentCount, int cuts) {
            this.pdfId = pdfId;
            this.segmentCount = segmentCount;
            this.cuts = cuts;
        }

        public String getPdfId() {
            return pdfId;
        }

        public void setPdfId(String pdfId) {
            this.pdfId = pdfId;
        }

        public int getSegmentCount() {
            return segmentCount;
        }

        public void setSegmentCount(int segmentCount) {
            this.segmentCount = segmentCount;
        }

        public int getCuts() {
            return cuts;
        }

        public void setCuts(int cuts) {
            this.cuts = cuts;
        }
    }
}
