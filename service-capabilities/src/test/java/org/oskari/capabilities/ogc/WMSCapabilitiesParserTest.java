package org.oskari.capabilities.ogc;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ogc.wms.WMSCapsParser1_1_1;
import org.oskari.capabilities.ogc.wms.WMSCapsParser1_3_0;

import java.util.*;

public class WMSCapabilitiesParserTest {

    private static final Set<String> SYSTEM_CRS = new HashSet<>(5);
    private WMSCapabilitiesParser parser = new WMSCapabilitiesParser();

    @BeforeAll
    public static void init() {
        SYSTEM_CRS.add("EPSG:3857");
        SYSTEM_CRS.add("EPSG:3067");
    }

    @Test
    public void parseCp1_1_1() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-cp_1_1_1-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-cp-expected.json", this);

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(2, layers.size(), "Should find layers");
        LayerCapabilitiesWMS layerCaps = (LayerCapabilitiesWMS) layers.get("CP.CadastralBoundary");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        JSONObject expectedJSON = JSONHelper.createJSONObject(expected);
        // Check and remove version as it is different on expected between 1.1.1 and 1.3.0 input
        Assertions.assertEquals(WMSCapsParser1_1_1.VERSION, json.remove("version"), "Check version");
        // Note! 1.1.1 doesn't have the metadata url
        expectedJSON.remove(LayerCapabilitiesOGC.METADATA_URL);
        expectedJSON.remove(LayerCapabilitiesOGC.METADATA_UUID);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, expectedJSON), "JSON should match");

        String wkt = "POLYGON ((15.608220469655935 59.36205414098515, 15.608220469655935 70.09468368748001, 33.107629330539034 70.09468368748001, 33.107629330539034 59.36205414098515, 15.608220469655935 59.36205414098515))";
        Assertions.assertEquals(wkt, layerCaps.getBbox().getWKT(), "Coverage should match");
    }

    @Test
    public void parseCp1_3_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-cp_1_3_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-cp-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(2, layers.size(), "Should find layers");
        LayerCapabilitiesWMS layerCaps = (LayerCapabilitiesWMS) layers.get("CP.CadastralBoundary");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        // Check and remove version as it is different on expected between 1.1.1 and 1.3.0 input
        Assertions.assertEquals(WMSCapsParser1_3_0.VERSION, json.remove("version"), "Check version");
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");

        String wkt = "POLYGON ((15.608220469655935 59.36205414098515, 15.608220469655935 70.09468368748001, 33.107629330539034 70.09468368748001, 33.107629330539034 59.36205414098515, 15.608220469655935 59.36205414098515))";
        Assertions.assertEquals(wkt, layerCaps.getBbox().getWKT(), "Coverage should match");
    }

    @Test
    public void parseChloro1_1_1() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-chloro_1_1_1-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-chloro-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(9, layers.size(), "Should find layers");
        LayerCapabilitiesWMS layerCaps = (LayerCapabilitiesWMS) layers.get("arctic_sdi:SeaSurfaceTemperature");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        // Check and remove version as it is different on expected between 1.1.1 and 1.3.0 input
        Assertions.assertEquals(WMSCapsParser1_1_1.VERSION, json.remove("version"), "Check version");
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");

        String wkt = "POLYGON ((-180.0 32.536799480530846, -180.0 90.0, 180.0 90.0, 180.0 32.536799480530846, -180.0 32.536799480530846))";
        Assertions.assertEquals(wkt, layerCaps.getBbox().getWKT(), "Coverage should match");
    }

    @Test
    public void parseChloro1_3_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-chloro_1_3_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-chloro-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(9, layers.size(), "Should find layers");
        LayerCapabilitiesWMS layerCaps = (LayerCapabilitiesWMS) layers.get("arctic_sdi:SeaSurfaceTemperature");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        // Check and remove version as it is different on expected between 1.1.1 and 1.3.0 input
        Assertions.assertEquals(WMSCapsParser1_3_0.VERSION, json.remove("version"), "Check version");
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");

        String wkt = "POLYGON ((-180.0 32.536799480530846, -180.0 90.0, 180.0 90.0, 180.0 32.536799480530846, -180.0 32.536799480530846))";
        Assertions.assertEquals(wkt, layerCaps.getBbox().getWKT(), "Coverage should match");
    }

    @Test
    public void parseDuplicatedName() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-capabilities-with-duplicated-layername-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-capabilities-with-duplicated-layername-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(25, layers.size(), "Should find a layer");

        JSONObject json = CapabilitiesService.toJSON(layers.get("muinaismuistot"), SYSTEM_CRS);
        // Check and remove version as it is different on expected between 1.1.1 and 1.3.0 input
        Assertions.assertEquals(WMSCapsParser1_3_0.VERSION, json.remove("version"), "Check version");
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }

    @Test
    public void parseLmi1_1_1() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-lmi_1_1_1-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-lmi-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(23, layers.size(), "Should find layers");
        LayerCapabilitiesWMS layerCaps = (LayerCapabilitiesWMS) layers.get("LMI_Island_einfalt");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }

    @Test
    public void parseLipas1_3_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-lipas_1_3_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-lipas-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(175, layers.size(), "Should find layers");
        LayerCapabilitiesWMS layerCaps = (LayerCapabilitiesWMS) layers.get("lipas_1500_jaaurheilualueet_ja_luonnonjaat");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }
    @Test
    public void parseNBA1_3_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-nba_1_3_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-nba-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(10, layers.size(), "Should find layers");
        LayerCapabilitiesWMS layerCaps = (LayerCapabilitiesWMS) layers.get("1");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }

    @Test
    public void parseWeatherWMST1_3_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-weather-wms-t-1.3.0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-weather-wms-t-1.3.0-output.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(3, layers.size(), "Should find layers");
        LayerCapabilitiesWMS layerCaps = (LayerCapabilitiesWMS) layers.get("GDPS.ETA_TT");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }



    @Test
    public void parseDummy() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-dummy_1_3_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-dummy-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(1, layers.size(), "Should find a layer");
        LayerCapabilitiesWMS layerCaps = (LayerCapabilitiesWMS) layers.get("LayerName");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }
}