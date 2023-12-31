package mekanism.common.tile.prefab;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityConfigurableMachine extends TileEntityMekanism implements ISideConfiguration {

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;//does not tick!

    public TileEntityConfigurableMachine(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public final TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public final TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public CompoundTag getConfigurationData(Player player) {
        CompoundTag data = super.getConfigurationData(player);
        configComponent.write(data);
        ejectorComponent.write(data);
        return data;
    }

    @Override
    public void setConfigurationData(Player player, CompoundTag data) {
        super.setConfigurationData(player, data);
        configComponent.read(data);
        ejectorComponent.read(data);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        //TODO - 1.20.4: When can this be null?? I don't believe it ever is and we don't check it in other spots
        if (ejectorComponent != null) {
            ejectorComponent.tickServer();
        }
    }
}