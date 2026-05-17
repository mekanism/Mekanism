package mekanism.common.integration.lookingat;

import java.util.List;
import java.util.Map.Entry;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.LargeResourceStack;
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
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utils for simplifying the code for interacting with various mods that you look at things for (TOP, and Hwyla)
 */
public class LookingAtUtils {

    public static final Identifier ENERGY = Mekanism.rl("energy");
    public static final Identifier FLUID = Mekanism.rl("fluid");
    public static final Identifier CHEMICAL = Mekanism.rl("chemical");

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
                blockData.addToTooltip(TooltipContext.EMPTY, info::addText, TooltipFlag.NORMAL, /*unused*/DataComponentMap.EMPTY);
            }
            if (tile instanceof TileEntityBin bin && bin.getBinSlot().isLocked()) {
                info.addText(MekanismLang.LOCKED.translateColored(EnumColor.AQUA, EnumColor.GRAY, bin.getBinSlot().getLockType()));
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
            for (IEnergyContainer container : structure.getEnergyContainers()) {
                info.addEnergyElement(new EnergyElement(container.getEnergy(), container.getCapacity()));
            }
        }
        if (displayTanks) {
            //Fluid - only add it to our own tiles in which we disable the default display for
            if (displayFluidTanks && tile instanceof TileEntityUpdateable) {
                ResourceHandler<FluidResource> fluidCapability = Capabilities.FLUID.getCapabilityIfLoaded(level, pos, state, tile, null);
                if (fluidCapability != null) {
                    FluidResource fallback = FluidResource.EMPTY;
                    if (tile instanceof TileEntityMechanicalPipe pipe && pipe.getTransmitter().hasTransmitterNetwork()) {
                        fallback = pipe.getTransmitter().getTransmitterNetwork().getLastType();
                    }
                    displayFluid(info, fluidCapability, fallback);
                } else if (structure != null && structure.isFormed()) {
                    //Special handling to allow viewing the fluid in a multiblock when looking at things other than the ports
                    displayFluid(info, structure.getFluidTanks(), FluidResource.EMPTY);
                }
            }
            //Chemicals
            addInfo(level, pos, state, tile, structure, info);
        }
    }

    private static void displayFluid(LookingAtHelper info, ResourceHandler<FluidResource> fluidHandler, FluidResource fallback) {
        if (fluidHandler instanceof IMekanismResourceHandler<FluidResource, ?> mekFluidHandler) {
            //TODO - 26.1: Re-evaluate this. I don't think it currently works, but if we make it so that proxy resource handler implements IMekanismResourceHandler
            // then maybe it has a chance?
            displayFluid(info, (List<IFluidTank>) mekFluidHandler.getContainers(), fallback);
        } else {
            //Fallback handling if it is not our fluid handler (probably never gets used)
            for (int tank = 0, size = fluidHandler.size(); tank < size; tank++) {
                FluidResource resource = fluidHandler.getResource(tank);
                addFluidInfo(info, resource, fluidHandler.getAmountAsLong(tank), fluidHandler.getCapacityAsLong(tank, resource), fallback);
            }
        }
    }

    private static void displayFluid(LookingAtHelper info, List<IFluidTank> fluidTanks, FluidResource fallback) {
        for (IFluidTank fluidTank : fluidTanks) {
            if (fluidTank instanceof FluidTankWrapper wrapper) {
                MergedTank mergedTank = wrapper.getMergedTank();
                CurrentType currentType = mergedTank.getCurrentType();
                if (currentType != CurrentType.EMPTY && currentType != CurrentType.FLUID) {
                    //Skip if the tank is on a chemical
                    continue;
                }
            }
            FluidResource storedType = fluidTank.getResource();
            addFluidInfo(info, storedType, fluidTank.amountAsLong(), fluidTank.capacityAsLong(storedType), fallback);
        }
    }

    private static void addFluidInfo(LookingAtHelper info, FluidResource fluidType, long stored, long capacity, FluidResource fallback) {
        if (!fluidType.isEmpty()) {
            info.addText(MekanismLang.LIQUID.translate(fluidType));
        } else if (!fallback.isEmpty()) {
            info.addText(MekanismLang.LIQUID.translate(fallback));
        }
        info.addFluidElement(new FluidElement(new LargeResourceStack<>(fluidType, stored), capacity));
    }

    private static void displayEnergy(LookingAtHelper info, IStrictEnergyHandler energyHandler) {
        int containers = energyHandler.size();
        for (int container = 0; container < containers; container++) {
            info.addEnergyElement(new EnergyElement(energyHandler.getAmountAsLong(container), energyHandler.getCapacityAsLong(container)));
        }
    }

    private static void addInfo(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity tile, @Nullable MultiblockData structure, LookingAtHelper info) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapabilityIfLoaded(level, pos, state, tile, null);
        if (handler != null) {
            ChemicalResource fallback = ChemicalResource.EMPTY;
            if (tile instanceof TileEntityPressurizedTube tube && tube.getTransmitter().hasTransmitterNetwork()) {
                ChemicalNetwork network = tube.getTransmitter().getTransmitterNetwork();
                if (!network.getLastType().isEmpty()) {
                    fallback = network.getLastType();
                }
            }
            if (handler instanceof IMekanismResourceHandler<ChemicalResource, ?> mekHandler) {
                for (IChemicalTank tank : (List<IChemicalTank>) mekHandler.getContainers()) {
                    addChemicalTankInfo(info, tank, fallback);
                }
            } else {
                for (int i = 0, size = handler.size(); i < size; i++) {
                    ChemicalResource resource = handler.getResource(i);
                    addChemicalInfo(info, resource, handler.getAmountAsLong(i), handler.getCapacityAsLong(i, resource), fallback);
                }
            }
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the chemicals in a multiblock when looking at things other than the ports
            for (IChemicalTank tank : structure.getChemicalTanks()) {
                addChemicalTankInfo(info, tank, ChemicalResource.EMPTY);
            }
        }
    }

    private static void addChemicalTankInfo(LookingAtHelper info, IChemicalTank chemicalTank, ChemicalResource fallback) {
        if (chemicalTank instanceof ChemicalTankWrapper tankWrapper) {
            MergedTank tank = tankWrapper.getMergedTank();
            //If we are also support fluid, only show if we are the correct type
            if (tank.getCurrentType() != CurrentType.CHEMICAL) {
                //Skip if the tank is not the correct chemical type (fluid is default for merged tanks when empty)
                return;
            }
        }
        ChemicalResource resource = chemicalTank.getResource();
        addChemicalInfo(info, resource, chemicalTank.amountAsLong(), chemicalTank.capacityAsLong(resource), fallback);
    }

    private static void addChemicalInfo(LookingAtHelper info, ChemicalResource chemicalType, long stored, long capacity, ChemicalResource fallback) {
        if (!chemicalType.isEmpty()) {
            info.addText(MekanismLang.CHEMICAL.translate(chemicalType));
        } else if (!fallback.isEmpty()) {
            info.addText(MekanismLang.CHEMICAL.translate(fallback));
        }
        info.addChemicalElement(new ChemicalElement(new LargeResourceStack<>(chemicalType, stored), capacity));
    }
}