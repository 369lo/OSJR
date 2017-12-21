package os.jr.hooks;

import os.jr.hooks.loader.GameClass;

public class CacheableNode extends GameClass {

	public static final String previous = "previous";
	public static final String next = "next";

	public CacheableNode(Object reference) {
		super(Hooks.classNames.get("CacheableNode"));
		this.reference = reference;
	}

	public CacheableNode(String className, Object reference) {
		super(className);
	}

	public CacheableNode getNext() {
		return new CacheableNode(fields.get(next).getValue(reference));
	}

	public CacheableNode getPrevious() {
		return new CacheableNode(fields.get(previous).getValue(reference));
	}

}
