package fi.nls.test.view;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;

/**
 * @author SMAKINEN
 * Helper class for creating mock views
 */
public class ViewTestHelper {

    /**
     * Populates a view object with given bundles.
     * Bundles are given as string like "framework.mapfull" and are loaded as resources with BundleTestHelper
     * @param bundles
     * @return
     */
    public static View createMockView(String ... bundles){
        final View view = new View();
        view.setName("test");
        view.setDescription("test view");
        view.setLang("fi");
        view.setUuid("aaaa-bbbbb-cccc");
        view.setId(123);
        view.setIsPublic(true);
        view.setIsDefault(false);
        view.setPubDomain("paikkis.fi");
        view.setCreated(OffsetDateTime.parse("2022-05-18T12:15:15.15Z", DateTimeFormatter.ISO_DATE_TIME));
        view.setUpdated(OffsetDateTime.parse("2022-05-18T12:15:15.15Z", DateTimeFormatter.ISO_DATE_TIME));
        try {
            for(String bundle : bundles) {
                Bundle b = BundleTestHelper.loadBundle(bundle);
                view.addBundle(b);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to create view", e);
            //fail("Unable to create view");
        }
        return view;
    }

}
