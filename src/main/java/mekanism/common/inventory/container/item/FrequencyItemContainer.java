package mekanism.common.inventory.container.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.IOwnerItem;
import mekanism.common.network.PacketGuiItemDataRequest;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public abstract class FrequencyItemContainer<FREQ extends Frequency> extends MekanismItemContainer {

    private FREQ frequency;

    private List<FREQ> publicCache = new ArrayList<>();
    private List<FREQ> privateCache = new ArrayList<>();

    protected FrequencyItemContainer(ContainerTypeRegistryObject<?> type, int id, @Nullable PlayerInventory inv, Hand hand, ItemStack stack) {
        super(type, id, inv, hand, stack);
    }

    public void handleCacheUpdate(List<FREQ> publicCache, List<FREQ> privateCache, FREQ frequency) {
        this.publicCache = publicCache;
        this.privateCache = privateCache;
        this.frequency = frequency;
    }

    public abstract FrequencyType<FREQ> getFrequencyType();

    @Override
    public void openInventory(@Nonnull PlayerInventory inv) {
        super.openInventory(inv);

        if (inv.player.world.isRemote()) {
            Mekanism.packetHandler.sendToServer(PacketGuiItemDataRequest.frequencyList(hand));
        }
    }

    public UUID getOwnerUUID() {
        return ((IOwnerItem) stack.getItem()).getOwnerUUID(stack);
    }

    public String getOwnerUsername() {
        return MekanismClient.clientUUIDMap.get(((IOwnerItem) stack.getItem()).getOwnerUUID(stack));
    }

    public FREQ getFrequency() {
        return frequency;
    }

    public List<FREQ> getPublicCache() {
        return publicCache;
    }

    public List<FREQ> getPrivateCache() {
        return privateCache;
    }
}
