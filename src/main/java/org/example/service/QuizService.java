package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Choice;
import org.example.model.Question;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizService {
    private final ObjectMapper mapper = new ObjectMapper();
    private List<Question> questions = Collections.emptyList();

    public List<Question> getQuestions() { return questions; }
    public void clear() { questions = Collections.emptyList(); }

    /** Load /data/quiz.json from resources (simple format). */
    public boolean loadDefaultQuiz() {
        try (InputStream in = getClass().getResourceAsStream("/data/quiz.json")) {
            if (in == null) return false;
            List<Question> loaded = mapper.readValue(in, new TypeReference<>() {});
            this.questions = loaded != null ? loaded : Collections.emptyList();
            return !this.questions.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            this.questions = Collections.emptyList();
            return false;
        }
    }

    /** Load from filesystem. Supports our simple format OR the teacher's SurveyJS format. */
    public boolean loadFromFile(Path path) {
        File file = new File(path.toString());
        // 1) Try the simple format first
        if (tryLoadSimple(file)) return true;
        // 2) Try converting the teacher's SurveyJS format
        if (tryLoadSurveyJS(file)) return true;

        this.questions = Collections.emptyList();
        return false;
    }

    private boolean tryLoadSimple(File file) {
        try {
            List<Question> loaded = mapper.readValue(file, new TypeReference<>() {});
            if (loaded != null && !loaded.isEmpty()) {
                this.questions = loaded;
                return true;
            }
        } catch (Exception ignore) { /* fall back */ }
        return false;
    }

    /**
     * Convert the SurveyJS-style JSON in the assignment to our internal model:
     * - Assume one question per page (as allowed by the brief).
     * - Supports "radiogroup" and "boolean".
     */
    private boolean tryLoadSurveyJS(File file) {
        try {
            JsonNode root = mapper.readTree(file);
            if (root == null) return false;

            JsonNode pages = root.get("pages");
            if (pages == null || !pages.isArray()) return false;

            List<Question> result = new ArrayList<>();

            for (JsonNode page : pages) {
                JsonNode elements = page.get("elements");
                if (elements == null || !elements.isArray() || elements.size() == 0) continue;

                // one question per page as per assignment
                JsonNode elem = elements.get(0);

                String type = text(elem, "type");      // "radiogroup" | "boolean"
                String title = text(elem, "title");    // question text
                if (title == null || title.isBlank()) continue;

                Question q = new Question();
                q.setCategory(type != null ? type : ""); // optional category
                q.setText(title);

                List<Choice> choices = new ArrayList<>();

                if ("radiogroup".equalsIgnoreCase(type)) {
                    // choices: [ "A", "B", ... ], correctAnswer: "B"
                    String correct = text(elem, "correctAnswer");
                    JsonNode arr = elem.get("choices");
                    if (arr != null && arr.isArray()) {
                        for (JsonNode c : arr) {
                            String txt = c.isTextual() ? c.asText() : c.toString();
                            choices.add(new Choice(txt, txt != null && txt.equals(correct)));
                        }
                    }
                } else if ("boolean".equalsIgnoreCase(type)) {
                    // labelTrue/labelFalse optional; default to "True"/"False"
                    String labelTrue = textOrDefault(elem, "labelTrue", "True");
                    String labelFalse = textOrDefault(elem, "labelFalse", "False");
                    boolean correct = elem.get("correctAnswer") != null && elem.get("correctAnswer").asBoolean(false);
                    choices.add(new Choice(labelTrue,  correct));
                    choices.add(new Choice(labelFalse, !correct));
                } else {
                    // Unknown type -> skip
                    continue;
                }

                q.setChoices(choices);
                result.add(q);
            }

            this.questions = result;
            return !this.questions.isEmpty();
        } catch (Exception e) {
            // keep quiet; caller will show an error dialog
        }
        return false;
    }

    private static String text(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && n.isTextual()) ? n.asText() : null;
    }

    private static String textOrDefault(JsonNode node, String field, String def) {
        String v = text(node, field);
        return (v == null || v.isBlank()) ? def : v;
    }
}
