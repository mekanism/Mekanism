package mekanism.common.inventory.container.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.frequency.IFrequencyItem;
import mekanism.common.network.PacketFrequencyItemGuiUpdate;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.security.IOwnerItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
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
    protected void openInventory(@Nonnull PlayerInventory inv) {
        super.openInventory(inv);

        if (!inv.player.world.isRemote()) {
            FrequencyIdentity identity = ((IFrequencyItem) stack.getItem()).getFrequency(stack);
            FREQ freq = null;
            if (identity != null) {
                FrequencyManager<FREQ> manager = identity.isPublic() ? getFrequencyType().getManager(null) : getFrequencyType().getManager(inv.player.getUniqueID());
                freq = manager.getFrequency(identity.getKey());
                // if this frequency no longer exists, remove the reference from the stack
                if (freq == null) {
                    ((IFrequencyItem) stack.getItem()).setFrequency(stack, null);
                }
            }
            Mekanism.packetHandler.sendTo(PacketFrequencyItemGuiUpdate.create(hand, getFrequencyType(), inv.player.getUniqueID(), freq), (ServerPlayerEntity) inv.player);
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
