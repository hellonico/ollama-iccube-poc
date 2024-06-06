package org.hellonico.aiccube;

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaAsyncResultCallback;
import io.github.amithkoujalgi.ollama4j.core.types.OllamaModelType;
import org.hellonico.aiccube.utils.CSVToMarkdown;
import org.hellonico.aiccube.utils.ResourceLoader;
import org.hellonico.aiccube.utils.Utils;

import java.io.IOException;
import java.util.Map;

public class LlamaCaller {

    private final ResourceLoader resourceLoader;
    private final InMemorySQL h2;
    private String model = "llama2";

    final static String host = "http://localhost:11434/";
    OllamaAPI ollamaAPI = new OllamaAPI(host);
    // String prefix = "grades/";

    public LlamaCaller(String model, String prefix) {
        ollamaAPI.setRequestTimeoutSeconds(3000);
        ollamaAPI.setVerbose(true);
        this.model = model;
        this.resourceLoader = new ResourceLoader(prefix);
        this.h2 = new InMemorySQL(prefix);
    }

    public void question(String question) throws Exception {
        String data = CSVToMarkdown.read(resourceLoader.getPath("grades.csv"));
        String prompt_template = Utils.fileToString(resourceLoader.getPath("prompt.txt"));
        String prompt = String.format(prompt_template, data, question);
        
        OllamaAsyncResultCallback callback = ollamaAPI.generateAsync(model, prompt);
        if(System.getenv().containsKey("DEBUG_PROMPT")) {
            System.out.printf("Prompt: %s", prompt);
        }

        // maybe broke on ollama4j version up, this was not needed before.
        callback.getOllamaRequestModel().setStream(true);

        while (!callback.isComplete() || !callback.getStream().isEmpty()) {
            String result = callback.getStream().poll();
            if (result != null) {
                System.out.print(result);
                System.out.flush();
            }
            Thread.sleep(100);
        }
        System.out.println();
    }

    public void test(String question, String sqlFile) {
        try {
            this.question(question);
            h2.queryWithFile(sqlFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//    public void test(String question) {
//        String sqlFile = question.toLowerCase().replaceAll(" ", "_").replace("?","")+".sql";
//        this.test(question, sqlFile);
//    }

    public void test(String scenario) {

        try {
            String base = resourceLoader.getPath(scenario);
            String question = Utils.fileToString(base + "/text.md");
            String sqlFile = Utils.fileToString(base + "/query.sql");
            this.test(question, sqlFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) throws Exception {
//        Map<String, String> env =  System.getenv();
//        String model = env.get("MODEL");

        LlamaCaller llama = new LlamaCaller("llama2", "grades/");
//        llama.question("Who is the best student this year ?");

//        llama.question("What should Marc study to beat Dominic ?");
        // llama.question("Write a SQL query to find who is the best student?");
//        llama.question();

        llama.test("best_student");
    }

}
