package com.pdfprocessor.pdf_segmentation.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PdfSegmentationServiceTests {

    @Autowired
    private PdfSegmentationService pdfSegmentationService;

    @Test
    public void testSegmentPdf_ValidPdf() throws IOException {
        // Arrange
        File pdfFile = new File("C:\\Users\\Mohit gupta\\OneDrive\\Documents\\testing\\Assignemnt.pdf");

        // Act
        List<File> segmentedFiles = pdfSegmentationService.segmentPdf(pdfFile, 2);

        // Assert
        assertNotNull(segmentedFiles);
        assertEquals(2, segmentedFiles.size());
        assertTrue(segmentedFiles.stream().allMatch(File::exists));
    }

//    @Test
//    public void testSegmentPdf_InvalidPdf() {
//        // Arrange
//        File invalidPdfFile = new File("C:\\Users\\Mohit gupta\\OneDrive\\Documents\\testing\\Assignemnt.pdf");
//
//        // Act and Assert
//        assertThrows(IOException.class, () -> pdfSegmentationService.segmentPdf(invalidPdfFile, 2));
//    }

    @Test
    public void testSegmentPdf_ZeroCuts() {
        // Arrange
        File pdfFile = new File("C:\\Users\\Mohit gupta\\OneDrive\\Documents\\testing\\Assignemnt.pdf");

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> pdfSegmentationService.segmentPdf(pdfFile, 0));
    }

    @Test
    public void testGetPdfMetadata_ExistingPdf() throws IOException {
        // Arrange
        File pdfFile = new File("C:\\Users\\Mohit gupta\\OneDrive\\Documents\\testing\\Assignemnt.pdf");
        pdfSegmentationService.segmentPdf(pdfFile, 2);
        String pdfId = pdfFile.getName();

        // Act
        PdfSegmentationService.PdfMetadata metadata = pdfSegmentationService.getPdfMetadata(pdfId);

        // Assert
        assertNotNull(metadata);
        assertEquals(pdfId, metadata.getPdfId());
        assertEquals(2, metadata.getSegmentCount());
    }

    @Test
    public void testSegmentPdf_InvalidPdf() throws Exception {
        // Arrange: Create a non-existent PDF file
        File invalidPdfFile = new File("C:\\Users\\Mohit gupta\\OneDrive\\Documents\\testing\\NonExistent.pdf");

        // Act & Assert: Expect IOException to be thrown
        Exception exception = assertThrows(IOException.class, () -> {
            pdfSegmentationService.segmentPdf(invalidPdfFile, 2);
        });

        assertEquals("Invalid PDF file.", exception.getMessage());
    }

    @Test
    public void testUpdateSegmentation_ValidPdf() throws IOException {
        // Arrange
        File pdfFile = new File("C:\\Users\\Mohit gupta\\OneDrive\\Documents\\testing\\Assignemnt.pdf");
        pdfSegmentationService.segmentPdf(pdfFile, 2);
        String pdfId = pdfFile.getName();

        // Act
        pdfSegmentationService.updateSegmentation(pdfId, 3);

        // Assert
        PdfSegmentationService.PdfMetadata metadata = pdfSegmentationService.getPdfMetadata(pdfId);
        assertEquals(3, metadata.getCuts());
    }

    @Test
    public void testUpdateSegmentation_NonexistentPdf() {
        // Arrange
        String nonexistentPdfId = "nonexistent";

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> pdfSegmentationService.updateSegmentation(nonexistentPdfId, 3));
    }

    // Add more tests for modifySegmentation and deletePdf methods
}