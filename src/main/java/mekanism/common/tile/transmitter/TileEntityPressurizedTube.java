package mekanism.common.tile.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicGasHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicInfusionHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicPigmentHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicSlurryHandler;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.GasHandlerManager;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.InfusionHandlerManager;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.PigmentHandlerManager;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager.SlurryHandlerManager;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.ITileRadioactive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityPressurizedTube extends TileEntityTransmitter implements IComputerTile, ITileRadioactive {

    private static final Collection<BlockCapability<?, @Nullable Direction>> CAPABILITIES = Set.of(
          Capabilities.GAS.block(),
          Capabilities.INFUSION.block(),
          Capabilities.PIGMENT.block(),
          Capabilities.SLURRY.block()
    );

    private final GasHandlerManager gasHandlerManager;
    private final InfusionHandlerManager infusionHandlerManager;
    private final PigmentHandlerManager pigmentHandlerManager;
    private final SlurryHandlerManager slurryHandlerManager;

    public TileEntityPressurizedTube(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        Predicate<@Nullable Direction> canExtract = getExtractPredicate();
        Predicate<@Nullable Direction> canInsert = getInsertPredicate();
        addCapabilityResolver(gasHandlerManager = new GasHandlerManager(getHolder(BoxedPressurizedTube::getGasTanks),
              new DynamicGasHandler(this::getGasTanks, canExtract, canInsert, null)));
        addCapabilityResolver(infusionHandlerManager = new InfusionHandlerManager(getHolder(BoxedPressurizedTube::getInfusionTanks),
              new DynamicInfusionHandler(this::getInfusionTanks, canExtract, canInsert, null)));
        addCapabilityResolver(pigmentHandlerManager = new PigmentHandlerManager(getHolder(BoxedPressurizedTube::getPigmentTanks),
              new DynamicPigmentHandler(this::getPigmentTanks, canExtract, canInsert, null)));
        addCapabilityResolver(slurryHandlerManager = new SlurryHandlerManager(getHolder(BoxedPressurizedTube::getSlurryTanks),
              new DynamicSlurryHandler(this::getSlurryTanks, canExtract, canInsert, null)));
    }

    @Override
    protected BoxedPressurizedTube createTransmitter(IBlockProvider blockProvider) {
        return new BoxedPressurizedTube(blockProvider, this);
    }

    @Override
    public BoxedPressurizedTube getTransmitter() {
        return (BoxedPressurizedTube) super.getTransmitter();
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
            BoxedChemicalNetwork network = getTransmitter().getTransmitterNetwork();
            updateTag.put(NBTConstants.BOXED_CHEMICAL, network.lastChemical.saveOptional(provider));
            updateTag.putFloat(NBTConstants.SCALE, network.currentScale);
        }
        return updateTag;
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
    IChemicalTankHolder<CHEMICAL, STACK, TANK> getHolder(BiFunction<BoxedPressurizedTube, Direction, List<TANK>> tankFunction) {
        return direction -> {
            BoxedPressurizedTube tube = getTransmitter();
            if (direction != null && (tube.getConnectionTypeRaw(direction) == ConnectionType.NONE) || tube.isRedstoneActivated()) {
                //If we actually have a side, and our connection type on that side is none, or we are currently activated by redstone,
                // then return that we have no tanks
                return Collections.emptyList();
            }
            return tankFunction.apply(tube, direction);
        };
    }

    @Override
    public float getRadiationScale() {
        if (IRadiationManager.INSTANCE.isRadiationEnabled()) {
            BoxedPressurizedTube tube = getTransmitter();
            if (isRemote()) {
                if (tube.hasTransmitterNetwork()) {
                    BoxedChemicalNetwork network = tube.getTransmitterNetwork();
                    if (!network.lastChemical.isEmpty() && !network.isTankEmpty() && network.lastChemical.getChemical().isRadioactive()) {
                        //Note: This may act as full when the network isn't actually full if there is radioactive stuff
                        // going through it, but it shouldn't matter too much
                        return network.currentScale;
                    }
                }
            } else {
                IGasTank gasTank = tube.getGasTank();
                if (!gasTank.isEmpty() && gasTank.getStack().isRadioactive()) {
                    return gasTank.getStored() / (float) gasTank.getCapacity();
                }
            }
        }
        return 0;
    }

    @Override
    public int getRadiationParticleCount() {
        return MathUtils.clampToInt(3 * getRadiationScale());
    }

    private List<IGasTank> getGasTanks(@Nullable Direction side) {
        return gasHandlerManager.getContainers(side);
    }

    private List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        return infusionHandlerManager.getContainers(side);
    }

    private List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
        return pigmentHandlerManager.getContainers(side);
    }

    private List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
        return slurryHandlerManager.getContainers(side);
    }

    @Override
    public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
        super.sideChanged(side, old, type);
        if (type == ConnectionType.NONE) {
            //We no longer have a capability, invalidate it, which will also notify the level
            invalidateCapabilities(CAPABILITIES, side);
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
            invalidateCapabilitiesAll(CAPABILITIES);
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
    ChemicalStack<?> getBuffer() {
        return getTransmitter().getBufferWithFallback().getChemicalStack();
    }

    @ComputerMethod
    long getCapacity() {
        BoxedPressurizedTube tube = getTransmitter();
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