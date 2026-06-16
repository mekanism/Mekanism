package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.common.recipe.ISubRecipeProvider;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public abstract class BaseSubRecipeProvider implements ISubRecipeProvider {

    protected final HolderGetter<Item> items;
    protected final HolderGetter<Fluid> fluids;
    protected final HolderGetter<Chemical> chemicals;

    protected BaseSubRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        this.items = items;
        this.fluids = fluids;
        this.chemicals = chemicals;
    }

    @Override
    public HolderGetter<Item> items() {
        return items;
    }
}