package org.oskari.maplayer.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import org.junit.jupiter.api.Test;
import org.oskari.maplayer.model.MapLayer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LayerValidatorTest {

    @Test
    public void validateUrl() {
        String url = "https://api.maptiler.com/tiles/v3/{z}/{x}/{y}.pbf";
        String validated = LayerValidator.sanitizeUrl(url);
        assertEquals( url, validated, "URL should be unchanged");
    }

    @Test
    public void validateUrlNull() {
        String url = null;
        String validated = LayerValidator.sanitizeUrl(url);
        assertNull(validated, "URL should be unchanged");
    }

    @Test()
    public void validateUrlNonvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            String url = "sgashahah";
            String validated = LayerValidator.sanitizeUrl(url);
        });
    }

    @Test
    public void validateWMSLayer() {
        MapLayer input = new MapLayer();
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'type'", e.getMessage());
        }

        input.setType(OskariLayer.TYPE_WMS);
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'name'", e.getMessage());
        }
        input.setName("testing");
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'url'", e.getMessage());
        }

        input.setUrl("http://oskari.org/testing");
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'version'", e.getMessage());
        }

        input.setVersion("1.1.0");
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            //
            assertEquals("Localization for layer names missing", e.getMessage());
        }
        input.setLocale(new HashMap<>());
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            // Localization for layer names missing
            assertEquals("Name missing for default language: en", e.getMessage());
        }

        input.setLocale(createValidLocaleForLayer());
        LayerValidator.validateAndSanitizeLayerInput(input);
    }
    @Test
    public void validateBingLayer() {
        MapLayer input = new MapLayer();
        input.setType(OskariLayer.TYPE_BINGLAYER);
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'options.apiKey'", e.getMessage());
        }
        input.setOptions(new HashMap<>());
        try {
            LayerValidator.validateAndSanitizeLayerInput(input);
        } catch (IllegalArgumentException e) {
            assertEquals("Required field missing 'options.apiKey'", e.getMessage());
        }

        input.setLocale(createValidLocaleForLayer());

        HashMap jee = new HashMap<>();
        jee.put("apiKey", "testing");
        input.setOptions(jee);
        LayerValidator.validateAndSanitizeLayerInput(input);
    }

    @Test
    public void testShift() {
        String path = "testing.like.there's.no.tomorrow";
        String[] original = path.split("\\.");
        String[] rest = LayerValidator.shiftArray(original);
        for (int i = 1; i < original.length; i++) {
            assertEquals(original[i], rest[0]);
            rest = LayerValidator.shiftArray(rest);
        }
        assertEquals(rest.length, 0, "Rest should be empty");
    }

    private Map<String, Map<String, String>> createValidLocaleForLayer() {
        HashMap locale = new HashMap<>();
        HashMap en = new HashMap<>();
        en.put("name", "testing");
        locale.put("en", en);
        return locale;
    }
}