package mekanism.common.tile.prefab;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityConfigurableMachine extends TileEntityMekanism implements ISideConfiguration {

    public TileComponentEjector ejectorComponent;
    public final TileComponentConfig configComponent;//does not tick!

    public TileEntityConfigurableMachine(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
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
    public void readFromStack(ItemStack stack) {
        super.readFromStack(stack);
        //The read methods validate that data is stored
        stack.getData(MekanismAttachmentTypes.SIDE_CONFIG).copyTo(configComponent);
        stack.getData(MekanismAttachmentTypes.EJECTOR).copyTo(ejectorComponent);
    }

    @Override
    public void writeToStack(ItemStack stack) {
        super.writeToStack(stack);
        stack.getData(MekanismAttachmentTypes.SIDE_CONFIG).copyFrom(configComponent);
        stack.getData(MekanismAttachmentTypes.EJECTOR).copyFrom(ejectorComponent);
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