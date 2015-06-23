<configure-default.sql>

-- DML: purge and regenerate random data first
DELETE FROM @dmltable
INSERT INTO @dmltable VALUES (@insertvals)

-- TEMP, for debugging, just so I can quickly see what data was generated:
select * FROM @fromtables ORDER BY @idcol

-- Causes NPE in HsqlDb (ENG-8292):
----select ID, (SELECT SUM(ID) FROM _table WHERE A2.ID = ID) FROM _table AS A2 GROUP BY ID HAVING SUM(ID) = 12

-- May cause NPE in HsqlDb (ENG-8292):
--select ID, (SELECT SUM(ID) FROM _table WHERE A2.ID = ID) FROM _table AS A2 GROUP BY ID HAVING SUM(ID) = 12

-- May cause NPE in HsqlDb (ENG-8381):
--select PKEY FROM A UNION ALL SELECT I FROM B UNION ALL SELECT I FROM C ORDER BY PKEY OFFSET 3

-- May cause NPE in HsqlDb (ENG-8273):
--select A.ID, NUM(*), (SELECT max(ID) FROM R2 B where B.NUM = A.NUM) FROM _table A GROUP BY ID, NUM order by ID, NUM

-- Test ORDER BY, especially with DESC
select * from _table ORDER BY ID
select * from _table ORDER BY ID ASC
select * from _table ORDER BY ID DESC
select ID, NUM from _table   ORDER BY ID
select ID, NUM from _table   ORDER BY ID ASC
select ID, NUM from _table   ORDER BY ID DESC
select ID, NUM from _table   ORDER BY ID, NUM DESC
select ID, NUM from _table T ORDER BY T.ID DESC, T.NUM DESC
select ID, NUM from _table T ORDER BY T.ID DESC
select ID, NUM from _table   ORDER BY ID DESC

select ID, NUM from _table   ORDER BY ID, NUM
select ID, NUM from _table   ORDER BY NUM  ASC, ID   ASC
select ID, NUM from _table   ORDER BY ID  DESC, NUM DESC
select ID, NUM from _table   ORDER BY NUM DESC, ID  DESC
select ID, NUM from _table   ORDER BY ID  DESC, NUM  ASC
select ID, NUM from _table   ORDER BY NUM  ASC, ID  DESC

select ID, RATIO, NUM, DESC from _table ORDER BY DESC,      NUM
select RATIO, ID, NUM, DESC from _table ORDER BY DESC  ASC, NUM  ASC
select ID, RATIO, NUM, DESC from _table ORDER BY DESC DESC, NUM DESC
select RATIO, ID, NUM, DESC from _table ORDER BY DESC DESC, NUM DESC
select ID, RATIO, NUM, DESC from _table ORDER BY DESC  ASC, NUM DESC
select RATIO, ID, NUM, DESC from _table ORDER BY DESC DESC, NUM  ASC
