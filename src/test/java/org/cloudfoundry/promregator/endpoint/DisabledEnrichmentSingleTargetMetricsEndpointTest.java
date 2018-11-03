package org.cloudfoundry.promregator.endpoint;

import java.io.IOException;
import java.util.HashMap;

import org.cloudfoundry.promregator.fetcher.TextFormat004Parser;
import org.cloudfoundry.promregator.mockServer.DefaultMetricsEndpointHttpHandler;
import org.cloudfoundry.promregator.mockServer.MetricsEndpointMockServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LabelEnrichmentMockedMetricsEndpointSpringApplication.class)
@TestPropertySource(locations="disabledLabelEnrichment.properties")
public class DisabledEnrichmentSingleTargetMetricsEndpointTest {

	private static MetricsEndpointMockServer mockServer;

	@BeforeClass
	public static void startMockedTargetMetricsEndpoint() throws IOException {
		mockServer = new MetricsEndpointMockServer();
		DefaultMetricsEndpointHttpHandler meh = mockServer.getMetricsEndpointHandler();
		meh.setResponse("# HELP dummy This is a dummy metric\n"+
				"# TYPE dummy counter\n"+
				"dummy{label=\"xyz\"} 42 1395066363000");
		
		mockServer.start();
	}
	
	@AfterClass
	public static void stopMockedTargetMetricsEndpoint() {
		mockServer.stop();
	}
	
	@Autowired
	@Qualifier("singleTargetMetricsEndpoint") // NB: Otherwise ambiguity with TestableSingleTargetMetricsEndpoint would be a problem - we want "the real one"
	private SingleTargetMetricsEndpoint subject;
	
	@Test
	public void testGetMetricsLabelsAreCorrectIfLabelEnrichmentIsDisabled() {
		Assert.assertNotNull(subject);
		
		String response = subject.getMetrics("faedbb0a-2273-4cb4-a659-bd31331f7daf", "0").getBody();
		
		Assert.assertNotNull(response);
		Assert.assertNotEquals("", response);
		
		TextFormat004Parser parser = new TextFormat004Parser(response);
		HashMap<String, MetricFamilySamples> mapMFS = parser.parse();
		
		MetricFamilySamples dummyMFS = mapMFS.get("dummy");
		Assert.assertNotNull(dummyMFS);
		
		Assert.assertEquals(1, dummyMFS.samples.size());
		
		Sample dummySample = dummyMFS.samples.get(0);
		Assert.assertNotNull(dummySample);
		
		Assert.assertEquals(2, dummySample.labelNames.size());
		Assert.assertEquals(2, dummySample.labelValues.size());
		
		int indexOfLabel = dummySample.labelNames.indexOf("label");
		Assert.assertNotEquals(-1, indexOfLabel);
		Assert.assertEquals("xyz", dummySample.labelValues.get(indexOfLabel));

		int indexOfInstance = dummySample.labelNames.indexOf("instance");
		Assert.assertNotEquals(-1, indexOfInstance);
		Assert.assertEquals("faedbb0a-2273-4cb4-a659-bd31331f7daf:0", dummySample.labelValues.get(indexOfInstance));
		
		MetricFamilySamples upMFS = mapMFS.get("promregator_up");
		Assert.assertNotNull(upMFS);
		Assert.assertEquals(1, upMFS.samples.size());
		
		Sample upSample = upMFS.samples.get(0);
		Assert.assertEquals(1, upSample.labelNames.size());
		Assert.assertEquals(1, upSample.labelValues.size());
		
		MetricFamilySamples scrapeDurationMFS = mapMFS.get("promregator_scrape_duration_seconds");
		Assert.assertNotNull(scrapeDurationMFS);
		Assert.assertEquals(1, scrapeDurationMFS.samples.size());
		
		Sample scrapeDurationSample = scrapeDurationMFS.samples.get(0);
		
		// NB: as the duration is part of the usual scraping response, it also must comply to the rules
		// if labelEnrichment is disabled.
		Assert.assertEquals(-1, scrapeDurationSample.labelNames.indexOf("org_name"));
		
		indexOfInstance = scrapeDurationSample.labelNames.indexOf("instance");
		Assert.assertNotEquals(-1, indexOfInstance);
		Assert.assertEquals("faedbb0a-2273-4cb4-a659-bd31331f7daf:0", scrapeDurationSample.labelValues.get(indexOfInstance));
	}
	
}
