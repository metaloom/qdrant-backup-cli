package io.metaloom.qdrant.cli.eta;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ETAUtilTest {

	@Test
	public void testETA() {
		// Started 10s ago
		long now = System.currentTimeMillis();
		long startTime = now - 10_000;

		// Managed 500 until now
		assertEquals("33m 10s", ETAUtil.getETA(500, 100_000, startTime, now));
		assertEquals("Unknown", ETAUtil.getETA(0, 100_000, startTime, now));
		assertEquals("1m 53.977s", ETAUtil.getETA(8066, 100_000, startTime, now));
		assertEquals("0s", ETAUtil.getETA(100_000, 100_000, startTime, now));
	}

	@Test
	public void testPercent() {
		assertEquals("0%", ETAUtil.getPercent(0, 1));
		assertEquals("100%", ETAUtil.getPercent(1, 1));
		assertEquals("50.0%", ETAUtil.getPercent(1, 2));
		assertEquals("0%", ETAUtil.getPercent(0, 0));
		assertEquals("0%", ETAUtil.getPercent(0, 100_000));
		assertEquals("50.0%", ETAUtil.getPercent(50_000, 100_000));
	}
}
