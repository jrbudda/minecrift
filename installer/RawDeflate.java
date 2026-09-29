import java.io.*;
import java.util.zip.Deflater;

// Used by legacy40r12.py: deflates with the JDK's classic zlib, as the official builds'
// Python 2.7 did. Reads <int length><bytes> from stdin and answers <int length><raw deflate
// at level 6>, until a negative length.
public class RawDeflate {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(System.in));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(System.out));
        byte[] buf = new byte[65536];
        for (int n; (n = in.readInt()) >= 0; ) {
            byte[] data = new byte[n];
            in.readFully(data);
            Deflater deflater = new Deflater(6, true);
            deflater.setInput(data);
            deflater.finish();
            ByteArrayOutputStream deflated = new ByteArrayOutputStream();
            while (!deflater.finished())
                deflated.write(buf, 0, deflater.deflate(buf));
            deflater.end();
            out.writeInt(deflated.size());
            deflated.writeTo(out);
            out.flush();
        }
    }
}
