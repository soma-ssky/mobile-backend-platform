package expired.object.exception;

public class SskyException extends Exception {
	private static final long serialVersionUID = 0;

	static int ACCOUNT_ALREADY_LINKED = 0; // Error code indicating that an an account being linked is already linked to another user.
	static int CACHE_MISS = 1; // Error code indicating the result was not found in the cache.
	static int COMMAND_UNAVAILABLE = 1; // Error code indicating that the feature you tried to access is only available internally for testing purposes.
	static int CONNECTION_FAILED = 2; // Error code indicating the connection to the Parse servers failed.
	static int DUPLICATE_VALUE = 3; // Error code indicating that a unique field was given a value that is already taken.
	static int EMAIL_MISSING = 4; // Error code indicating that the email is missing, but must be specified.
	static int EMAIL_NOT_FOUND = 5; // Error code indicating that a user with the specified email was not found.
	static int EMAIL_TAKEN = 6; // Error code indicating that the email has already been taken.
	static int EXCEEDED_QUOTA = 7; // Error code indicating that an application quota was exceeded.
	static int FILE_DELETE_ERROR = 8; // Error code indicating that deleting a file failed.
	static int INCORRECT_TYPE = 9; // Error code indicating that a field was set to an inconsistent type.
	static int INTERNAL_SERVER_ERROR = 10; // Error code indicating that something has gone wrong with the server.
	static int INVALID_ACL = 11; // Error code indicating an invalid ACL was provided.
	static int INVALID_CHANNEL_NAME = 12; // Error code indicating an invalid channel name.
	static int INVALID_CLASS_NAME = 13; // Error code indicating a missing or invalid classname.
	static int INVALID_EMAIL_ADDRESS = 14; // Error code indicating that the email address was invalid.
	static int INVALID_FILE_NAME = 15; // Error code indicating that an invalid filename was used for ParseFile.
	static int INVALID_JSON = 16; // Error code indicating that badly formed JSON was received upstream.
	static int INVALID_KEY_NAME = 17; // Error code indicating an invalid key name.
	static int INVALID_LINKED_SESSION = 18; // Error code indicating that a user with a linked (e.g.
	static int INVALID_NESTED_KEY = 19; // Error code indicating that an invalid key was used in a nested JSONObject.
	static int INVALID_POINTER = 20; // Error code indicating a malformed pointer.
	static int INVALID_QUERY = 21; // Error code indicating you tried to query with a datatype that doesn't support it, like exact matching an array or object.
	static int INVALID_ROLE_NAME = 22; // Error code indicating that a role's name is invalid.
	static int LINKED_ID_MISSING = 23; // Error code indicating that a user cannot be linked to an account because that account's id could not be found.
	static int MISSING_OBJECT_ID = 24; // Error code indicating an unspecified object id.
	static int MUST_CREATE_USER_THROUGH_SIGNUP = 25; // Error code indicating that a user can only be created through signup.
	static int NOT_INITIALIZED = 26; // You must call Parse.initialize before using the Parse library.
	static int OBJECT_NOT_FOUND = 27; // Error code indicating the specified object doesn't exist.
	static int OBJECT_TOO_LARGE = 28; // Error code indicating that the object is too large.
	static int OPERATION_FORBIDDEN = 29; // Error code indicating that the operation isn't allowed for clients.
	static int OTHER_CAUSE = 30;

	static int PASSWORD_MISSING = 31; // Error code indicating that the password is missing or empty.
	static int PUSH_MISCONFIGURED = 32; // Error code indicating that push is misconfigured.
	static int SCRIPT_ERROR = 33; // Error code indicating that a Cloud Code script failed.
	static int SESSION_MISSING = 34; // Error code indicating that a user object without a valid session could not be altered.

	static int TIMEOUT = 35; // Error code indicating that the request timed out on the server.
	static int UNSUPPORTED_SERVICE = 36; // Error code indicating that a service being linked (e.g.
	static int USERNAME_MISSING = 37; // Error code indicating that the username is missing or empty.
	static int USERNAME_TAKEN = 38; // Error code indicating that the username has already been taken.
	static int VALIDATION_ERROR = 39; // Error code indicating that cloud code validation failed.

	private int errorCode;

	public SskyException(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getCode() {
		return errorCode;
	}
}
