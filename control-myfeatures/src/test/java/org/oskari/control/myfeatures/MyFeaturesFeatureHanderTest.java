package org.oskari.control.myfeatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.oskari.control.myfeatures.dto.CreateMyFeaturesFeature;
import org.oskari.util.ObjectMapperProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.control.ActionParameters;

public class MyFeaturesFeatureHanderTest {

    private static MyFeaturesFeatureHandler handler;

    @BeforeAll
    public static void init() {
        handler = new MyFeaturesFeatureHandler();
        // Don't init(), we haven't initialized MyFeaturesService and don't want the default
        handler.setObjectMapper(ObjectMapperProvider.OM);
    }

    @Test
    public void parsePayloadWorksWithGeoJSONGeometry() throws Exception {
        CreateMyFeaturesFeature expected = new CreateMyFeaturesFeature();
        expected.setLayerId(UUID.fromString("6a58d4ee-d91f-45ea-bc56-0870b8866793"));
        expected.setGeometry(new GeometryFactory().createPoint(new Coordinate(125.6, 10.1)));
        expected.setProperties(Map.of("fid", "abc123", "name", "Dinagat Islands"));

        String input = "{'layerId': '6a58d4ee-d91f-45ea-bc56-0870b8866793', 'geometry': {'type': 'Point','coordinates': [125.6, 10.1]}, 'properties': {'fid': 'abc123','name': 'Dinagat Islands'}}"
                .replace('\'', '"');
        ActionParameters params = mock(ActionParameters.class);
        when(params.getPayLoad()).thenReturn(input);

        CreateMyFeaturesFeature actual = handler.parsePayload(params, CreateMyFeaturesFeature.class);
        assertEquals(expected.getLayerId(), actual.getLayerId());
        assertEquals(expected.getGeometry(), actual.getGeometry());
        assertEquals(expected.getProperties(), actual.getProperties());

        String response = handler.toJSONString(actual.toDomain(new ObjectMapper()));
        String expectedResponse = "{'id':0,'created':null,'updated':null,'geometry':{'type':'Point','coordinates':[125.6,10.1]},'properties':{'fid':'abc123','name':'Dinagat Islands'},\"databaseSRID\":0,\"applicationSRID\":0}"
                .replace('\'', '"');
        assertEquals(expectedResponse, response);
    }

}
