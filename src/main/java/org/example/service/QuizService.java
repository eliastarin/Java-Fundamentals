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

    // Current quiz
    private String lastQuizId = "quiz";
    private String lastQuizName = "Quiz";

    public List<Question> getQuestions() { return questions; }

    public void clear() { questions = Collections.emptyList(); }

    public String getLastQuizId() { return lastQuizId; }

    public String getLastQuizName() { return lastQuizName; }

    public boolean loadDefaultQuiz() {
        try (InputStream in = getClass().getResourceAsStream("/data/quiz.json")) {
            if (in == null) return false;
            List<Question> loaded = mapper.readValue(in, new TypeReference<List<Question>>() {});
            this.questions = loaded != null ? loaded : Collections.emptyList();
            this.lastQuizId = "default";
            this.lastQuizName = "Default Quiz";
            return !this.questions.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            this.questions = Collections.emptyList();
            return false;
        }
    }

    public boolean loadFromFile(Path path) {
        File file = new File(path.toString());

        String base = file.getName();
        int dot = base.lastIndexOf('.');
        String fallback = (dot > 0 ? base.substring(0, dot) : base)
                .replaceAll("\\W+", "-").toLowerCase();
        this.lastQuizId = fallback;
        this.lastQuizName = fallback;

        if (tryLoadSimple(file)) return true;
        if (tryLoadSurveyJS(file)) return true;

        this.questions = Collections.emptyList();
        return false;
    }

    private boolean tryLoadSimple(File file) {
        try {
            List<Question> loaded = mapper.readValue(file, new TypeReference<List<Question>>() {});
            if (loaded != null && !loaded.isEmpty()) {
                this.questions = loaded;
                return true;
            }
        } catch (Exception ignore) { }
        return false;
    }

    private boolean tryLoadSurveyJS(File file) {
        try {
            JsonNode root = mapper.readTree(file);
            if (root == null) return false;

            JsonNode quizIdNode = root.get("quizId");
            if (quizIdNode != null && quizIdNode.isTextual() && !quizIdNode.asText().isBlank()) {
                this.lastQuizId = quizIdNode.asText().trim();
            }

            JsonNode titleNode = root.get("title");
            if (titleNode != null && titleNode.isTextual() && !titleNode.asText().isBlank()) {
                this.lastQuizName = titleNode.asText().trim();
            } else {
                this.lastQuizName = this.lastQuizId;
            }

            JsonNode pages = root.get("pages");
            if (pages == null || !pages.isArray()) return false;

            List<Question> result = new ArrayList<>();

            for (JsonNode page : pages) {
                JsonNode elements = page.get("elements");
                if (elements == null || !elements.isArray() || elements.size() == 0) continue;

                JsonNode elem = elements.get(0);

                String type = text(elem, "type");
                String title = text(elem, "title");
                if (title == null || title.isBlank()) continue;

                Question q = new Question();
                q.setCategory(type != null ? type : "");
                q.setText(title);

                // time limit
                Integer timeLimit = (page.get("timeLimit") != null && page.get("timeLimit").isInt())
                        ? page.get("timeLimit").asInt() : null;
                q.setTimeLimitSec(timeLimit);

                List<Choice> choices = new ArrayList<>();

                if ("radiogroup".equalsIgnoreCase(type)) {
                    // choices
                    String correct = text(elem, "correctAnswer");
                    JsonNode arr = elem.get("choices");
                    if (arr != null && arr.isArray()) {
                        for (JsonNode c : arr) {
                            String txt = c.isTextual() ? c.asText() : c.toString();
                            choices.add(new Choice(txt, txt != null && txt.equals(correct)));
                        }
                    }
                } else if ("boolean".equalsIgnoreCase(type)) {
                    // true/false
                    String labelTrue  = textOrDefault(elem, "labelTrue",  "True");
                    String labelFalse = textOrDefault(elem, "labelFalse", "False");
                    boolean correct = elem.get("correctAnswer") != null && elem.get("correctAnswer").asBoolean(false);
                    choices.add(new Choice(labelTrue,  correct));
                    choices.add(new Choice(labelFalse, !correct));
                } else {
                    // unsupported type
                    continue;
                }

                q.setChoices(choices);
                result.add(q);
            }

            this.questions = result;
            return !this.questions.isEmpty();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && n.isTextual()) ? n.asText() : null;
    }

    private static String textOrDefault(JsonNode node, String field, String def) {
        String v = text(node, field);
        return (v == null || v.isBlank()) ? def : v;
    }
    public String renderCompletedHtml(int totalQuestions, int correctQuestions) {

        if(correctQuestions == totalQuestions)
        {
            return "<h4>Excellent! You answered all questions correctly!</h4>";
        }
        else
        {
            return "<h4>You got <b>" + correctQuestions + "</b> out of <b>" + totalQuestions + "</b> correct.</h4>";
        }
    }

}
