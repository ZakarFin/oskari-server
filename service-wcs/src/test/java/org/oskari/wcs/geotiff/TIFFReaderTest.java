package org.oskari.wcs.geotiff;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

public class TIFFReaderTest {

    @Test
    public void tiled_Int16_neg_32500() throws IOException {
        // gdal_create -outsize 512 512 -ot Int16 -co TILED=YES -co BLOCKXSIZE=256 -co BLOCKYSIZE=256 -burn -32500 int16_neg_32500.tif
        int expectedValue = -32500;

        TIFFReader r = new TIFFReader(readResource("int16_neg_32500.tif"));
        IFD ifd = r.getIFD(0);
        assertEquals(1, r.getIFDCount());
        assertEquals(1, ifd.getBitsPerSample().length);
        assertEquals(2, ifd.getSampleFormat()[0]);
        assertEquals(16, ifd.getBitsPerSample()[0]);
        assertEquals(256, ifd.getTileWidth());
        assertEquals(256, ifd.getTileHeight());
        assertEquals(512, ifd.getWidth());
        assertEquals(512, ifd.getHeight());

        short[] expected = new short[256 * 256];
        Arrays.fill(expected, (short) expectedValue);
        for (int tileIndex = 0; tileIndex < 4; tileIndex++) {
            short[] actual = r.readTile(0, tileIndex, new short[256 * 256]);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void tiled_UInt16_65500() throws IOException {
        // gdal_create -outsize 512 512 -ot UInt16 -co TILED=YES -co BLOCKXSIZE=256 -co BLOCKYSIZE=256 -burn 65500 uint16_65500.tif
        int expectedValue = 65500;

        TIFFReader r = new TIFFReader(readResource("uint16_65500.tif"));
        IFD ifd = r.getIFD(0);
        assertEquals(1, r.getIFDCount());
        assertEquals(1, ifd.getBitsPerSample().length);
        assertEquals(1, ifd.getSampleFormat()[0]);
        assertEquals(16, ifd.getBitsPerSample()[0]);
        assertEquals(256, ifd.getTileWidth());
        assertEquals(256, ifd.getTileHeight());
        assertEquals(512, ifd.getWidth());
        assertEquals(512, ifd.getHeight());

        short[] expected = new short[256 * 256];
        Arrays.fill(expected, (short) expectedValue);
        for (int tileIndex = 0; tileIndex < 4; tileIndex++) {
            short[] actual = r.readTile(0, tileIndex, new short[256 * 256]);
            assertEquals(expectedValue, actual[0] & 0xFFFF);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void tiled_Float32_neg_1337_125() throws IOException {
        // gdal_create -outsize 512 512 -ot Float32 -co TILED=YES -co BLOCKXSIZE=256 -co BLOCKYSIZE=256 -burn -1337.125 float32_neg_1337_125.tif
        float expectedValue = -1337.125f;

        TIFFReader r = new TIFFReader(readResource("float32_neg_1337_125.tif"));
        IFD ifd = r.getIFD(0);
        assertEquals(1, r.getIFDCount());
        assertEquals(1, ifd.getBitsPerSample().length);
        assertEquals(3, ifd.getSampleFormat()[0]);
        assertEquals(32, ifd.getBitsPerSample()[0]);
        assertEquals(256, ifd.getTileWidth());
        assertEquals(256, ifd.getTileHeight());
        assertEquals(512, ifd.getWidth());
        assertEquals(512, ifd.getHeight());

        float[] expected = new float[256 * 256];
        Arrays.fill(expected, expectedValue);
        for (int tileIndex = 0; tileIndex < 4; tileIndex++) {
            float[] actual = r.readTile(0, tileIndex, new float[256 * 256]);
            assertArrayEquals(expected, actual, 0.0f);
        }
    }

    private byte[] readResource(String res) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(res)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toByteArray();
        }
    }

}
