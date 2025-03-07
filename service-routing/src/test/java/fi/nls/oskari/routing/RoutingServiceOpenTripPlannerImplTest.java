package fi.nls.oskari.routing;


import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class RoutingServiceOpenTripPlannerImplTest {
    private static final Logger LOGGER = LogFactory.getLogger(RoutingServiceOpenTripPlannerImplTest.class);
    private static final RoutingServiceOpenTripPlannerImpl ROUTING_SERVICE = new RoutingServiceOpenTripPlannerImpl();

    public static final String ROUTING_SRS = "EPSG:4326";
    private static final String MAP_SRS = "EPSG:3067";
    private static final String JSON_ENCODING = "UTF-8";

    private HttpURLConnection mockHttpURLConnection;
    private MockedStatic<IOHelper> mockIOHelper;
    @BeforeEach
    public void initialize()throws Exception{
        PropertyUtil.addProperty("routing.srs", ROUTING_SRS);
        mockIOHelper = mockStatic(IOHelper.class);
        mockHttpURLConnection = mock(HttpURLConnection.class);

    }

    @AfterEach
    public void goAway() throws Exception{
        PropertyUtil.clearProperties();
        if (mockIOHelper != null) {
            mockIOHelper.close();
        }
    }

    private RouteParams initRouteParams() {
        RouteParams routeParams = new RouteParams();
        routeParams.setSrs(MAP_SRS);
        routeParams.setFrom(1235467.0, 765432.0);
        routeParams.setTo(2235467.0, 665432.0);
        routeParams.setDate(OffsetDateTime.now());
        routeParams.setMode(null);
        routeParams.setIsArriveBy(true);
        routeParams.setIsWheelChair(false);
        routeParams.setLang("fi");

        return routeParams;
    }
    @Test
    public void testParseRouteThatIsOk() throws Exception {

        String responseJson = new String(getClass().getResourceAsStream("digitransit-response-success-v2.json").readAllBytes());
        when(IOHelper.post((String)any(), any(), (byte[])any())).thenReturn(this.mockHttpURLConnection);
        when(IOHelper.readString((InputStream)any(), (String)any())).thenReturn(responseJson);

        RouteParams routeParams = initRouteParams();
        RouteResponse response = ROUTING_SERVICE.getRoute(routeParams);

        Assertions.assertTrue(response.isSuccess());
    }

    @Test
    public void testParseRouteThatIsNotOk() throws Exception {
        String responseJson = new String(getClass().getResourceAsStream("digitransit-response-error-v2.json").readAllBytes());
        when(IOHelper.post((String)any(), any(), (byte[])any())).thenReturn(this.mockHttpURLConnection);
        when(IOHelper.readString((InputStream)any(), (String)any())).thenReturn(responseJson);

        RouteParams routeParams = initRouteParams();
        RouteResponse response = ROUTING_SERVICE.getRoute(routeParams);

        Assertions.assertFalse(response.isSuccess());

    }
}
