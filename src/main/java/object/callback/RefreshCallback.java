package main.java.object.callback;

import main.java.object.exception.SskyException;
import main.java.object.object.SskyObject;

abstract public class RefreshCallback {

	abstract public void done(SskyObject object, SskyException e);

}
