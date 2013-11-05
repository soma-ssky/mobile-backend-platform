package expired.object.callback;

import expired.object.exception.SskyException;

abstract public class CountCallback {

	abstract public void done(int count, SskyException e);
}
