package com.pdfprocessor.pdf_segmentation.controller;
import com.pdfprocessor.pdf_segmentation.service.PdfSegmentationService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.nio.file.Files;


@SpringBootTest
@AutoConfigureMockMvc
public class PdfControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PdfSegmentationService pdfSegmentationService;

    @Test
    public void testSegmentPdf() throws Exception {
        // Arrange
        File pdfFile = new File("C:\\Users\\Mohit gupta\\OneDrive\\Documents\\testing\\Assignemnt.pdf");
        MockMultipartFile multipartFile = new MockMultipartFile("file", pdfFile.getName(), MediaType.APPLICATION_PDF_VALUE, Files.readAllBytes(pdfFile.toPath()));

        // Act
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/pdf/segment-pdf")
                        .file(multipartFile)
                        .param("cuts", "2"))
                .andExpect(MockMvcResultMatchers.status().isCreated())  // Expecting 201 Created instead of 200 OK
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE));
    }


//    @Test
//    public void testGetPdfMetadata() throws Exception {
//        // Arrange
//        File pdfFile = new File("C:\\Users\\Mohit gupta\\OneDrive\\Documents\\testing\\Assignemnt.pdf");
//        pdfSegmentationService.segmentPdf(pdfFile, 2);
//        String pdfId = pdfFile.getName();
//
//        // Act
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/pdf/pdf-metadata/" + pdfId))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.pdfId").value(pdfId))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.segmentCount").value(2));
//    }

    // Add more tests for updateSegmentation, modifySegmentation, and deletePdf
}