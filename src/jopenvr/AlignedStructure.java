package jopenvr;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Platform;

abstract class AlignedStructure extends Structure {
	AlignedStructure() {
		super();
	}
        AlignedStructure(Pointer peer) {
		super(peer);
	}

	protected int getNativeAlignment(Class type, Object value, boolean isFirstElement) {
	  /*if ((!isFirstElement || inArray)
		    && ((type == long.class) || (type == Long.class))
		    && (Platform.isLinux() || Platform.isMac())) {
			return 4;
		} else {
			return super.getNativeAlignment(type, value, isFirstElement);
			}*/

	  int ret = super.getNativeAlignment(type, value, isFirstElement);
	  if (ret > 4)
	    return 4;
	  return ret;
	}
}
