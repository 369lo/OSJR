package os.jr.hooks;

public class ByteArrayNode extends Node {

	public static final String byteArray = "byteArray";

	public ByteArrayNode() {
		super(Hooks.classNames.get("ByteArrayNode"));
	}

	public byte[] getByteArray() {
		return (byte[]) fields.get(byteArray).getValue(reference);
	}

}
