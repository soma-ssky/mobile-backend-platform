package expired.object.callback;

import java.util.List;

import expired.object.exception.SskyException;

abstract public class FindCallback<T> {
	public abstract void done(List<T> objects, SskyException e);
}
