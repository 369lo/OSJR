package os.jr.hooks;

import os.jr.hooks.loader.GameClass;

public class Task extends GameClass {

	public Task(Object reference) {
		super(Hooks.classNames.get("Task"));
		this.reference = reference;
	}

}
