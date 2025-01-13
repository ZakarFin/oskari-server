package org.geotools.mif;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class MIFHeaderTest {

    @Test
    public void testHeaderParsing() throws URISyntaxException, IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {
        File mif = new File(getClass().getResource("kenro_alue_maarajat.MIF").toURI());
        MIFDataStore store = new MIFDataStore(mif, null);
        MIFHeader header = store.readHeader();
        assertEquals(750, header.getVersion());
        assertEquals(StandardCharsets.ISO_8859_1, header.getCharset());
        assertEquals(",", header.getDelimiter());

        assertEquals(8, header.getColumns().length);
        int i = 0;
        assertEquals("id", header.getColumns()[i++].getName());
        assertEquals("aineisto_id", header.getColumns()[i++].getName());
        assertEquals("aluekoodi", header.getColumns()[i++].getName());
        assertEquals("nimi", header.getColumns()[i++].getName());
        assertEquals("nimi_se", header.getColumns()[i++].getName());
        assertEquals("www_osoite", header.getColumns()[i++].getName());
        assertEquals("modify_user", header.getColumns()[i++].getName());
        assertEquals("modify_time", header.getColumns()[i++].getName());
    }

    @Test
    public void testCoordSysWithExplicitBoundsTypeCode() throws URISyntaxException, IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {
        File mif = new File(getClass().getResource("has_explicit_bounds.mif").toURI());
        MIFDataStore store = new MIFDataStore(mif, null);
        MIFHeader header = store.readHeader();
        assertEquals(750, header.getVersion());
        assertEquals(StandardCharsets.ISO_8859_1, header.getCharset());
        CoordinateReferenceSystem actual = header.getCoordSys();
        MathTransform t = CRS.findMathTransform(actual, CRS.decode("EPSG:3067", true));
        double[] src = new double[2];
        double[] dst = new double[2];
        src[0] = 357517.2;
        src[1] = 6860602.8;
        t.transform(src, 0, dst, 0, 1);
        assertArrayEquals(src, dst, 0.0);
    }

}
