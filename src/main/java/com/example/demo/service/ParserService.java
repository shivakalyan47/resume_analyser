package com.example.demo.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Service to parse text content from uploaded files (PDF, TXT).
 */
@Service
public class ParserService {

    /**
     * Parse the uploaded file and return its textual content.
     */
    public String parseFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType != null && contentType.equals("application/pdf") 
                || (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf"))) {
            return parsePdf(file.getBytes());
        } else if (contentType != null && contentType.startsWith("text/") 
                || (originalFilename != null && originalFilename.toLowerCase().endsWith(".txt"))) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } else {
            // Fallback: try to read it as plain text
            try {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported file type. Please upload a PDF or TXT file.");
            }
        }
    }

    /**
     * Extract text from PDF bytes using Apache PDFBox.
     */
    private String parsePdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            // Optional: order by position to maintain columns/reading order
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            if (text == null || text.trim().isEmpty()) {
                throw new IOException("The PDF document does not contain any readable text. It might be scanned. Please upload a text-based PDF.");
            }
            return text;
        } catch (IOException e) {
            throw new IOException("Error reading PDF content: " + e.getMessage(), e);
        }
    }
}
