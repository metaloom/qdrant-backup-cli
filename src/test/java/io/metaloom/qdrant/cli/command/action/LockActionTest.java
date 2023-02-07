package io.metaloom.qdrant.cli.command.action;

import static io.metaloom.qdrant.cli.ExitCode.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.metaloom.qdrant.cli.AbstractCLITest;
import io.metaloom.qdrant.cli.command.action.impl.AdminAction;
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.impl.HttpErrorException;
import io.metaloom.qdrant.client.http.model.service.LockOption;

public class LockActionTest extends AbstractCLITest {

	private static final String LOCK_MSG = "dummy msg";

	@Test
	public void testLock() throws HttpErrorException, InterruptedException {
		try (QDrantHttpClient client = newClient()) {
			assertLock(client, false, null);
			assertEquals(OK, new AdminAction(dummyCommand()).lock(LOCK_MSG));
			assertEquals(OK, new AdminAction(dummyCommand()).status());
			assertLock(client, true, LOCK_MSG);
			assertEquals(OK, new AdminAction(dummyCommand()).unlock());
			assertLock(client, false, null);
		}
	}

	private void assertLock(QDrantHttpClient client, boolean expectedLockStatus, String msg) throws HttpErrorException {
		LockOption result = client.getLockOptions().sync().getResult();
		assertEquals(msg, result.getErrorMessage());
		if (expectedLockStatus) {
			assertTrue("Write should be enabled", result.isWrite());
		} else {
			assertFalse("Write should be disabled", result.isWrite());
		}

	}

}
