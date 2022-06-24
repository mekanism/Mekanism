package mekanism.common.content.filter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;

public interface IMaterialFilter<FILTER extends IMaterialFilter<FILTER>> extends IFilter<FILTER> {

    default Material getMaterial() {
        return Block.byItem(getMaterialItem().getItem()).defaultBlockState().getMaterial();
    }

    @NotNull
    ItemStack getMaterialItem();

    void setMaterialItem(@NotNull ItemStack stack);

    @Override
    default boolean hasFilter() {
        return !getMaterialItem().isEmpty();
    }
}