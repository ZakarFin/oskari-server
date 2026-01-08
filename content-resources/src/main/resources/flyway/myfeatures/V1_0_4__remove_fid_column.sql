WITH layers_to_update AS (
    SELECT DISTINCT layer_id AS id
    FROM myfeatures_feature
    WHERE fid IS NOT NULL
    AND fid <> ''
)
UPDATE myfeatures_layer
SET fields = (fields::jsonb || jsonb_build_array(jsonb_build_object('name', 'fid', 'type', 'String')))::json
FROM layers_to_update
WHERE myfeatures_layer.id = layers_to_update.id;

UPDATE myfeatures_feature
SET properties = (properties::jsonb || jsonb_build_object('fid', fid))::json;

ALTER TABLE myfeatures_feature DROP COLUMN fid;