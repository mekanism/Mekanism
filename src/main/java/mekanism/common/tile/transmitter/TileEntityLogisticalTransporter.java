package mekanism.common.tile.transmitter;

import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.network.transmitter.LogisticalTransporter;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TileEntityLogisticalTransporter extends TileEntityLogisticalTransporterBase {

    public static final BlockTintSource TINT_SOURCE = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            TileEntityLogisticalTransporter transporter = WorldUtils.getTileEntity(TileEntityLogisticalTransporter.class, level, pos);
            if (transporter != null) {
                EnumColor renderColor = transporter.getTransmitter().getColor();
                if (renderColor != null) {
                    return renderColor.getPackedColor();
                }
            }
            return -1;
        }
    };

    public TileEntityLogisticalTransporter(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    protected LogisticalTransporter createTransmitter(Holder<Block> blockProvider) {
        return new LogisticalTransporter(blockProvider, this);
    }

    @Override
    public LogisticalTransporter getTransmitter() {
        return (LogisticalTransporter) super.getTransmitter();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.LOGISTICAL_TRANSPORTER;
    }

    @Override
    protected void updateModelData(TransmitterModelData modelData) {
        super.updateModelData(modelData);
        modelData.setHasColor(getTransmitter().getColor() != null);
    }

    @NotNull
    @Override
    protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
        return BlockStateHelper.copyStateData(current, switch (tier) {
            case BASIC -> MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER;
            case ADVANCED -> MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER;
            case ELITE -> MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER;
            case ULTIMATE -> MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER;
            default -> null;
        });
    }
}