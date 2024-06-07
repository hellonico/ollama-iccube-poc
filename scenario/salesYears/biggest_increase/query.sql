SELECT
    s2024.Country,
    (s2024.Amount - s2023.Amount) AS AmountIncrease
FROM
    Sales s2023
        JOIN
    Sales s2024
    ON
        s2023.Country = s2024.Country
WHERE
    s2023."Year" = 2023
  AND s2024."Year" = 2024
ORDER BY AmountIncrease DESC LIMIT 3;