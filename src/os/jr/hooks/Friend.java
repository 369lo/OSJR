package os.jr.hooks;

import os.jr.hooks.model.GameClass;

public class Friend extends GameClass{

	public Friend(Object reference) {
		super(Hooks.classNames.get("Friend"));
		this.reference = reference;
	}

}
