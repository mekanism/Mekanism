package mekanism.common.transmitters;

import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.ITextComponent;

public abstract class Transmitter<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, BUFFER>, BUFFER> implements IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> {

    public NETWORK theNetwork = null;

    public boolean orphaned = true;

    @Override
    public NETWORK getTransmitterNetwork() {
        return theNetwork;
    }

    @Override
    public void setTransmitterNetwork(NETWORK network) {
        if (theNetwork == network) {
            return;
        }
        if (world().isRemote && theNetwork != null) {
            theNetwork.removeTransmitter(this);
        }
        theNetwork = network;
        orphaned = theNetwork == null;
        if (world().isRemote) {
            if (theNetwork != null) {
                theNetwork.addTransmitter(this);
            }
        } else {
            setRequestsUpdate();
        }
    }

    @Override
    public boolean hasTransmitterNetwork() {
        return !isOrphan() && getTransmitterNetwork() != null;
    }

    @Override
    public int getTransmitterNetworkSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().transmittersSize() : 0;
    }

    @Override
    public int getTransmitterNetworkAcceptorSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getAcceptorSize() : 0;
    }

    @Override
    public ITextComponent getTransmitterNetworkNeeded() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getNeededInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    @Override
    public ITextComponent getTransmitterNetworkFlow() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getFlowInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    @Override
    public ITextComponent getTransmitterNetworkBuffer() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getStoredInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    @Override
    public int getTransmitterNetworkCapacity() {
        //This isn't *fully* accurate as the fluid and gas networks only actually support up to max int currently
        return hasTransmitterNetwork() ? getTransmitterNetwork().getCapacity() : getCapacity();
    }

    @Override
    public boolean isOrphan() {
        return orphaned;
    }

    @Override
    public void setOrphan(boolean nowOrphaned) {
        orphaned = nowOrphaned;
    }
}