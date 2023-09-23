package mekanism.common.integration.computer;

import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileRedstone;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public enum MethodRestriction implements Predicate<Object> {
    /**
     * No restrictions
     */
    NONE(ConstantPredicates.alwaysTrue()),
    /**
     * Handler is a directional tile that is actually directional.
     */
    DIRECTIONAL(handler -> handler instanceof ITileDirectional directional && directional.isDirectional()),
    /**
     * Handler is an energy handler that can handle energy.
     */
    ENERGY(handler -> handler instanceof IMekanismStrictEnergyHandler energyHandler && energyHandler.canHandleEnergy()),
    /**
     * Handler is a multiblock that can expose the multiblock.
     */
    MULTIBLOCK(handler -> handler instanceof TileEntityMultiblock<?> multiblock && multiblock.exposesMultiblockToComputer()),
    /**
     * Handler is a tile that can support redstone.
     */
    REDSTONE_CONTROL(handler -> handler instanceof ITileRedstone redstone && redstone.supportsRedstone()),
    /**
     * Handler is a tile that has comparator support.
     */
    COMPARATOR(handler -> handler instanceof IComparatorSupport comparatorSupport && comparatorSupport.supportsComparator());

    private final Predicate<Object> validator;

    MethodRestriction(Predicate<Object> validator) {
        this.validator = validator;
    }

    @Override
    public boolean test(@Nullable Object handler) {
        return validator.test(handler);
    }
}
