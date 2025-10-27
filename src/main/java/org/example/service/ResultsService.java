package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;

public class ResultsService {

    public static class ResultsFile {
        public String quizId;
        public String name; // quiz title
        public List<ResultEntry> results = new ArrayList<>();
    }

    public static class ResultEntry {
        public String playerName;
        public int totalQuestions;
        public int correctQuestions;
        public String date;


        public String getPlayerName()      { return playerName; }
        public int    getTotalQuestions()  { return totalQuestions; }
        public int    getCorrectQuestions(){ return correctQuestions; }
        public String getDate()            { return date; } // if your field is named dateIso, rename accordingly


        public ResultEntry() {}
        public ResultEntry(String playerName, int total, int correct, String dateIso) {
            this.playerName = playerName;
            this.totalQuestions = total;
            this.correctQuestions = correct;
            this.date = dateIso;
        }

        @Override public String toString() {
            return playerName + " • " + correctQuestions + "/" + totalQuestions + " • " + date;
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path baseDir = Paths.get("results");

    private Path ensureDir() throws IOException {
        if (!Files.exists(baseDir)) Files.createDirectories(baseDir);
        return baseDir;
    }

    // export the CSV
    public void exportCsv(String quizId, String quizName, File file) throws IOException {
        // load all results for this quiz
        List<ResultEntry> all = loadAll(quizId, quizName);

        StringBuilder sb = new StringBuilder();
        sb.append("quizId;quizName;playerName;totalQuestions;correctQuestions;date\n");
        for (ResultEntry e : all) {
            sb.append(safe(quizId)).append(';')
                    .append(safe(quizName)).append(';')
                    .append(safe(e.getPlayerName())).append(';')
                    .append(e.getTotalQuestions()).append(';')
                    .append(e.getCorrectQuestions()).append(';')
                    .append(safe(e.getDate()))  // or getDateIso() if that’s your getter
                    .append('\n');
        }
        Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }

    private static String safe(String s) {
        if (s == null) return "";
        // very light escaping: replace semicolons/newlines
        return s.replace(";", ",").replace("\r", " ").replace("\n", " ");
    }


    public List<ResultEntry> appendAndReadAll(String quizId, String quizName, ResultEntry newEntry) throws IOException {
        ensureDir();
        Path file = baseDir.resolve(quizId + "-results.json");

        ResultsFile data;
        if (Files.exists(file) && Files.size(file) > 0) {
            data = mapper.readValue(Files.readAllBytes(file), ResultsFile.class);
            if (data.results == null) data.results = new ArrayList<>();
        } else {
            data = new ResultsFile();
            data.results = new ArrayList<>();
        }

        data.quizId = quizId;
        data.name = quizName;

        if (newEntry != null) data.results.add(newEntry);

        byte[] out = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
        Files.write(file, out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return data.results;
    }

    public static ResultEntry makeEntry(String playerName, int total, int correct) {
        return new ResultEntry(playerName, total, correct, Instant.now().toString());
    }

    public java.util.List<ResultEntry> loadAll(String quizId, String quizName) throws java.io.IOException {
        return appendAndReadAll(quizId, quizName, null);
    }

}
