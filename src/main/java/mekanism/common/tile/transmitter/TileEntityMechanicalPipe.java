package mekanism.common.tile.transmitter;

import com.mojang.serialization.Codec;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class TileEntityMechanicalPipe extends TileEntityResourceTransmitter<FluidResource, IFluidTank, FluidNetwork, MechanicalPipe> {

    public TileEntityMechanicalPipe(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, Capabilities.FLUID);
    }

    @Override
    protected MechanicalPipe createTransmitter(Holder<Block> blockProvider) {
        return new MechanicalPipe(blockProvider, this);
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.MECHANICAL_PIPE;
    }

    @Override
    protected Codec<FluidResource> resourceCodec() {
        return FluidResource.CODEC;
    }

    @Override
    protected BlockState upgradeResult(BlockState current, BaseTier tier) {
        return BlockStateHelper.copyStateData(current, switch (tier) {
            case BASIC -> MekanismBlocks.BASIC_MECHANICAL_PIPE;
            case ADVANCED -> MekanismBlocks.ADVANCED_MECHANICAL_PIPE;
            case ELITE -> MekanismBlocks.ELITE_MECHANICAL_PIPE;
            case ULTIMATE -> MekanismBlocks.ULTIMATE_MECHANICAL_PIPE;
            default -> null;
        });
    }

    //Methods relating to IComputerTile
    @Override
    public String getComputerName() {
        return getTransmitter().getTier().getBaseTier().getLowerName() + "MechanicalPipe";
    }
    //End methods IComputerTile
}