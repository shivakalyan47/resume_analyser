package com.example.demo.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intelligent utility to extract professional skills and keywords from a Job Description.
 */
public class KeywordExtractor {

    // Predefined exhaustive set of common industry keywords, tools, and methodologies.
    private static final Set<String> SKILL_DICTIONARY = new HashSet<>();
    private static final Set<String> STOP_WORDS = new HashSet<>();

    static {
        // Populate common technical and professional skills (normalized to lowercase for matching)
        String[] skills = {
            // Programming Languages
            "java", "python", "javascript", "typescript", "c++", "c#", "ruby", "php", "go", "golang", "swift", "kotlin", "rust", "scala", "r", "perl", "sql", "html", "css", "sass", "bash", "shell",
            // Frameworks & Libraries
            "spring", "spring boot", "django", "flask", "express", "react", "react.js", "reactjs", "angular", "vue", "vuejs", "next.js", "nextjs", "node", "nodejs", "node.js", "jquery", "bootstrap", "tailwind", "hibernate", "jpa", "laravel", "rails", "asp.net", "net core", "tensorflow", "pytorch", "keras", "pandas", "numpy", "scikit-learn",
            // Databases & Storage
            "mysql", "postgresql", "postgres", "mongodb", "redis", "oracle", "sql server", "sqlite", "mariadb", "cassandra", "dynamodb", "neo4j", "elasticsearch", "firebase",
            // DevOps, Cloud & Tools
            "aws", "amazon web services", "azure", "gcp", "google cloud", "docker", "kubernetes", "k8s", "jenkins", "git", "github", "gitlab", "bitbucket", "maven", "gradle", "terraform", "ansible", "ci/cd", "cicd", "linux", "unix", "nginx", "apache", "prometheus", "grafana", "jira", "confluence",
            // Concepts & Architectures
            "microservices", "rest api", "restful", "graphql", "soap", "mvc", "oop", "object-oriented", "agile", "scrum", "kanban", "tdd", "bdd", "devops", "cloud computing", "system design", "data structures", "algorithms",
            // Data Science & AI
            "machine learning", "deep learning", "artificial intelligence", "ai", "nlp", "computer vision", "data analysis", "data science", "big data", "hadoop", "spark", "tableau", "power bi",
            // Soft Skills & Business
            "project management", "leadership", "communication", "teamwork", "problem solving", "collaboration", "analytical", "critical thinking", "agile methodology", "product development", "customer service", "time management", "organization", "strategy", "marketing", "sales", "business analysis"
        };

        for (String skill : skills) {
            SKILL_DICTIONARY.add(skill.toLowerCase());
        }

        // Standard English stop words to filter out
        String[] stopWords = {
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "arent", "as", "at",
            "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "cant", "cannot", "could",
            "did", "didnt", "do", "does", "doesnt", "doing", "dont", "down", "during", "each", "few", "for", "from", "further",
            "had", "hadnt", "has", "hasnt", "have", "havent", "having", "he", "hed", "hell", "hes", "her", "here", "heres",
            "hers", "herself", "him", "himself", "his", "how", "hows", "i", "id", "ill", "im", "ive", "if", "in", "into",
            "is", "isnt", "it", "its", "itself", "lets", "me", "more", "most", "mustnt", "my", "myself", "no", "nor", "not",
            "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own",
            "same", "shant", "she", "shed", "shell", "shes", "should", "shouldnt", "so", "some", "such", "than", "that",
            "thats", "the", "their", "theirs", "them", "themselves", "then", "there", "theres", "these", "they", "theyd",
            "theyll", "theyre", "theyve", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was",
            "wasnt", "we", "wed", "well", "were", "weve", "werent", "what", "whats", "when", "whens", "where", "wheres",
            "which", "while", "who", "whos", "whom", "why", "whys", "with", "wont", "would", "wouldnt", "you", "youd",
            "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "the", "will", "can", "work", "job",
            "experience", "years", "role", "team", "skills", "knowledge", "ability", "candidate", "requirements", "support",
            "responsibilities", "highly", "preferred", "minimum", "degree", "required", "strong", "using", "working"
        };

        for (String word : stopWords) {
            STOP_WORDS.add(word.toLowerCase());
        }
    }

    /**
     * Extracts a list of relevant skills/keywords from the job description text.
     */
    public static List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> extracted = new LinkedHashSet<>();
        String normalizedText = text.toLowerCase();

        // 1. First, search for exact matches from our SKILL_DICTIONARY (handles multi-word skills like "spring boot")
        for (String skill : SKILL_DICTIONARY) {
            // Use word boundaries to avoid matching sub-words (e.g. "go" matching inside "good")
            // For special characters like c++ or .net, we handle word boundaries carefully
            String escapedSkill = Pattern.quote(skill);
            String regex;
            if (skill.endsWith("+") || skill.startsWith(".") || skill.contains("/") || skill.contains(".")) {
                regex = "(?i)\\b" + escapedSkill + "(?=\\s|\\p{Punct}|$)";
            } else {
                regex = "(?i)\\b" + escapedSkill + "\\b";
            }

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(normalizedText);
            if (matcher.find()) {
                extracted.add(skill);
            }
        }

        // 2. Next, search for capitalized noun-like terms in the original text that might be domain-specific proper nouns
        // E.g. "Kubernetes", "Thymeleaf", or custom tools, avoiding first word of sentences if they are common words
        Pattern properNounPattern = Pattern.compile("\\b([A-Z][a-zA-Z0-9+#.]+)\\b");
        Matcher properNounMatcher = properNounPattern.matcher(text);
        while (properNounMatcher.find()) {
            String word = properNounMatcher.group(1);
            String wordLower = word.toLowerCase();
            
            // Filter out common stop words, numbers, and very short characters
            if (wordLower.length() > 2 
                    && !STOP_WORDS.contains(wordLower) 
                    && !word.matches("\\d+") 
                    && !wordLower.equals("the")
                    && !wordLower.equals("this")
                    && !wordLower.equals("with")) {
                
                // If it looks like a tech skill or specialized noun, extract it
                extracted.add(wordLower);
            }
        }

        // Return sorted list (predefined skills first, then custom ones, up to a reasonable limit of e.g. 20 keywords)
        List<String> result = new ArrayList<>(extracted);
        if (result.size() > 25) {
            return result.subList(0, 25);
        }
        return result;
    }
}
