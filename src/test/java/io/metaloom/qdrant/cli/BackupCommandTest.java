package io.metaloom.qdrant.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class BackupCommandTest extends AbstractCommandTest {

	@Test
	public void testBackup() {
		String backupPath = "target/backup.json";
		File backupFile = new File(backupPath);
		if (backupFile.exists()) {
			backupFile.delete();
		}
		String[] args = new String[] { "-v", "backup", "points", "-c", TEST_COLLECTION_NAME, "-h", "localhost", "-p",
			String.valueOf(qdrant.httpPort()), backupPath };
		assertEquals(0, QDrantCLI.execute(args));
		assertTrue("The backup output file could not be found", backupFile.exists());
	}
}
