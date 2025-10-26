package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private String title;
    private List<Question> questions = new ArrayList<>();

    public Quiz() {}
    public Quiz(String title, List<Question> questions) {
        this.title = title;
        if (questions != null) this.questions = questions;
    }

    public String getTitle() { return title; }
    public List<Question> getQuestions() { return questions; }

    public void setTitle(String title) { this.title = title; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
