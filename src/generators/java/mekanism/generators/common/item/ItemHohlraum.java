package mekanism.generators.common.item;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.item.interfaces.IChemicalItem;
import mekanism.common.util.StorageUtils;
import mekanism.generators.common.registries.GeneratorsDataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemHohlraum extends Item implements IChemicalItem {

    public ItemHohlraum(Properties properties) {
        super(properties.stacksTo(1).component(GeneratorsDataComponents.REACTION_STARTER, Unit.INSTANCE));
    }

    @Override
    public ResourceKey<Chemical> getChemicalType() {
        return ChemicalIds.FUSION_FUEL;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ContainerType.CHEMICAL.getRGBDurabilityForDisplay(stack);
    }
}