package main.java.object.callback;

import java.util.List;

import main.java.object.exception.SskyException;

abstract public class FindCallback<T> {
	public abstract void done(List<T> objects, SskyException e);
}
