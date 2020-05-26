package UDP;

import java.nio.ByteBuffer;

public class Utils {

    public static int byteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    public static byte[] boolToByteArray(boolean b) {
        return new byte[] {(byte) (b ? 1 : 0)};
    }

    public static boolean byteArrayToBool(byte b) {
        return b == 1;
    }
}
