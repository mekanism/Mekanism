package mekanism.common.tile.prefab;

import mekanism.api.IConfigCardAccess;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public abstract class TileEntityConfigurableMachine extends TileEntityMekanism implements ISideConfiguration, IConfigCardAccess {

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;//does not tick!

    public TileEntityConfigurableMachine(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public CompoundNBT getConfigurationData(PlayerEntity player) {
        CompoundNBT data = super.getConfigurationData(player);
        getConfig().write(data);
        getEjector().write(data);
        return data;
    }

    @Override
    public void setConfigurationData(PlayerEntity player, CompoundNBT data) {
        super.setConfigurationData(player, data);
        getConfig().read(data);
        getEjector().read(data);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (ejectorComponent != null) {
            ejectorComponent.tickServer();
        }
    }
}