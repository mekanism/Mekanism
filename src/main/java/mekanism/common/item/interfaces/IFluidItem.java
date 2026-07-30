package mekanism.common.item.interfaces;

import java.util.function.Consumer;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import net.minecraft.core.Holder;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

@FunctionalInterface
public interface IFluidItem extends ICustomCreativeTabContents {

    Holder<Fluid> getFluidType();

    @Override
    default void addItems(ItemDisplayParameters displayParameters, Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ContainerType.FLUID.getFilledVariant(item, getFluidType(), null));
    }
}