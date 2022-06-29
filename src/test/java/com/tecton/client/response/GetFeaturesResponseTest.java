package com.tecton.client.response;

import com.tecton.client.model.FeatureValue;
import com.tecton.client.model.SloInformation;
import com.tecton.client.model.SloInformation.SloIneligibilityReason;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

public class GetFeaturesResponseTest {

  GetFeaturesResponse getFeaturesResponse;
  String simpleResponse;
  String responseWithSlo;

  @Before
  public void setup() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    String simpleInput = classLoader.getResource("sample_response.json").getFile();
    simpleResponse = new String(Files.readAllBytes(Paths.get(simpleInput)));
    String sloInput = classLoader.getResource("sample_response_slo.json").getFile();
    responseWithSlo = new String(Files.readAllBytes(Paths.get(sloInput)));
  }

  @Test
  public void testSimpleResponse() {
    Duration duration = Duration.ofMillis(10);
    getFeaturesResponse = new GetFeaturesResponse(simpleResponse, duration);

    Assert.assertEquals(duration, getFeaturesResponse.getRequestLatency());
    Assert.assertFalse(getFeaturesResponse.getSloInformation().isPresent());
    checkFeatureValues(getFeaturesResponse.getFeatureValuesAsMap());
  }

  @Test
  public void testSloresponse() {
    Duration duration = Duration.ofMillis(10);
    getFeaturesResponse = new GetFeaturesResponse(responseWithSlo, duration);
    checkFeatureValues(getFeaturesResponse.getFeatureValuesAsMap());
    SloInformation sloInfo = getFeaturesResponse.getSloInformation().get();

    Assert.assertFalse(sloInfo.isSloEligible().get());
    Assert.assertEquals(new Double(0.034437937), sloInfo.getServerTimeSeconds().get());
    Assert.assertEquals(new Integer(13100000), sloInfo.getStoreResponseSizeBytes().get());
    Assert.assertEquals(1, sloInfo.getSloIneligibilityReasons().size());
    Assert.assertTrue(
        sloInfo
            .getSloIneligibilityReasons()
            .contains(SloIneligibilityReason.DYNAMODB_RESPONSE_SIZE_LIMIT_EXCEEDED));
  }

  private void checkFeatureValues(Map<String, FeatureValue> featureValues) {
    Assert.assertEquals(5, getFeaturesResponse.getFeatureValues().size());
    Assert.assertEquals(
        new Long(0), featureValues.get("average_rain.rain_in_last_24_hrs").int64value());
    Assert.assertEquals(
        Boolean.FALSE,
        featureValues.get("average_rain.precipitation_higher_than_average").booleanValue());
    Assert.assertNull(featureValues.get("average_rain.atmospheric_pressure").float64Value());
    Assert.assertEquals(featureValues.get("average_rain.cloud_type").stringValue(), "nimbostratus");
    Assert.assertEquals(
        featureValues.get("average_rain.average_temperate_24hrs").float64Value(), new Double(55.5));
  }
}