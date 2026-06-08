package mekanism.common.upgrade.transmitter;

import mekanism.api.resource.IResourceContainer;
import mekanism.common.lib.transmitter.ConnectionType;
import net.neoforged.neoforge.transfer.resource.Resource;

public class ResourceTransmitterUpgradeData<RESOURCE extends Resource> extends TransmitterUpgradeData {

    public final IResourceContainer<RESOURCE> buffer;

    public ResourceTransmitterUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, IResourceContainer<RESOURCE> buffer) {
        super(redstoneReactive, connectionTypes);
        this.buffer = buffer;
    }
}