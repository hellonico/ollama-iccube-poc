package org.hellonico.aiccube.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ResourceLoader {
    static final String DEFAULT_PREFIX = "scenario/";
    private String prefix = DEFAULT_PREFIX;

    public ResourceLoader(String prefix) {
        prefix(prefix);
    }

    public void prefix(String prefix) {
        // TODO: handle full path
        this.prefix = DEFAULT_PREFIX + prefix;
    }

    public String getPath(String filename) {
        return prefix + filename;
    }

    public List<Path> getAllCSVFiles() throws IOException {
        return Utils.getAllFiles(getPath(""), "csv");
    }

}
