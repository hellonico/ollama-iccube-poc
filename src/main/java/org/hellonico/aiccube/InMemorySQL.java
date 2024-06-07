package org.hellonico.aiccube;

import org.hellonico.aiccube.utils.ResourceLoader;
import org.hellonico.aiccube.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class InMemorySQL {

    //    static final String jdbcUrl = "jdbc:h2:file:./test;DB_CLOSE_DELAY=-1";
    static final String jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private ResourceLoader resourceLoader;

    private InMemorySQL() {

    }

    public InMemorySQL(String prefix) {
        this.resourceLoader = new ResourceLoader(prefix);
        try {
            importDataFromCSVFiles();
            importExtraSQL();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void queryWithFile(String filePath) throws IOException, SQLException {
        if(new File(filePath).exists()) {
            System.out.println("| Query with file: " + filePath);
            this.query(Utils.fileToString(filePath));
        } else {
            System.out.println("| Skipping SQL Validation");
        }
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
        System.out.println("| Update with file: " + filename);
        this.updateWithStatement(Utils.fileToString(filename));
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
        String statement = String.format(getCreateStatement(file), baseName, file.toString());
        System.out.printf("| SQL Data using: %s\n", statement);
        updateWithStatement(statement);
    }

    String getCreateStatement(Path file) {
        File f = new File(resourceLoader.getPath(file.toString() + ".tpl"));
        if (f.exists()) {
            try {
                return Utils.fileToString(f.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "CREATE TABLE %s (Country VARCHAR(20), Amount INT) AS SELECT * FROM CSVREAD('%s')";
    }

    /**
     * All csv files in the root folders are used as data
     */
    void importDataFromCSVFiles() throws SQLException, IOException {
        List<Path> csvFiles = Utils.getAllFiles(resourceLoader.getPath(""), "csv");
        for (Path p : csvFiles) {
            importData(p);
        }
    }

    private void importExtraSQL() throws IOException, SQLException {
        List<Path> extraSQL = Utils.getAllFiles(resourceLoader.getPath(""), "sql");
        for (Path p : extraSQL) {
            this.updateWithFile(p.toString());
        }
    }
}
