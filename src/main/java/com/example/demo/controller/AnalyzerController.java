package com.example.demo.controller;

import com.example.demo.model.ResumeAnalysisResult;
import com.example.demo.service.AnalysisService;
import com.example.demo.service.ParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to handle web UI requests and REST API requests for resume analysis.
 */
@Controller
public class AnalyzerController {

    private final ParserService parserService;
    private final AnalysisService analysisService;

    @Autowired
    public AnalyzerController(ParserService parserService, AnalysisService analysisService) {
        this.parserService = parserService;
        this.analysisService = analysisService;
    }

    /**
     * Renders the landing page.
     */
    @GetMapping("/")
    public String index(Model model) {
        // Pre-fill model attributes to avoid Thymeleaf errors
        model.addAttribute("analyzed", false);
        model.addAttribute("result", new ResumeAnalysisResult());
        model.addAttribute("isPasteResume", false); // Default to file upload tab
        return "index";
    }

    /**
     * Handles the form submission for resume analysis.
     */
    @PostMapping("/analyze")
    public String analyzeResume(
            @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestParam(value = "resumeText", required = false) String resumeText,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            Model model) {
        
        String extractedResumeText = "";
        boolean isPaste = (resumeFile == null || resumeFile.isEmpty());

        try {
            // Check if file is uploaded
            if (resumeFile != null && !resumeFile.isEmpty()) {
                extractedResumeText = parserService.parseFile(resumeFile);
            } else if (resumeText != null && !resumeText.trim().isEmpty()) {
                extractedResumeText = resumeText;
            } else {
                model.addAttribute("error", "Please provide a resume by either uploading a file or pasting the text.");
                model.addAttribute("analyzed", false);
                model.addAttribute("result", new ResumeAnalysisResult());
                model.addAttribute("isPasteResume", isPaste);
                return "index";
            }

            // Run analysis
            ResumeAnalysisResult result = analysisService.analyze(extractedResumeText, jobDescription);
            
            // Add attributes to view model
            model.addAttribute("result", result);
            model.addAttribute("analyzed", true);
            model.addAttribute("jobDescription", jobDescription);
            model.addAttribute("isPasteResume", isPaste);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("analyzed", false);
            model.addAttribute("result", new ResumeAnalysisResult());
            model.addAttribute("isPasteResume", isPaste);
        } catch (IOException e) {
            model.addAttribute("error", "Failed to parse the uploaded file. " + e.getMessage());
            model.addAttribute("analyzed", false);
            model.addAttribute("result", new ResumeAnalysisResult());
            model.addAttribute("isPasteResume", isPaste);
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred during analysis: " + e.getMessage());
            model.addAttribute("analyzed", false);
            model.addAttribute("result", new ResumeAnalysisResult());
            model.addAttribute("isPasteResume", isPaste);
        }

        return "index";
    }

    /**
     * REST Endpoint for programmatic resume analysis.
     */
    @PostMapping("/api/analyze")
    @ResponseBody
    public ResponseEntity<?> analyzeApi(
            @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestParam(value = "resumeText", required = false) String resumeText,
            @RequestParam(value = "jobDescription", required = false) String jobDescription) {

        String extractedResumeText = "";
        Map<String, Object> errorResponse = new HashMap<>();

        try {
            if (resumeFile != null && !resumeFile.isEmpty()) {
                extractedResumeText = parserService.parseFile(resumeFile);
            } else if (resumeText != null && !resumeText.trim().isEmpty()) {
                extractedResumeText = resumeText;
            } else {
                errorResponse.put("error", "Please provide resume content via file upload (resumeFile) or raw text (resumeText).");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            ResumeAnalysisResult result = analysisService.analyze(extractedResumeText, jobDescription);
            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            errorResponse.put("error", "File read error: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            errorResponse.put("error", "Analysis failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
