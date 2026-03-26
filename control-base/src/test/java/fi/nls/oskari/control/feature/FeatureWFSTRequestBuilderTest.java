package fi.nls.oskari.control.feature;

import fi.nls.oskari.domain.map.Feature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class FeatureWFSTRequestBuilderTest {

    @Test
    public void testUpdateOmitsValueElementForNullProperty() throws Exception {
        Feature feature = new Feature();
        feature.setLayerName("foo");
        feature.setId("feature.1");
        feature.addProperty("omistaja", "testi");
        feature.addProperty("asuntoalat", null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FeatureWFSTRequestBuilder.updateFeature(baos, feature);
        Document doc = parseXML(baos.toString());

        NodeList properties = doc.getElementsByTagNameNS("http://www.opengis.net/wfs", "Property");
        for (int i = 0; i < properties.getLength(); i++) {
            NodeList children = properties.item(i).getChildNodes();
            String name = null;
            String value = null;
            for (int j = 0; j < children.getLength(); j++) {
                if ("Name".equals(children.item(j).getLocalName())) {
                    name = children.item(j).getTextContent();
                }
                if ("Value".equals(children.item(j).getLocalName())) {
                    value = children.item(j).getTextContent();
                }
            }
            if ("omistaja".equals(name)) {
                Assertions.assertEquals("testi", value);
            } else if ("asuntoalat".equals(name)) {
                // Value element should be omitted entirely for null
                Assertions.assertNull(value, "Null property should not have a Value element");
            }
        }
    }

    @Test
    public void testInsertOmitsNullProperty() throws Exception {
        Feature feature = new Feature();
        feature.setLayerName("foo");
        feature.addProperty("omistaja", "testi");
        feature.addProperty("asuntoalat", null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FeatureWFSTRequestBuilder.insertFeature(baos, feature);
        String xml = baos.toString();

        Document doc = parseXML(xml);
        // "omistaja" element should exist
        Assertions.assertEquals(1, doc.getElementsByTagName("omistaja").getLength());
        // "asuntoalat" element should be omitted entirely
        Assertions.assertEquals(0, doc.getElementsByTagName("asuntoalat").getLength());
    }

    private Document parseXML(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
