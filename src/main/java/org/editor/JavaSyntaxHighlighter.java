package org.editor;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSyntaxHighlighter {

    private JavaSyntaxHighlighter() {
        // do nothing
    }

    // Java Language Keywords
    private static final List<String> KEYWORDS = List.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while"
    );

    // Common Java Data Structures
    private static final List<String> DATA_STRUCTURES = List.of(
            "List", "ArrayList", "LinkedList", "Set", "HashSet", "TreeSet",
            "Map", "HashMap", "TreeMap", "Queue", "Deque", "Stack",
            "Vector", "Hashtable", "ConcurrentHashMap"
    );

    // Regex Patterns
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String DATASTRUCTURE_PATTERN = "\\b(" + String.join("|", DATA_STRUCTURES) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\\n]*|/\\*(.|\\R)*?\\*/";
    private static final String NUMBER_PATTERN = "\\b\\d+(\\.\\d+)?\\b";

    // Combined Pattern
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<DATASTRUCTURE>" + DATASTRUCTURE_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
    );

    // Main Highlighter Logic
    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = null;
            if (matcher.group("KEYWORD") != null) styleClass = "keyword";
            else if (matcher.group("DATASTRUCTURE") != null) styleClass = "datastructure";
            else if (matcher.group("PAREN") != null) styleClass = "paren";
            else if (matcher.group("BRACE") != null) styleClass = "brace";
            else if (matcher.group("BRACKET") != null) styleClass = "bracket";
            else if (matcher.group("SEMICOLON") != null) styleClass = "semicolon";
            else if (matcher.group("STRING") != null) styleClass = "string";
            else if (matcher.group("COMMENT") != null) styleClass = "comment";
            else if (matcher.group("NUMBER") != null) styleClass = "number";

            int skipped = matcher.start() - lastKwEnd;
            if (skipped > 0) {
                spansBuilder.add(Collections.emptyList(), skipped);
            }

            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
