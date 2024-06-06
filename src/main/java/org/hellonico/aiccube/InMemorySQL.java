package org.hellonico.aiccube;

import org.hellonico.aiccube.utils.ResourceLoader;
import org.hellonico.aiccube.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class InMemorySQL {

    static final String jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private ResourceLoader resourceLoader;

    private InMemorySQL() {

    }

    public InMemorySQL(String prefix) {
        this.resourceLoader = new ResourceLoader(prefix);
        try {
            prepare();
        } catch (Exception e) {
            e.printStackTrace();
            // throw new RuntimeException(e);
        }
    }

    public void queryWithFile(String filePath) throws IOException, SQLException {
//        String filePath = resourceLoader.getPath(filename);
        System.out.println("Query with file: " + filePath);
        this.query(Utils.fileToString(filePath));
    }

    public void query(String query) throws SQLException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)
        ) {
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        }
    }

    void updateWithFile(String filename) throws SQLException, IOException {
        String update = Utils.fileToString(resourceLoader.getPath(filename));
        this.updateWithStatement(update);
    }
    void updateWithStatement(String statement) throws SQLException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement();
        ) {
            stmt.executeUpdate(statement);
        }
    }

    void importData(Path file) throws SQLException {
        String baseName = Utils.getBasenameWithoutExtension(file).toUpperCase();
        String statement = String.format("CREATE TABLE %s AS SELECT * FROM CSVREAD('%s')", baseName, file.toString());
        System.out.printf("Create table %s with data from %s\n", baseName, file.toString());
        System.out.println(statement);
        updateWithStatement(statement);
    }
    void importDataFromCSVFiles() throws SQLException, IOException {
        List<Path> csvFiles = resourceLoader.getAllCSVFiles();
        for(Path p : csvFiles) {
            importData(p);
        }
    }

    void prepare() throws SQLException, IOException {
        this.importDataFromCSVFiles();
        this.updateWithFile("prepare.sql");
    }
}
