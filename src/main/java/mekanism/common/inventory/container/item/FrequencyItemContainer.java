package mekanism.common.inventory.container.item;

import java.util.Collections;
import java.util.List;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public abstract class FrequencyItemContainer<FREQ extends Frequency> extends MekanismItemContainer {

    private List<FREQ> publicCache = Collections.emptyList();
    private List<FREQ> privateCache = Collections.emptyList();
    private FREQ selectedFrequency;

    protected FrequencyItemContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(type, id, inv, hand, stack);
    }

    public abstract FrequencyType<FREQ> getFrequencyType();

    public InteractionHand getHand() {
        return hand;
    }

    public FREQ getFrequency() {
        return selectedFrequency;
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
        if (isRemote()) {
            //Client side sync handling
            track(SyncableFrequency.create(this::getFrequency, value -> selectedFrequency = value));
            track(SyncableFrequencyList.create(this::getPublicCache, value -> publicCache = value));
            track(SyncableFrequencyList.create(this::getPrivateCache, value -> privateCache = value));
        } else {
            //Server side sync handling
            //Note: It is important these are in the same order as the client side trackers
            track(SyncableFrequency.create(() -> {
                IFrequencyItem frequencyItem = (IFrequencyItem) stack.getItem();
                //Note: We "cache" the last selected frequency server side to simplify a bit of lookups for the PortableTeleporter container trackers
                if (frequencyItem.hasFrequency(stack)) {
                    selectedFrequency = (FREQ) frequencyItem.getFrequency(stack);
                    if (selectedFrequency == null) {
                        // if this frequency no longer exists, remove the reference from the stack
                        frequencyItem.setFrequency(stack, null);
                    }
                } else {
                    selectedFrequency = null;
                }
                return selectedFrequency;
            }, value -> selectedFrequency = value));
            track(SyncableFrequencyList.create(() -> getFrequencyType().getManager(null).getFrequencies(), value -> publicCache = value));
            track(SyncableFrequencyList.create(() -> getFrequencyType().getManager(getPlayerUUID()).getFrequencies(), value -> privateCache = value));
        }
    }
}