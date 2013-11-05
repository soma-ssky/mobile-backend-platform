package expired.object.callback;

import expired.object.exception.SskyException;
import expired.object.object.SskyObject;

abstract public class RefreshCallback {

	abstract public void done(SskyObject object, SskyException e);

}
