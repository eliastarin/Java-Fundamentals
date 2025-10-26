package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private String category;
    private String text;
    private List<Choice> choices = new ArrayList<>();

    public Question() {}
    public Question(String category, String text, List<Choice> choices) {
        this.category = category; this.text = text;
        if (choices != null) this.choices = choices;
    }

    public String getCategory() { return category; }
    public String getText() { return text; }
    public List<Choice> getChoices() { return choices; }

    public void setCategory(String category) { this.category = category; }
    public void setText(String text) { this.text = text; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }
}
