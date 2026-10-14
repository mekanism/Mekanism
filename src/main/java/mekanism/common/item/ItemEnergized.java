package mekanism.common.item;

import java.util.function.Consumer;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemEnergized extends Item implements ICustomCreativeTabContents {

    public ItemEnergized(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ContainerType.ENERGY.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ContainerType.ENERGY.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public void addItems(ItemDisplayParameters displayParameters, Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ContainerType.ENERGY.getFilledVariant(item, null));
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        //Ignore NBT for energized items causing block break reset
        return !ItemStack.matchesIgnoringComponents(oldStack, newStack, DataComponentType::ignoreSwapAnimation);
    }
}