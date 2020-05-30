package UDP;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Utils {

    /**
 * Conversion d'octet a int
 * @param {byte[]} array d'octets a convertir
 */

    public static int byteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

       /**
 * Conversion d'int a array d'octets
 * @param {int} int a convertir
 */
    public static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

       /**
 * Conversion de bool vers array d'octets
 * @param {boolean} bool a convertir
 */

    public static byte[] boolToByteArray(boolean b) {
        return new byte[] {(byte) (b ? 1 : 0)};
    }

       /**
 * Conversion d'octet a bool
 * @param {byte[]} array d'octets a convertir
 */

    public static boolean byteToBool(byte b) {
        return b == 1;
    }

       /**
 *Conversion du numero de paquet en array d'octets (4 premiers octets du paquet)
 * @param {paquet} paquet recu dont les 4 premiers octets contiennent le numero de paquet
 */
    public static byte[] getPacketSeqNumberInBytes(DatagramPacket packet) {
        byte[] seqNumberInBytes = new byte[4];

        for(int i = 0; i < 4; i++) {
            seqNumberInBytes[i] = packet.getData()[i];
        }

        return seqNumberInBytes;
    }

       /**
 * Verification si cest le dernier paquet
 * @param {paquet} array d'octets a convertir (octet[4])
 */

    public static boolean isLastPacket(DatagramPacket packet) {
        return Utils.byteToBool(packet.getData()[4]);
    }
}
