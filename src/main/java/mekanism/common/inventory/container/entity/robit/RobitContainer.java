package mekanism.common.inventory.container.entity.robit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

public abstract class RobitContainer extends MekanismEntityContainer<EntityRobit> {

    protected RobitContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, EntityRobit entity) {
        super(type, id, inv, entity);
    }

    @Override
    protected void openInventory(@Nonnull PlayerInventory inv) {
        entity.openInventory(inv.player);
    }

    @Override
    protected void closeInventory(PlayerEntity player) {
        entity.closeInventory(player);
    }
}