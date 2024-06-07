package org.hellonico.aiccube;

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaAsyncResultCallback;
import org.hellonico.aiccube.utils.CSVToMarkdown;
import org.hellonico.aiccube.utils.ResourceLoader;
import org.hellonico.aiccube.utils.Utils;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "ollama-iccube-poc", mixinStandardHelpOptions = true, version = "1.0",
        description = "Compare query data with a ollama model and with sql queries")
public class LlamaCaller implements Callable<Integer> {

    private ResourceLoader resourceLoader;
    private InMemorySQL h2;

    @CommandLine.Option(names = {"--ollama"}, defaultValue = "http://localhost:11434/", description = "The ollama url to use")
    private String host;

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

    @CommandLine.Option(names = {"-q", "--question"}, description = "Free question on the data.")
    String question = "";

    @CommandLine.Option(names = {"--prompt"}, description = "Prompt template override")
    String promptOverride = "";


    @CommandLine.Option(names = {"--repl"}, description = "Run in REPL mode.")
    boolean replMode;

    public enum Item {
        MDX, SQL;
    }

    @CommandLine.Option(names = {"-i", "--items"}, split = ",", description = "One or more of MDX, SQL, PANDAS")
    private List<Item> items = new ArrayList<>();

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

    OllamaAPI ollamaAPI;

    public void init() {

        ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(timeoutInSeconds);
        ollamaAPI.setVerbose(true);

        this.resourceLoader = new ResourceLoader(prefix);
        this.h2 = new InMemorySQL(prefix);
    }

    public static String getModel() {
        Map<String, String> env = System.getenv();
        return env.getOrDefault("MODEL", "llama2");
    }

    public void question(String question) throws Exception {
        String firstCsv = Utils.getAllFiles(resourceLoader.getPath(""), "csv").get(0).toString();
        String data = CSVToMarkdown.read(firstCsv);

        String prompt_template = "";

        if(!promptOverride.equalsIgnoreCase("")) {
            prompt_template = promptOverride;
        } else {
            String promptTemplatePath = resourceLoader.getPath("prompt.txt");
            System.out.println("| Prompt: " + promptTemplatePath);
            prompt_template = Utils.fileToString(promptTemplatePath);
        }

        String prompt = String.format(prompt_template, data, question);
        System.out.printf("| Model: %s\n", model);
        callLLM(prompt);

    }

    private void generateQueries() throws InterruptedException {
        for (Item i : items) {
            String extra = String.format("If you were to obtain the above result in %s, how would you write the query? ", i.name());
            System.out.printf("| %s\n", extra);
            this.callLLM(extra);
        }
    }

    private void callLLM(String prompt) throws InterruptedException {
        OllamaAsyncResultCallback callback = ollamaAPI.generateAsync(model, prompt);
        if (System.getenv().containsKey("DEBUG_PROMPT")) {
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
            this.generateQueries();
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
        for (File f : scenarios) {
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

        if(replMode) {
            LineReader lineReader = LineReaderBuilder.builder()
                    .history(new DefaultHistory())
//                    .terminal(new Term)
//                    .terminal(terminal)
//                    .completer(new MyCompleter())
//                    .highlighter(new MyHighlighter())
//                    .parser(new MyParser())
                    .build();
            lineReader.variable(
                    LineReader.HISTORY_FILE,
                    ".history.txt"
            );

            String line = "";
            while(true) {
                System.out.print("> ");
                try {
                    line = lineReader.readLine();
//                    if(line.equalsIgnoreCase(":q")) {
//                        break;
//                    }
                    this.question(line);
//                    this.generateQueries();
                } catch(Exception e) {
                    break;
                }
            }
            lineReader.getHistory().save();
            return 0;
        }

        if (!question.equalsIgnoreCase("")) {
            this.question(question);
            this.generateQueries();
            return 0;
        }

        if ("".equalsIgnoreCase(runOne)) {
            this.testAll();
            return 0;
        } else {
            this.test(this.runOne);
            return 0;
        }

    }
}
