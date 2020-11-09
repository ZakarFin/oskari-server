
-- NO CONSTRAINT ON maplayer_id since we want to keep the rows here even if layer is removed
-- But the value points to oskari_maplayer.id
-- maplayer_name is used to save the layer name that will be shown if the layer has been removed.
CREATE TABLE public.oskari_usage_maplayer (
    id integer NOT NULL,
    usage_date date NOT NULL,
    maplayer_id integer NOT NULL,
    daily_total integer NOT NULL DEFAULT 0,
    maplayer_name text
);

COMMENT ON TABLE public.oskari_usage_maplayer IS 'Daily usage of map layers';
COMMENT ON COLUM oskari_usage_maplayer.maplayer_id IS 'Reference to oskari_maplayer.id';
COMMENT ON COLUM oskari_usage_maplayer.maplayer_name IS 'Name of the maplayer in case the layer has been removed';
COMMENT ON COLUM oskari_usage_maplayer.usage_date IS 'Date for usage stats';
COMMENT ON COLUM oskari_usage_maplayer.daily_total IS 'Number of times the layer was used on this date';

CREATE SEQUENCE public.oskari_usage_maplayer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- There should be only one total count for date/maplayer
ALTER TABLE ONLY public.oskari_usage_maplayer
    ADD CONSTRAINT oskari_usage_maplayer__unique_dailyTotal UNIQUE (usage_date, maplayer_id);

ALTER TABLE ONLY public.oskari_usage_maplayer
    ADD CONSTRAINT oskari_usage_maplayer_pkey PRIMARY KEY (id);

CREATE INDEX oskari_usage_maplayer_date_idx ON public.oskari_usage_maplayer USING btree (usage_date, maplayer_id);
