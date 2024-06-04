package org.hellonico.aiccube;

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaAsyncResultCallback;
import io.github.amithkoujalgi.ollama4j.core.types.OllamaModelType;
import org.hellonico.aiccube.utils.CSVToMarkdown;

public class LlamaCaller {

    public static void main(String[] args) throws InterruptedException {
        LlamaCaller llama = new LlamaCaller("random_grades.csv.gz");
        llama.question("Who is the best student this year ?");

        llama.question("What should Marc study to beat Dominic ?");
    }

    final static String host = "http://localhost:11434/";
    OllamaAPI ollamaAPI = new OllamaAPI(host);
    String grades;

    public LlamaCaller(String gradesFile) {
        grades = CSVToMarkdown.read(gradesFile);
        ollamaAPI.setRequestTimeoutSeconds(3000);
        ollamaAPI.setVerbose(true);
    }

    public void question(String question) throws InterruptedException {
        String prompt = "I have the following tables of grades:\n" + grades + "\n\n" + question + "\n\n";
        OllamaAsyncResultCallback callback = ollamaAPI.generateAsync(OllamaModelType.LLAMA3, prompt);
        System.out.printf("Prompt: %s", prompt);

        // maybe broke on ollama4j version up, this was not needed before.
        callback.getOllamaRequestModel().setStream(true);

        while (!callback.isComplete() || !callback.getStream().isEmpty()) {
            String result = callback.getStream().poll();
            if (result != null) {
                System.out.print(result);
//                System.out.print(result);
                System.out.flush();
            }
            Thread.sleep(100);
        }
    }

}
