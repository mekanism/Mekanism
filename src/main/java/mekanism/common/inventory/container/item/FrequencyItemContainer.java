package mekanism.common.inventory.container.item;

import java.util.Collections;
import java.util.List;
import mekanism.api.security.SecurityMode;
import mekanism.common.component.FrequencyAware;
import mekanism.common.inventory.container.sync.SyncableStreamCodec;
import mekanism.common.inventory.container.sync.SyncableFrequencyList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

public abstract class FrequencyItemContainer<FREQ extends Frequency> extends MekanismItemContainer {

    private List<FREQ> publicCache = Collections.emptyList();
    private List<FREQ> privateCache = Collections.emptyList();
    private List<FREQ> trustedCache = Collections.emptyList();
    @Nullable
    private FREQ freq;

    protected FrequencyItemContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess) {
        super(type, id, inv, hand, itemAccess);
    }

    public InteractionHand getHand() {
        return hand;
    }

    protected abstract FrequencyType<FREQ> getFrequencyType();

    @Nullable
    public FREQ getClientFrequency() {
        return freq;
    }

    @Nullable
    protected FREQ getFrequencyFromStack() {
        DataComponentType<FrequencyAware<FREQ>> frequencyComponent = MekanismDataComponents.getFrequencyComponent(getFrequencyType());
        if (frequencyComponent != null) {
            //Note: It should never be null, but we check just in case
            FrequencyAware<FREQ> frequencyAware = itemAccess.getResource().get(frequencyComponent);
            if (frequencyAware != null) {
                //Start it out at what the value on the stack is
                return frequencyAware.frequency().orElse(null);
            }
        }
        return null;
    }

    public List<FREQ> getPublicCache() {
        return publicCache;
    }

    public List<FREQ> getPrivateCache() {
        return privateCache;
    }

    public List<FREQ> getTrustedCache() {
        return trustedCache;
    }

    private void setFrequency(@Nullable FREQ frequency) {
        this.freq = frequency;
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        FrequencyType<FREQ> frequencyType = getFrequencyType();
        if (getLevel().isClientSide()) {
            //Client side sync handling
            track(SyncableStreamCodec.frequency(frequencyType, this::getClientFrequency, this::setFrequency));
            track(SyncableFrequencyList.create(frequencyType, this::getPublicCache, value -> publicCache = value));
            track(SyncableFrequencyList.create(frequencyType, this::getPrivateCache, value -> privateCache = value));
            track(SyncableFrequencyList.create(frequencyType, this::getTrustedCache, value -> trustedCache = value));
        } else {
            //Server side sync handling
            //Note: It is important these are in the same order as the client side trackers
            track(SyncableStreamCodec.frequency(frequencyType, this::getFrequencyFromStack, this::setFrequency));
            track(SyncableFrequencyList.create(frequencyType, () -> frequencyType.getLookup(null, SecurityMode.PUBLIC).getFrequencies(), value -> publicCache = value));
            track(SyncableFrequencyList.create(frequencyType, () -> frequencyType.getLookup(getPlayerUUID(), SecurityMode.PRIVATE).getFrequencies(), value -> privateCache = value));
            track(SyncableFrequencyList.create(frequencyType, () -> frequencyType.getLookup(getPlayerUUID(), SecurityMode.TRUSTED).getFrequencies(), value -> trustedCache = value));
        }
    }
}