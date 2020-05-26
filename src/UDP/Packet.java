package UDP;

import java.net.DatagramPacket;

public class Packet {
    int seqNumber;
    DatagramPacket packet;
    boolean isLast;

    public Packet(int _seqNumber, DatagramPacket _packet, boolean _isLast) {
        this.seqNumber = _seqNumber;
        this.packet = _packet;
        this.isLast = _isLast;
    }

    /* GETTERS AND SETTERS */
    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public void setPacket(DatagramPacket packet) {
        this.packet = packet;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }
}
