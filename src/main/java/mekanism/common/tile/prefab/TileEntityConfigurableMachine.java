package mekanism.common.tile.prefab;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void writeConfigurationData(ValueOutput output, Player player) {
        super.writeConfigurationData(output, player);
        configComponent.write(output);
        ejectorComponent.write(output);
    }

    @Override
    public void setConfigurationData(ValueInput input, Player player) {
        super.setConfigurationData(input, player);
        configComponent.read(input);
        ejectorComponent.read(input);
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        ejectorComponent.tickServer();
        return sendUpdatePacket;
    }
}