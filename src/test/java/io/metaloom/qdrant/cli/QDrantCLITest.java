package io.metaloom.qdrant.cli;

import org.junit.Test;

public class QDrantCLITest {

	@Test
	public void testHelp() {
		QDrantCLI.execute(new String[] {"--help"});
	}
}
