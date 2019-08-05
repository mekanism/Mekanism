package mekanism.common.transmitters;

import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;

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
        if (world().isRemote && theNetwork != null) {
            theNetwork.addTransmitter(this);
        }
        setRequestsUpdate();
    }

    @Override
    public boolean hasTransmitterNetwork() {
        return !isOrphan() && getTransmitterNetwork() != null;
    }

    @Override
    public int getTransmitterNetworkSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getSize() : 0;
    }

    @Override
    public int getTransmitterNetworkAcceptorSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getAcceptorSize() : 0;
    }

    @Override
    public String getTransmitterNetworkNeeded() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getNeededInfo() : "No Network";
    }

    @Override
    public String getTransmitterNetworkFlow() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getFlowInfo() : "No Network";
    }

    @Override
    public String getTransmitterNetworkBuffer() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getStoredInfo() : "No Network";
    }

    @Override
    public double getTransmitterNetworkCapacity() {
        //This isn't *fully* accurate as the fluid and gas networks only actually support up to max int currently
        return hasTransmitterNetwork() ? getTransmitterNetwork().getCapacityAsDouble() : getCapacity();
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