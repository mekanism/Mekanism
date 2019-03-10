package mekanism.common.tile.component;

import java.util.Observable;
import java.util.Observer;
import net.minecraft.util.EnumFacing;

public class SideConfig {

    private byte[] directions = new byte[EnumFacing.VALUES.length];
    private Observable observable = new Observable();

    public SideConfig() {
    }

    public SideConfig(byte[] b) {
        this(b[0], b[1], b[2], b[3], b[4], b[5]);
        assert b.length == EnumFacing.VALUES.length;
    }

    public SideConfig(byte d, byte u, byte n, byte s, byte w, byte e) {
        setDirections(d, u, n, s, w, e);
    }

    public void setDirections(byte d, byte u, byte n, byte s, byte w, byte e) {
        directions[0] = d;
        directions[1] = u;
        directions[2] = n;
        directions[3] = s;
        directions[4] = w;
        directions[5] = e;

        observable.notifyObservers(this);
    }

    public byte get(EnumFacing f) {
        return directions[f.ordinal()];
    }

    public byte[] asByteArray() {
        return directions;
    }

    public void set(EnumFacing f, byte value) {
        directions[f.ordinal()] = value;
        observable.notifyObservers(this);
    }

    public void addObserver(Observer o) {
        observable.addObserver(o);
    }

    public void deleteObserver(Observer o) {
        observable.deleteObserver(o);
    }
}
