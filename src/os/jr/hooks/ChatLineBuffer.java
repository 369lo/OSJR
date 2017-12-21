package os.jr.hooks;

import os.jr.hooks.loader.GameClass;

public class ChatLineBuffer extends GameClass {

	public static final String lines = "lines";
	public static final String length = "length";

	public ChatLineBuffer() {
		super(Hooks.classNames.get("ChatLineBuffer"));
	}

	public int getLength() {
		return (int) fields.get(length).getValue(reference);
	}

	public MessageNode[] getLines() {
		Object[] os = (Object[]) fields.get(lines).getValue(reference);
		MessageNode[] lines = new MessageNode[os.length];
		int count = 0;
		for (Object o : os) {
			lines[count] = new MessageNode(o);
			count++;
		}
		return lines;
	}

}
