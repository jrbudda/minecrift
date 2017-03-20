package jopenvr;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Platform;

abstract class AlignedStructure extends Structure {
	protected AlignedStructure() {
		super();
	}

	protected AlignedStructure(Pointer peer) {
		super(peer);
	}

	protected int getNativeAlignment(Class type, Object value, boolean isFirstElement) {
		if (Platform.isLinux() || Platform.isMac()) {
			int ret = super.getNativeAlignment(type, value, isFirstElement);
			if (ret > 4) return 4;
			return ret;
		} else {
			return super.getNativeAlignment(type, value, isFirstElement);
		}
	}
}
