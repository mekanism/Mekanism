package mekanism.common.inventory.container.item;

import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class DictionaryContainer extends MekanismItemContainer {

    public DictionaryContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(MekanismContainerTypes.DICTIONARY, id, inv, hand, stack);
    }

    @Override
    protected int getInventoryYOffset() {
        return super.getInventoryYOffset() + 5;
    }
}