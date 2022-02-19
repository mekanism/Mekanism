package mekanism.common.content.filter;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;

public interface IMaterialFilter<FILTER extends IMaterialFilter<FILTER>> extends IFilter<FILTER> {

    default Material getMaterial() {
        return Block.byItem(getMaterialItem().getItem()).defaultBlockState().getMaterial();
    }

    @Nonnull
    ItemStack getMaterialItem();

    void setMaterialItem(@Nonnull ItemStack stack);

    @Override
    default boolean hasFilter() {
        return !getMaterialItem().isEmpty();
    }
}