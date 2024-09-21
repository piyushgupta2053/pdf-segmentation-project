package com.pdfprocessor.pdf_segmentation.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PdfSegmentationService {

    // Simple in-memory metadata storage for the assignment
    private final Map<String, PdfMetadata> pdfMetadataStorage = new HashMap<>();

    public List<File> segmentPdf(File pdfFile, int cuts) throws IOException {
        // Validate the PDF file
        if (pdfFile == null || !pdfFile.exists() || !pdfFile.isFile()) {
            throw new IOException("Invalid PDF file.");
        }

        // Validate cuts
        if (cuts <= 0) {
            throw new IllegalArgumentException("Number of cuts must be greater than zero.");
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new CustomPDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.getText(document);

            List<Float> yPositions = ((CustomPDFTextStripper) stripper).getYPositions();
            List<Integer> cutPositions = findCutPositions(yPositions, cuts);

            List<File> segmentedFiles = splitPdf(document, cutPositions, pdfFile.getName());

            // Store metadata
            String pdfId = pdfFile.getName();
            PdfMetadata metadata = new PdfMetadata(pdfId, segmentedFiles.size(), cuts);
            pdfMetadataStorage.put(pdfId, metadata);

            return segmentedFiles;
        }
    }

    // Fetch metadata for a PDF
    public PdfMetadata getPdfMetadata(String pdfId) {
        return pdfMetadataStorage.get(pdfId);
    }

    // Update segmentation by changing the number of cuts
    public void updateSegmentation(String pdfId, int newCuts) {
        if (newCuts <= 0) {
            throw new IllegalArgumentException("Number of cuts must be greater than zero.");
        }

        PdfMetadata metadata = pdfMetadataStorage.get(pdfId);
        if (metadata == null) {
            throw new IllegalArgumentException("PDF with given ID does not exist.");
        }
        metadata.setCuts(newCuts);
    }

    // Modify segmentation partially (e.g., adjust cuts)
    public void modifySegmentation(String pdfId, int updatedCuts) {
        if (updatedCuts <= 0) {
            throw new IllegalArgumentException("Number of cuts must be greater than zero.");
        }

        PdfMetadata metadata = pdfMetadataStorage.get(pdfId);
        if (metadata == null) {
            throw new IllegalArgumentException("PDF with given ID does not exist.");
        }
        metadata.setCuts(updatedCuts);
    }

    // Delete the PDF metadata
    public void deletePdf(String pdfId) {
        pdfMetadataStorage.remove(pdfId);
    }

    // Private helpers
    private List<Integer> findCutPositions(List<Float> yPositions, int cuts) {
        List<Float> sortedYPositions = yPositions.stream().distinct().sorted().collect(Collectors.toList());
        List<Float> gaps = new ArrayList<>();
        for (int i = 1; i < sortedYPositions.size(); i++) {
            gaps.add(sortedYPositions.get(i) - sortedYPositions.get(i - 1));
        }
        List<Integer> largestGapIndices = new ArrayList<>();
        for (int i = 0; i < cuts; i++) {
            float maxGap = Collections.max(gaps);
            largestGapIndices.add(gaps.indexOf(maxGap));
            gaps.set(gaps.indexOf(maxGap), 0f); // Mark as processed
        }
        return largestGapIndices.stream().map(index -> Math.round(sortedYPositions.get(index))).sorted().collect(Collectors.toList());
    }

    private List<File> splitPdf(PDDocument document, List<Integer> cutPositions, String originalFileName) throws IOException {
        List<File> segmentedFiles = new ArrayList<>();
        for (int i = 0; i < cutPositions.size(); i++) {
            PDDocument newDocument = new PDDocument();
            newDocument.addPage(document.getPage(i));
            String newFileName = originalFileName.replace(".pdf", "_segment_" + (i + 1) + ".pdf");
            File segmentedFile = new File(newFileName);
            newDocument.save(segmentedFile);
            segmentedFiles.add(segmentedFile);
            newDocument.close();
        }
        return segmentedFiles;
    }

    // Custom PDFTextStripper to extract Y positions
    private static class CustomPDFTextStripper extends PDFTextStripper {
        private final List<Float> yPositions = new ArrayList<>();

        public CustomPDFTextStripper() throws IOException {
            super();
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            for (TextPosition position : textPositions) {
                yPositions.add(position.getYDirAdj());
            }
            super.writeString(text, textPositions);
        }

        public List<Float> getYPositions() {
            return yPositions;
        }
    }

    // Metadata class for assignment purposes
    public static class PdfMetadata {
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

        public int getSegmentCount() {
            return segmentCount;
        }

        public int getCuts() {
            return cuts;
        }

        public void setCuts(int cuts) {
            this.cuts = cuts;
        }
    }
}
