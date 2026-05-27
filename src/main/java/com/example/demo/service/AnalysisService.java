package com.example.demo.service;

import com.example.demo.model.ResumeAnalysisResult;
import com.example.demo.utils.KeywordExtractor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to execute ATS scoring, keyword matching, section extraction, and formatting analysis.
 */
@Service
public class AnalysisService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"
    );

    // Matches various international and US phone number formats
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b(?:\\+?\\d{1,3}[- .]?)?\\(?\\d{3}\\)?[- .]?\\d{3}[- .]?\\d{4}\\b"
    );

    private static final Pattern LINKEDIN_PATTERN = Pattern.compile(
            "\\b(?:https?:\\/\\/)?(?:www\\.)?linkedin\\.com\\/in\\/[A-Za-z0-9_-]+\\/?\\b"
    );

    private static final Pattern GITHUB_PATTERN = Pattern.compile(
            "\\b(?:https?:\\/\\/)?(?:www\\.)?github\\.com\\/[A-Za-z0-9_-]+\\/?\\b"
    );

    /**
     * Analyzes resume text against an optional job description.
     */
    public ResumeAnalysisResult analyze(String resumeText, String jobDescription) {
        ResumeAnalysisResult result = new ResumeAnalysisResult();
        result.setParsedResumeText(resumeText);

        // 1. Word Count & Formatting
        int wordCount = countWords(resumeText);
        result.setWordCount(wordCount);
        analyzeFormattingAndContact(resumeText, wordCount, result);

        // 2. Section Completeness Analysis
        analyzeSections(resumeText, result);

        // 3. Keyword and Skill Matching
        analyzeKeywords(resumeText, jobDescription, result);

        // 4. Calculate Final ATS Score
        // Weighting: Keywords (50%), Sections (30%), Contact & Formatting (20%)
        double rawScore = (result.getKeywordScore() * 0.50) + 
                           (result.getSectionScore() * 0.30) + 
                           (result.getFormattingScore() * 0.20);
        result.setAtsScore((int) Math.round(rawScore));

        // Generate generalized suggestions based on findings
        compileGeneralSuggestions(result);

        return result;
    }

    /**
     * Counts words in a string.
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    /**
     * Evaluates contact details and formatting metrics.
     */
    private void analyzeFormattingAndContact(String text, int wordCount, ResumeAnalysisResult result) {
        int score = 0;
        String lowercaseText = text.toLowerCase();

        // Email Extraction
        Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            String email = emailMatcher.group();
            result.setExtractedEmail(email);
            score += 25;
        } else {
            result.addSuggestion("contact", "Email address was not detected. Ensure your primary email is clearly visible in the header.");
        }

        // Phone Extraction
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            String phone = phoneMatcher.group();
            result.setExtractedPhone(phone);
            score += 25;
        } else {
            result.addSuggestion("contact", "Phone number was not detected. Include a professional phone number so recruiters can easily contact you.");
        }

        // LinkedIn & GitHub links
        Matcher linkedinMatcher = LINKEDIN_PATTERN.matcher(lowercaseText);
        Matcher githubMatcher = GITHUB_PATTERN.matcher(lowercaseText);

        boolean hasLinkedIn = linkedinMatcher.find();
        if (hasLinkedIn) {
            result.getExtractedLinks().put("LinkedIn", formatUrl(linkedinMatcher.group()));
        }
        boolean hasGitHub = githubMatcher.find();
        if (hasGitHub) {
            result.getExtractedLinks().put("GitHub", formatUrl(githubMatcher.group()));
        }

        if (hasLinkedIn || hasGitHub) {
            score += 25;
        } else {
            result.addSuggestion("contact", "LinkedIn and/or GitHub profiles were not found. Adding hyperlinks to your professional networks is highly recommended.");
        }

        // Word count criteria
        if (wordCount >= 350 && wordCount <= 950) {
            score += 25;
        } else if (wordCount > 150 && wordCount < 350) {
            score += 15;
            result.addSuggestion("formatting", "Your resume length is brief (" + wordCount + " words). Expand on your accomplishments, projects, and skills to provide depth.");
        } else if (wordCount > 950 && wordCount <= 1400) {
            score += 15;
            result.addSuggestion("formatting", "Your resume is slightly long (" + wordCount + " words). Try to consolidate descriptions to fit within 1-2 pages.");
        } else if (wordCount <= 150) {
            score += 5;
            result.addSuggestion("formatting", "Extremely short content (" + wordCount + " words). A professional resume should contain detailed statements about your education and experience.");
        } else {
            score += 5;
            result.addSuggestion("formatting", "Your resume is excessively long (" + wordCount + " words). Streamline bullet points and keep only relevant career highlights.");
        }

        result.setFormattingScore(score);
    }

    /**
     * Formats extracted URLs to ensure they contain a scheme.
     */
    private String formatUrl(String url) {
        if (url == null) {
            return null;
        }
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    /**
     * Checks for the presence of standard resume sections.
     */
    private void analyzeSections(String text, ResumeAnalysisResult result) {
        String lowercaseText = text.toLowerCase();

        Map<String, String[]> sectionsMap = new LinkedHashMap<>();
        sectionsMap.put("Experience", new String[]{"experience", "work history", "employment", "professional history", "positions held", "career history"});
        sectionsMap.put("Education", new String[]{"education", "academic", "qualification", "degree", "university", "college", "schooling"});
        sectionsMap.put("Skills", new String[]{"skills", "technologies", "technical expertise", "core competencies", "proficiencies", "expertise"});
        sectionsMap.put("Projects", new String[]{"projects", "academic projects", "personal projects", "key projects", "work accomplishments"});
        sectionsMap.put("Certifications", new String[]{"certifications", "certificates", "licenses", "courses", "credentials", "awards"});

        int score = 0;
        int valuePerSection = 100 / sectionsMap.size();

        for (Map.Entry<String, String[]> entry : sectionsMap.entrySet()) {
            String sectionName = entry.getKey();
            String[] keywords = entry.getValue();
            boolean found = false;

            // Search for sections with regex to avoid partial subword matches
            for (String kw : keywords) {
                Pattern p = Pattern.compile("\\b" + Pattern.quote(kw) + "\\b");
                if (p.matcher(lowercaseText).find()) {
                    found = true;
                    break;
                }
            }

            if (found) {
                result.getFoundSections().add(sectionName);
                score += valuePerSection;
            } else {
                result.getMissingSections().add(sectionName);
                result.addSuggestion("sections", "Missing '" + sectionName + "' section header. Explicitly categorizing your resume helps ATS systems parse your profile correctly.");
            }
        }

        result.setSectionScore(score);
    }

    /**
     * Scans for keywords in the resume.
     */
    private void analyzeKeywords(String resumeText, String jobDescription, ResumeAnalysisResult result) {
        String lowercaseResume = resumeText.toLowerCase();

        // Extract keywords from the job description
        List<String> jdKeywords = KeywordExtractor.extractKeywords(jobDescription);

        if (jdKeywords.isEmpty()) {
            // Fallback: If no job description is provided, search the resume for standard industry skills
            // to populate 'matched' keywords and give a reasonable score
            List<String> standardKeywords = KeywordExtractor.extractKeywords(resumeText);
            if (standardKeywords.isEmpty()) {
                result.setKeywordScore(50); // Neutral baseline
            } else {
                // Display found skills as matched keywords
                result.getMatchedKeywords().addAll(standardKeywords);
                result.setKeywordScore(75); // Standard score for having good skill density
            }
            return;
        }

        // Match job description keywords in the resume
        for (String keyword : jdKeywords) {
            String escapedKw = Pattern.quote(keyword);
            String regex;
            
            // Check for punctuation tags like c++, .net, rest api
            if (keyword.endsWith("+") || keyword.startsWith(".") || keyword.contains("/") || keyword.contains(".")) {
                regex = "(?i)\\b" + escapedKw + "(?=\\s|\\p{Punct}|$)";
            } else {
                regex = "(?i)\\b" + escapedKw + "\\b";
            }

            Pattern pattern = Pattern.compile(regex);
            if (pattern.matcher(lowercaseResume).find()) {
                result.getMatchedKeywords().add(keyword);
            } else {
                result.getMissingKeywords().add(keyword);
            }
        }

        // Calculate score percentage
        if (!jdKeywords.isEmpty()) {
            int score = (int) Math.round(((double) result.getMatchedKeywords().size() / jdKeywords.size()) * 100);
            result.setKeywordScore(score);
        } else {
            result.setKeywordScore(100);
        }
    }

    /**
     * Compiles overall general recommendations based on the scored sub-metrics.
     */
    private void compileGeneralSuggestions(ResumeAnalysisResult result) {
        List<String> keywordSugs = result.getSuggestions().get("keywords");

        if (result.getKeywordScore() < 50) {
            if (!result.getMissingKeywords().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Your skills match is low (")
                  .append(result.getKeywordScore())
                  .append("%). Target these critical keywords from the job description: ");
                
                int limit = Math.min(5, result.getMissingKeywords().size());
                for (int i = 0; i < limit; i++) {
                    sb.append("'").append(result.getMissingKeywords().get(i)).append("'");
                    if (i < limit - 1) sb.append(", ");
                }
                sb.append(". Integrate them organically in your 'Experience' or 'Skills' sections.");
                keywordSugs.add(sb.toString());
            } else {
                keywordSugs.add("Increase the density of professional skills in your resume to better match your target industry roles.");
            }
        } else if (result.getKeywordScore() >= 50 && result.getKeywordScore() < 80) {
            if (!result.getMissingKeywords().isEmpty()) {
                keywordSugs.add("Good keyword coverage! You can further boost your score by incorporating these remaining key skills: " 
                        + String.join(", ", result.getMissingKeywords().subList(0, Math.min(3, result.getMissingKeywords().size()))));
            }
        } else {
            keywordSugs.add("Excellent! Your resume exhibits a very strong alignment with the keywords required for this position.");
        }

        // Default placeholders if lists are empty
        if (result.getSuggestions().get("sections").isEmpty()) {
            result.getSuggestions().get("sections").add("Great job! Your resume contains all standard essential section divisions.");
        }
        if (result.getSuggestions().get("contact").isEmpty()) {
            result.getSuggestions().get("contact").add("All crucial contact details (Email, Phone, and Professional URLs) were verified successfully.");
        }
        if (result.getSuggestions().get("formatting").isEmpty()) {
            result.getSuggestions().get("formatting").add("Your resume layout, word count, and text composition comply perfectly with standard ATS guidelines.");
        }
    }
}
