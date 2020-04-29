package mekanism.common.inventory.container;

import java.util.Map;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;

public abstract class QIOItemViewerContainer extends MekanismContainer {

    private Map<HashedItem, Long> cachedInventory = new Object2ObjectOpenHashMap<>();

    protected QIOItemViewerContainer(ContainerTypeRegistryObject<?> type, int id, PlayerInventory inv) {
        super(type, id, inv);
    }

    public abstract QIOFrequency getFrequency();

    @Override
    protected void openInventory(@Nonnull PlayerInventory inv) {
        super.openInventory(inv);
        QIOFrequency freq = getFrequency();
        if (!inv.player.world.isRemote() && freq != null) {
            freq.openItemViewer((ServerPlayerEntity) inv.player);
        }
    }

    @Override
    protected void closeInventory(PlayerEntity player) {
        super.closeInventory(player);
        QIOFrequency freq = getFrequency();
        if (!inv.player.world.isRemote() && freq != null) {
            freq.closeItemViewer((ServerPlayerEntity) inv.player);
        }
    }

    public void handleBatchUpdate(Map<HashedItem, Long> itemMap) {
        cachedInventory = itemMap;
    }

    public void handleUpdate(Map<HashedItem, Long> itemMap) {
        itemMap.entrySet().forEach(e -> {
            if (e.getValue() == 0) {
                cachedInventory.remove(e.getKey());
            } else {
                cachedInventory.put(e.getKey(), e.getValue());
            }
        });
    }

    public Map<HashedItem, Long> getQIOInventory() {
        return cachedInventory;
    }
}
