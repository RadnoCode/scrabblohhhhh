package com.kotva.lan;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LanClientConnectorEndpointTest {
    @Test
    public void normalizeEndpointForDisplayAcceptsDefaultPortAndFullWidthColon() {
        assertEquals(
                "10.190.129.253:5050",
                LanClientConnector.normalizeEndpointForDisplay(" 10.190.129.253 "));
        assertEquals(
                "10.190.129.253:5050",
                LanClientConnector.normalizeEndpointForDisplay("10.190.129.253：5050"));
        assertEquals(
                "10.190.129.253:5050",
                LanClientConnector.normalizeEndpointForDisplay("https://10.190.129.253:5050/"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void normalizeEndpointForDisplayRejectsOutOfRangePort() {
        LanClientConnector.normalizeEndpointForDisplay("10.190.129.253:70000");
    }
}
