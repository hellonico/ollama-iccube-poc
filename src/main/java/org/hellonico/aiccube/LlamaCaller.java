package org.hellonico.aiccube;

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaAsyncResultCallback;
import io.github.amithkoujalgi.ollama4j.core.types.OllamaModelType;
import org.hellonico.aiccube.utils.CSVToMarkdown;
import org.hellonico.aiccube.utils.ResourceLoader;
import org.hellonico.aiccube.utils.Utils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "ollama-iccube-poc", mixinStandardHelpOptions = true, version = "1.0",
        description = "Compare query data with a ollama model and with sql queries")
public class LlamaCaller  implements Callable<Integer> {

    private ResourceLoader resourceLoader;
    private InMemorySQL h2;

    final static String host = "http://localhost:11434/";
    OllamaAPI ollamaAPI = new OllamaAPI(host);

    @CommandLine.Option(names = {"-m", "--model"}, description = "The ollama model to use")
    private String model = "llama2";

    @CommandLine.Option(names = {"-d", "--dir"}, description = "The folder to run against")
    private String prefix = "scenario/grades/";

    @CommandLine.Option(names = {"-t", "--timeout"}, description = "Ollama timeout")
    private int timeoutInSeconds = 3000;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
    boolean helpRequested;

    @CommandLine.Option(names = {"-1", "--one"}, description = "Which scenario to run")
    String runOne = "";


    public LlamaCaller() {

    }

    public LlamaCaller(String model, String prefix) {
        this.model = model;
        this.prefix = prefix;
        this.init();
    }

    public LlamaCaller(String prefix) {
        this.model = getModel();
        this.prefix = prefix;
        this.init();
    }

    public void init() {
        ollamaAPI.setRequestTimeoutSeconds(timeoutInSeconds);
        ollamaAPI.setVerbose(true);

        this.resourceLoader = new ResourceLoader(prefix);
        this.h2 = new InMemorySQL(prefix);
    }

    public static String getModel() {
        Map<String, String> env =  System.getenv();
        return env.getOrDefault("MODEL","llama2");
    }

    public void question(String question) throws Exception {
        String data = CSVToMarkdown.read(resourceLoader.getPath("grades.csv"));
        String promptTemplatePath = resourceLoader.getPath("prompt.txt");
        System.out.println("| Prompt: " + promptTemplatePath);
        String prompt_template = Utils.fileToString(promptTemplatePath);
        String prompt = String.format(prompt_template, data, question);

        System.out.printf("| Model: %s\n",model);
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
            String sqlFile = base + "/query.sql";
            System.out.printf("""

> Scenario: %s
> Question: %s
                    """, scenario, question);
            this.test(question, sqlFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void testAll() {
        File[] scenarios = Utils.listDirectories(new File(resourceLoader.getPath("")));
        for(File f: scenarios) {
            this.test(f.getName());
        }
    }


    public static void main(String... args) {
        int exitCode = new CommandLine(new LlamaCaller()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        this.init();
        if("".equalsIgnoreCase(runOne)) {
            this.testAll();
        } else {
            this.test(this.runOne);
        }
        return 0;
    }
}
