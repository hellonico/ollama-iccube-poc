select Countries, AMOUNT
from
   (SELECT GROUP_CONCAT(DISTINCT COUNTRY ORDER BY COUNTRY SEPARATOR ';') as Countries, AMOUNT, count(*) as C from Sales group by AMOUNT)
where C > 1;