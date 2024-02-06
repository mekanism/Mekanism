package mekanism.common.inventory.container.item;

import java.util.Collections;
import java.util.List;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public abstract class FrequencyItemContainer<FREQ extends Frequency> extends MekanismItemContainer {

    private List<FREQ> publicCache = Collections.emptyList();
    private List<FREQ> privateCache = Collections.emptyList();

    protected FrequencyItemContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(type, id, inv, hand, stack);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public FREQ getFrequency() {
        return getFrequencyAware().getFrequency();
    }

    private FrequencyAware<FREQ> getFrequencyAware() {
        return (FrequencyAware<FREQ>) stack.getData(MekanismAttachmentTypes.FREQUENCY_AWARE);
    }

    public List<FREQ> getPublicCache() {
        return publicCache;
    }

    public List<FREQ> getPrivateCache() {
        return privateCache;
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        FrequencyAware<FREQ> frequencyAware = getFrequencyAware();
        FrequencyType<FREQ> frequencyType = frequencyAware.getFrequencyType();
        track(SyncableFrequency.create(frequencyAware));
        if (isRemote()) {
            //Client side sync handling
            track(SyncableFrequencyList.create(frequencyType, this::getPublicCache, value -> publicCache = value));
            track(SyncableFrequencyList.create(frequencyType, this::getPrivateCache, value -> privateCache = value));
        } else {
            //Server side sync handling
            //Note: It is important these are in the same order as the client side trackers
            track(SyncableFrequencyList.create(frequencyType, () -> frequencyType.getManager(null).getFrequencies(), value -> publicCache = value));
            track(SyncableFrequencyList.create(frequencyType, () -> frequencyType.getManager(getPlayerUUID()).getFrequencies(), value -> privateCache = value));
        }
    }
}