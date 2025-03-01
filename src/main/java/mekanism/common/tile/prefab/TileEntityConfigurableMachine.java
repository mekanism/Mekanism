package mekanism.common.tile.prefab;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityConfigurableMachine extends TileEntityMekanism implements ISideConfiguration {

    public TileComponentEjector ejectorComponent;
    public final TileComponentConfig configComponent;//does not tick!

    public TileEntityConfigurableMachine(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        configComponent = new TileComponentConfig(this, Attribute.getOrThrow(blockProvider, AttributeSideConfig.class).supportedTypes());
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
    public CompoundTag getConfigurationData(HolderLookup.Provider provider, Player player) {
        CompoundTag data = super.getConfigurationData(provider, player);
        configComponent.write(data, provider);
        ejectorComponent.write(data, provider);
        return data;
    }

    @Override
    public void setConfigurationData(HolderLookup.Provider provider, Player player, CompoundTag data) {
        super.setConfigurationData(provider, player, data);
        configComponent.read(data, provider);
        ejectorComponent.read(data, provider);
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        ejectorComponent.tickServer();
        return sendUpdatePacket;
    }
}