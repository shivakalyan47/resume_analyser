package com.example.demo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data model representing the complete analysis output of a resume.
 */
public class ResumeAnalysisResult {
    private int atsScore;
    private int keywordScore;
    private int sectionScore;
    private int formattingScore;

    private List<String> matchedKeywords = new ArrayList<>();
    private List<String> missingKeywords = new ArrayList<>();
    
    private List<String> foundSections = new ArrayList<>();
    private List<String> missingSections = new ArrayList<>();

    private String extractedEmail;
    private String extractedPhone;
    private Map<String, String> extractedLinks = new HashMap<>();

    private Map<String, List<String>> suggestions = new HashMap<>();
    private int wordCount;
    private String parsedResumeText;

    public ResumeAnalysisResult() {
        suggestions.put("keywords", new ArrayList<>());
        suggestions.put("sections", new ArrayList<>());
        suggestions.put("contact", new ArrayList<>());
        suggestions.put("formatting", new ArrayList<>());
    }

    // Getters and Setters

    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public int getKeywordScore() {
        return keywordScore;
    }

    public void setKeywordScore(int keywordScore) {
        this.keywordScore = keywordScore;
    }

    public int getSectionScore() {
        return sectionScore;
    }

    public void setSectionScore(int sectionScore) {
        this.sectionScore = sectionScore;
    }

    public int getFormattingScore() {
        return formattingScore;
    }

    public void setFormattingScore(int formattingScore) {
        this.formattingScore = formattingScore;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(List<String> matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public List<String> getFoundSections() {
        return foundSections;
    }

    public void setFoundSections(List<String> foundSections) {
        this.foundSections = foundSections;
    }

    public List<String> getMissingSections() {
        return missingSections;
    }

    public void setMissingSections(List<String> missingSections) {
        this.missingSections = missingSections;
    }

    public String getExtractedEmail() {
        return extractedEmail;
    }

    public void setExtractedEmail(String extractedEmail) {
        this.extractedEmail = extractedEmail;
    }

    public String getExtractedPhone() {
        return extractedPhone;
    }

    public void setExtractedPhone(String extractedPhone) {
        this.extractedPhone = extractedPhone;
    }

    public Map<String, String> getExtractedLinks() {
        return extractedLinks;
    }

    public void setExtractedLinks(Map<String, String> extractedLinks) {
        this.extractedLinks = extractedLinks;
    }

    public Map<String, List<String>> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(Map<String, List<String>> suggestions) {
        this.suggestions = suggestions;
    }

    public void addSuggestion(String category, String suggestion) {
        this.suggestions.computeIfAbsent(category, k -> new ArrayList<>()).add(suggestion);
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getParsedResumeText() {
        return parsedResumeText;
    }

    public void setParsedResumeText(String parsedResumeText) {
        this.parsedResumeText = parsedResumeText;
    }
}
