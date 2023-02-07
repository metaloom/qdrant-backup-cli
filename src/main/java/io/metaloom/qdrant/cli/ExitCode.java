package io.metaloom.qdrant.cli;

public enum ExitCode {

	// Command executed successfully
	OK(0),

	// Unexpected error happened
	ERROR(10),

	// Connection error
	CONNECT_ERROR(15),

	// Invalid parameters were specified
	INVALID_PARAMETER(2),

	// Error due to filesystem issues (e.g. file exists, missing perm)
	FILE_ERROR(3),

	// Request to server failed on server-side.
	SERVER_FAILURE(20);

	int code;

	ExitCode(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}
}
