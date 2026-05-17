package mekanism.common.tile.transmitter;

import com.mojang.serialization.Codec;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.ChemicalNetwork;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.ITileRadioactive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityPressurizedTube extends TileEntityResourceTransmitter<ChemicalResource, IChemicalTank, ChemicalNetwork, PressurizedTube> implements ITileRadioactive {

    public TileEntityPressurizedTube(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, Capabilities.CHEMICAL);
    }

    @Override
    protected PressurizedTube createTransmitter(Holder<Block> blockProvider) {
        return new PressurizedTube(blockProvider, this);
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.PRESSURIZED_TUBE;
    }

    @Override
    protected Codec<ChemicalResource> resourceCodec() {
        return ChemicalResource.CODEC;
    }

    @NotNull
    @Override
    protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
        return BlockStateHelper.copyStateData(current, switch (tier) {
            case BASIC -> MekanismBlocks.BASIC_PRESSURIZED_TUBE;
            case ADVANCED -> MekanismBlocks.ADVANCED_PRESSURIZED_TUBE;
            case ELITE -> MekanismBlocks.ELITE_PRESSURIZED_TUBE;
            case ULTIMATE -> MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE;
            default -> null;
        });
    }

    @Override
    public float getRadiationScale() {
        if (!RadiationManager.isGlobalRadiationEnabled()) {
            return 0;
        }
        PressurizedTube tube = getTransmitter();
        if (isRemote()) {
            if (tube.hasTransmitterNetwork()) {
                ChemicalNetwork network = tube.getTransmitterNetwork();
                if (!network.getLastType().isEmpty() && !network.getContainer().isEmpty() && network.getLastType().isRadioactive()) {
                    //Note: This may act as full when the network isn't actually full if there is radioactive stuff
                    // going through it, but it shouldn't matter too much
                    return network.currentScale;
                }
            }
            return 0;
        }
        return tube.getRadiationScale();
    }

    @Override
    public int getRadiationParticleCount() {
        return MathUtils.clampToInt(3 * getRadiationScale());
    }

    //Methods relating to IComputerTile
    @Override
    public String getComputerName() {
        return getTransmitter().getTier().getBaseTier().getLowerName() + "PressurizedTube";
    }
    //End methods IComputerTile
}