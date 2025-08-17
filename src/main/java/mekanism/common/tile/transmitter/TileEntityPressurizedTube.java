package mekanism.common.tile.transmitter;

import com.mojang.serialization.DataResult;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.DynamicChemicalHandler;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager;
import mekanism.common.content.network.ChemicalNetwork;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.ITileRadioactive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityPressurizedTube extends TileEntityTransmitter implements IComputerTile, ITileRadioactive {

    private final ChemicalHandlerManager chemicalHandlerManager;

    public TileEntityPressurizedTube(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        Predicate<@Nullable Direction> canExtract = getExtractPredicate();
        Predicate<@Nullable Direction> canInsert = getInsertPredicate();
        addCapabilityResolver(chemicalHandlerManager = new ChemicalHandlerManager(direction -> {
            PressurizedTube tube = getTransmitter();
            if (direction != null && (tube.getConnectionTypeRaw(direction) == ConnectionType.NONE) || tube.isRedstoneActivated()) {
                //If we actually have a side, and our connection type on that side is none, or we are currently activated by redstone,
                // then return that we have no tanks
                return Collections.emptyList();
            }
            return tube.getChemicalTanks(direction);
        }, new DynamicChemicalHandler(this::getChemicalTanks, canExtract, canInsert, null)));
    }

    @Override
    protected PressurizedTube createTransmitter(Holder<Block> blockProvider) {
        return new PressurizedTube(blockProvider, this);
    }

    @Override
    public PressurizedTube getTransmitter() {
        return (PressurizedTube) super.getTransmitter();
    }

    @Override
    protected void onUpdateServer() {
        getTransmitter().pullFromAcceptors();
        super.onUpdateServer();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.PRESSURIZED_TUBE;
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

    @NotNull
    @Override
    public CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        //Note: We add the stored information to the initial update tag and not to the one we sync on side changes which uses getReducedUpdateTag
        CompoundTag updateTag = super.getUpdateTag(provider);
        if (getTransmitter().hasTransmitterNetwork()) {
            ChemicalNetwork network = getTransmitter().getTransmitterNetwork();
            if (!network.lastChemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                DataResult<Tag> encoded = Chemical.HOLDER_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), network.lastChemical);
                if (encoded.isSuccess()) {
                    updateTag.put(SerializationConstants.CHEMICAL, encoded.getOrThrow());
                } else {
                    encoded.ifError(error -> Mekanism.logger.warn("Failed to encode last chemical: {}", error.message()));
                }
            }
            updateTag.putFloat(SerializationConstants.SCALE, network.currentScale);
        }
        return updateTag;
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
                if (!network.lastChemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY) && !network.getChemicalTank().isEmpty() && network.lastChemical.value().isRadioactive()) {
                    //Note: This may act as full when the network isn't actually full if there is radioactive stuff
                    // going through it, but it shouldn't matter too much
                    return network.currentScale;
                }
            }
        } else {
            IChemicalTank gasTank = tube.getChemicalTank();
            if (!gasTank.isEmpty() && gasTank.getStack().isRadioactive()) {
                return gasTank.getStored() / (float) gasTank.getCapacity();
            }
        }
        return 0;
    }

    @Override
    public int getRadiationParticleCount() {
        return MathUtils.clampToInt(3 * getRadiationScale());
    }

    private List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        return chemicalHandlerManager.getContainers(side);
    }

    @Override
    public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
        super.sideChanged(side, old, type);
        if (type == ConnectionType.NONE) {
            //We no longer have a capability, invalidate it, which will also notify the level
            invalidateCapability(Capabilities.CHEMICAL.block(), side);
        } else if (old == ConnectionType.NONE) {
            //Notify any listeners to our position that we now do have a capability
            //Note: We don't invalidate our impls because we know they are already invalid, so we can short circuit setting them to null from null
            invalidateCapabilities();
        }
    }

    @Override
    public void redstoneChanged(boolean powered) {
        super.redstoneChanged(powered);
        if (powered) {
            //The transmitter now is powered by redstone and previously was not
            //Note: While at first glance the below invalidation may seem over aggressive, it is not actually that aggressive as
            // if a cap has not been initialized yet on a side then invalidating it will just NO-OP
            invalidateCapabilityAll(Capabilities.CHEMICAL.block());
        } else {
            //Notify any listeners to our position that we now do have a capability
            //Note: We don't invalidate our impls because we know they are already invalid, so we can short circuit setting them to null from null
            invalidateCapabilities();
        }
    }

    //Methods relating to IComputerTile
    @Override
    public String getComputerName() {
        return getTransmitter().getTier().getBaseTier().getLowerName() + "PressurizedTube";
    }

    @ComputerMethod
    ChemicalStack getBuffer() {
        return getTransmitter().getBufferWithFallback();
    }

    @ComputerMethod
    long getCapacity() {
        PressurizedTube tube = getTransmitter();
        return tube.hasTransmitterNetwork() ? tube.getTransmitterNetwork().getCapacity() : tube.getCapacity();
    }

    @ComputerMethod
    long getNeeded() {
        return getCapacity() - getBuffer().getAmount();
    }

    @ComputerMethod
    double getFilledPercentage() {
        return getBuffer().getAmount() / (double) getCapacity();
    }
    //End methods IComputerTile
}