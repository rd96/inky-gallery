UPDATE canvases AS c
SET canvas_status = 'FINISHED'
FROM (
         SELECT canvas_id
         FROM drawings
         GROUP BY canvas_id
         HAVING COUNT(*) = 1
     ) AS d
WHERE c.id = d.canvas_id
  AND c.canvas_type = 'SINGLE'
  AND c.canvas_status = 'DRAFT';