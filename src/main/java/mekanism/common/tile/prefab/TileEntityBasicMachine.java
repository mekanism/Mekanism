package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import mekanism.api.IConfigCardAccess;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public abstract class TileEntityBasicMachine<RECIPE extends MekanismRecipe> extends TileEntityOperationalMachine<RECIPE> implements ISideConfiguration, IConfigCardAccess {

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    public TileEntityBasicMachine(IBlockProvider blockProvider, int baseTicksRequired) {
        super(blockProvider, baseTicksRequired);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public Direction getOrientation() {
        return getDirection();
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        return configComponent.isCapabilityDisabled(capability, side) || super.isCapabilityDisabled(capability, side);
    }
}