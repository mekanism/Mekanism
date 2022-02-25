package mekanism.common.inventory.container.item;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SeismicReaderContainer extends MekanismItemContainer implements IEmptyContainer {

    public SeismicReaderContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(MekanismContainerTypes.SEISMIC_READER, id, inv, hand, stack);
    }
}