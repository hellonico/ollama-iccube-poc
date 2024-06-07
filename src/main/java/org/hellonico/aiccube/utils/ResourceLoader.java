package org.hellonico.aiccube.utils;

import java.nio.file.Path;

public class ResourceLoader {
    static final String DEFAULT_PREFIX = "scenario/grades/";
    private String prefix = DEFAULT_PREFIX;

    public ResourceLoader(String prefix) {
        this.prefix = prefix;
    }

    public String getPath(String filename) {
        return prefix + filename;
    }

}
