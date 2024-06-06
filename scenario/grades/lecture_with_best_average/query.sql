SELECT TOP 1 LECTURE, AVG(NUMERICVALUE) as average
FROM GRADES g
         JOIN GRADEMAPPING m ON g.GRADE = m.GRADE
GROUP BY LECTURE ORDER BY average desc;