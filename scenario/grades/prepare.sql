CREATE TABLE GradeMapping (
                              Grade CHAR(1) PRIMARY KEY,
                              NumericValue INT
);

INSERT INTO GradeMapping (Grade, NumericValue) VALUES ('A', 5),
                                                      ('B', 4),
                                                      ('C', 3),
                                                      ('D', 2),
                                                      ('F', 1);