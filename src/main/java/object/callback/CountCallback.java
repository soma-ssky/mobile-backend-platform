package main.java.object.callback;

import main.java.object.exception.SskyException;

abstract public class CountCallback {

	abstract public void done(int count, SskyException e);
}
