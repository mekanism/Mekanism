package mekanism.common.integration.lookingat;

import java.util.List;
import java.util.Map.Entry;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.BlockData;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.capabilities.merged.ChemicalTankWrapper;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler;
import mekanism.common.content.network.ChemicalNetwork;
import mekanism.common.entity.EntityRobit;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utils for simplifying the code for interacting with various mods that you look at things for (TOP, and Hwyla)
 */
public class LookingAtUtils {

    public static final ResourceLocation ENERGY = Mekanism.rl("energy");
    public static final ResourceLocation FLUID = Mekanism.rl("fluid");
    public static final ResourceLocation CHEMICAL = Mekanism.rl("chemical");

    private LookingAtUtils() {
    }

    @Nullable
    private static MultiblockData getMultiblock(@Nullable BlockEntity tile) {
        if (tile instanceof IMultiblock<?> multiblock) {
            return multiblock.getMultiblock();
        } else if (tile instanceof IStructuralMultiblock multiblock) {
            for (Entry<MultiblockManager<?>, Structure> entry : multiblock.getStructureMap().entrySet()) {
                if (entry.getKey() != null) {
                    //TODO: Figure out if the structure map is supposed to be able to have nulls in it (in which handling it like this is correct)
                    // if it is not meant to have nulls then we should modify how Structure#getManager handles things
                    Structure s = entry.getValue();
                    if (s.isValid()) {
                        return s.getMultiblockData();
                    }
                }
            }
        }
        return null;
    }

    public static void addInfo(LookingAtHelper info, @NotNull Entity entity) {
        if (entity instanceof EntityRobit robit) {
            displayEnergy(info, robit);
        }
    }

    public static void addInfoOrRedirect(LookingAtHelper info, Level level, BlockPos pos, BlockState state, @Nullable BlockEntity tile, boolean displayTanks, boolean displayFluidTanks) {
        if (tile instanceof TileEntityBoundingBlock boundingBlock) {
            //If we are a bounding block that has a position set, redirect the check to the main location
            tile = boundingBlock.getMainTile(pos);
            if (tile == null) {
                //If there is no tile where the bounding block thinks the main tile is, exit
                return;
            }
            pos = tile.getBlockPos();
            state = tile.getBlockState();
        }
        addInfo(info, level, pos, state, tile, displayTanks, displayFluidTanks);
    }

    private static void addInfo(LookingAtHelper info, Level level, BlockPos pos, BlockState state, @Nullable BlockEntity tile, boolean displayTanks, boolean displayFluidTanks) {
        if (tile != null) {
            BlockData blockData = tile.components().get(MekanismDataComponents.BLOCK_DATA.value());
            if (blockData != null) {
                blockData.addToTooltip(info::addText);
            }
            if (tile instanceof TileEntityBin bin && bin.getBinSlot().isLocked()) {
                info.addText(MekanismLang.LOCKED.translateColored(EnumColor.AQUA, EnumColor.GRAY, bin.getBinSlot().getLockStack()));
            }
            if (tile instanceof TileEntityQIORedstoneAdapter adapter) {
                ItemStack itemType = adapter.getItemType();
                if (!itemType.isEmpty()) {
                    info.addText(itemType.getHoverName());
                    ILangEntry match = adapter.isInverted() ? MekanismLang.GENERIC_LESS_THAN : MekanismLang.GENERIC_GREATER_EQUAL;
                    info.addText(match.translate(MekanismLang.QIO_TRIGGER_COUNT, TextUtils.format(adapter.getCount())));
                    info.addText(MekanismLang.QIO_FUZZY_MODE.translate(adapter.getFuzzyMode()));
                }
            }
        }
        MultiblockData structure = getMultiblock(tile);
        IStrictEnergyHandler energyCapability = Capabilities.STRICT_ENERGY.getCapabilityIfLoaded(level, pos, state, tile, null);
        if (energyCapability != null) {
            displayEnergy(info, energyCapability);
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the energy of multiblock's when looking at things other than the ports
            displayEnergy(info, structure);
        }
        if (displayTanks) {
            //Fluid - only add it to our own tiles in which we disable the default display for
            if (displayFluidTanks && tile instanceof TileEntityUpdateable) {
                IFluidHandler fluidCapability = Capabilities.FLUID.getCapabilityIfLoaded(level, pos, state, tile, null);
                if (fluidCapability != null) {
                    FluidStack fallback = FluidStack.EMPTY;
                    if (tile instanceof TileEntityMechanicalPipe pipe && pipe.getTransmitter().hasTransmitterNetwork()) {
                        fallback = pipe.getTransmitter().getTransmitterNetwork().lastFluid;
                    }
                    displayFluid(info, fluidCapability, fallback);
                } else if (structure != null && structure.isFormed()) {
                    //Special handling to allow viewing the fluid in a multiblock when looking at things other than the ports
                    displayFluid(info, structure, FluidStack.EMPTY);
                }
            }
            //Chemicals
            addInfo(level, pos, state, tile, structure, info);
        }
    }

    private static void displayFluid(LookingAtHelper info, IFluidHandler fluidHandler, FluidStack fallback) {
        if (fluidHandler instanceof IMekanismFluidHandler mekFluidHandler) {
            for (IExtendedFluidTank fluidTank : mekFluidHandler.getFluidTanks(null)) {
                if (fluidTank instanceof FluidTankWrapper wrapper) {
                    MergedTank mergedTank = wrapper.getMergedTank();
                    CurrentType currentType = mergedTank.getCurrentType();
                    if (currentType != CurrentType.EMPTY && currentType != CurrentType.FLUID) {
                        //Skip if the tank is on a chemical
                        continue;
                    }
                }
                addFluidInfo(info, fluidTank.getFluid(), fluidTank.getCapacity(), fallback);
            }
        } else {
            //Fallback handling if it is not our fluid handler (probably never gets used)
            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                addFluidInfo(info, fluidHandler.getFluidInTank(tank), fluidHandler.getTankCapacity(tank), fallback);
            }
        }
    }

    private static void addFluidInfo(LookingAtHelper info, FluidStack fluidInTank, int capacity, FluidStack fallback) {
        if (!fluidInTank.isEmpty()) {
            info.addText(MekanismLang.LIQUID.translate(fluidInTank));
        } else if (!fallback.isEmpty()) {
            info.addText(MekanismLang.LIQUID.translate(fallback));
        }
        info.addFluidElement(new FluidElement(fluidInTank, capacity));
    }

    private static void displayEnergy(LookingAtHelper info, IStrictEnergyHandler energyHandler) {
        int containers = energyHandler.getEnergyContainerCount();
        for (int container = 0; container < containers; container++) {
            info.addEnergyElement(new EnergyElement(energyHandler.getEnergy(container), energyHandler.getMaxEnergy(container)));
        }
    }
    
    private static void addInfo(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity tile, @Nullable MultiblockData structure, LookingAtHelper info) {
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapabilityIfLoaded(level, pos, state, tile, null);
        if (handler != null) {
            Holder<Chemical> fallback = MekanismAPI.EMPTY_CHEMICAL_HOLDER;
            if (tile instanceof TileEntityPressurizedTube tube && tube.getTransmitter().hasTransmitterNetwork()) {
                ChemicalNetwork network = tube.getTransmitter().getTransmitterNetwork();
                if (!network.lastChemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                    fallback = network.lastChemical;
                }
            }
            if (handler instanceof ProxyChemicalHandler) {
                List<IChemicalTank> tanks = ((ProxyChemicalHandler) handler).getTanksIfMekanism();
                if (!tanks.isEmpty()) {
                    //If there are any tanks add them and then exit, otherwise continue on assuming it is not a mekanism handler that is wrapped
                    for (IChemicalTank tank : tanks) {
                        addChemicalTankInfo(info, tank, fallback);
                    }
                    return;
                }
            }
            if (handler instanceof IMekanismChemicalHandler mekHandler) {
                for (IChemicalTank tank : mekHandler.getChemicalTanks(null)) {
                    addChemicalTankInfo(info, tank, fallback);
                }
            } else {
                for (int i = 0; i < handler.getChemicalTanks(); i++) {
                    addChemicalInfo(info, handler.getChemicalInTank(i), handler.getChemicalTankCapacity(i), fallback);
                }
            }
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the chemicals in a multiblock when looking at things other than the ports
            for (IChemicalTank tank : structure.getChemicalTanks(null)) {
                addChemicalTankInfo(info, tank, MekanismAPI.EMPTY_CHEMICAL_HOLDER);
            }
        }
    }

    private static void addChemicalTankInfo(LookingAtHelper info, IChemicalTank chemicalTank, Holder<Chemical> fallback) {
        if (chemicalTank instanceof ChemicalTankWrapper tankWrapper) {
            MergedTank tank = tankWrapper.getMergedTank();
            //If we are also support fluid, only show if we are the correct type
            if (tank.getCurrentType() != CurrentType.CHEMICAL) {
                //Skip if the tank is not the correct chemical type (fluid is default for merged tanks when empty)
                return;
            }
        }
        addChemicalInfo(info, chemicalTank.getStack(), chemicalTank.getCapacity(), fallback);
    }

    private static void addChemicalInfo(LookingAtHelper info, ChemicalStack chemicalInTank, long capacity, Holder<Chemical> fallback) {
        if (!chemicalInTank.isEmpty()) {
            info.addText(MekanismLang.CHEMICAL.translate(chemicalInTank));
        } else if (!fallback.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            info.addText(MekanismLang.CHEMICAL.translate(fallback));
        }
        info.addChemicalElement(new ChemicalElement(chemicalInTank, capacity));
    }
}