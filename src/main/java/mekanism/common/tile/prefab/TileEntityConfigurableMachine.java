package mekanism.common.tile.prefab;

import mekanism.api.IConfigCardAccess;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.util.Direction;

public abstract class TileEntityConfigurableMachine extends TileEntityMekanism implements ISideConfiguration, IConfigCardAccess {

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    public TileEntityConfigurableMachine(IBlockProvider blockProvider) {
        super(blockProvider);
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
}
