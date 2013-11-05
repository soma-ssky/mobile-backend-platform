package expired.object.callback;

import expired.object.exception.SskyException;
import expired.object.object.SskyObject;

abstract public class GetCallback<T> {
	abstract public void done(SskyObject object, SskyException e);
}
