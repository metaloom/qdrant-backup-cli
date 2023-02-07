package io.metaloom.qdrant.cli.eta;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public final class ETAUtil {

	private ETAUtil() {
	}

	/**
	 * Calculate the ETA.
	 * 
	 * @param current
	 * @param totalCount
	 * @param startTime
	 * @param now
	 * @return
	 */
	public static String getETA(long current, long totalCount, long startTime, long now) {
		if (current == 0) {
			return "Unknown";
		}
		long durationMs = now - startTime;
		long remaining = totalCount - current;

		// Elements/ms
		double speed = (double) current / (double) durationMs;

		long etaMs = (long) (remaining / speed);

		Duration dur = Duration.of(etaMs, ChronoUnit.MILLIS);
		return dur.toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase();
	}

	/**
	 * Calculate the percent completed.
	 * 
	 * @param current
	 * @param totalCount
	 * @return
	 */
	public static String getPercent(long current, long totalCount) {
		if (current == 0) {
			return "0%";
		}
		if (current == totalCount) {
			return "100%";
		}
		double factor = ((double) current / (double) totalCount) * 100;
		return String.format("%.1f", factor) + "%";
	}
}
