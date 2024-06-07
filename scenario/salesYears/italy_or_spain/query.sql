SELECT country,
       SUM(Amount) AS total_sales
FROM SALES
where COUNTRY IN ('Italy','Spain')
GROUP BY COUNTRY
ORDER BY total_sales DESC LIMIT 1;

-- CREATE TABLE SALES (Country VARCHAR(20), Amount INT, "Year" INT) AS SELECT * FROM CSVREAD('scenario/salesYears/sales.csv')