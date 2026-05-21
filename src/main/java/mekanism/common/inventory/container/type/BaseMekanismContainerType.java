package mekanism.common.inventory.container.type;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

public abstract class BaseMekanismContainerType<CONTAINER extends AbstractContainerMenu, FACTORY> extends MenuType<CONTAINER> {

    protected final FACTORY mekanismConstructor;

    protected BaseMekanismContainerType(FACTORY mekanismConstructor, IContainerFactory<CONTAINER> constructor) {
        super(constructor, FeatureFlags.VANILLA_SET);
        this.mekanismConstructor = mekanismConstructor;
    }
}