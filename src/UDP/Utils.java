package UDP;

import java.net.DatagramPacket;
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

    public static int getPacketSeqNumber(DatagramPacket packet) {
        byte[] seqNumberInBytes = new byte[4];

        for(int i = 0; i < 4; i++) {
            seqNumberInBytes[i] = packet.getData()[i];
        }

        return byteArrayToInt(seqNumberInBytes);
    }
}
