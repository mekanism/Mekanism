package mekanism.common.inventory.container.item;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

public abstract class MekanismItemContainer extends MekanismContainer {

    protected MekanismItemContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv) {
        super(type, id, inv);
    }
}