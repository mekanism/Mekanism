package mekanism.common.integration.lookingat;

import java.util.List;
import java.util.Map.Entry;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.BlockData;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.capabilities.merged.ChemicalTankWrapper;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.capabilities.proxy.ProxyResourceHandler;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
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
            displayEnergy(info, robit.getEnergyContainer());
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
                ItemResource itemType = adapter.getItemType();
                if (!itemType.isEmpty()) {
                    info.addText(itemType.getHoverName());
                    ILangEntry match = adapter.isInverted() ? MekanismLang.GENERIC_LESS_THAN : MekanismLang.GENERIC_GREATER_EQUAL;
                    info.addText(match.translate(MekanismLang.QIO_TRIGGER_COUNT, TextUtils.format(adapter.getCount())));
                    info.addText(MekanismLang.QIO_FUZZY_MODE.translate(adapter.getFuzzyMode()));
                }
            }
        }
        MultiblockData structure = getMultiblock(tile);
        EnergyHandler energyCapability = Capabilities.ENERGY.getCapabilityIfLoaded(level, pos, state, tile, null);
        if (energyCapability != null) {
            displayEnergy(info, energyCapability);
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the energy of multiblock's when looking at things other than the ports
            IEnergyContainer container = structure.getEnergyContainer();
            if (container != null) {
                info.addEnergyElement(new EnergyElement(container.getAmountAsLong(), container.getCapacityAsLong()));
            }
        }
        if (displayTanks) {
            //Fluid - only add it to our own tiles in which we disable the default display for
            if (displayFluidTanks && tile instanceof TileEntityUpdateable) {
                ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapabilityIfLoaded(level, pos, state, tile, null);
                if (fluidHandler != null) {
                    FluidResource fallback = FluidResource.EMPTY;
                    if (tile instanceof TileEntityMechanicalPipe pipe && pipe.getTransmitter().hasTransmitterNetwork()) {
                        fallback = pipe.getTransmitter().getTransmitterNetwork().getLastType();
                    }
                    if (fluidHandler instanceof ProxyResourceHandler<FluidResource, ?> proxiedHandler) {
                        addFluidTanks(info, proxiedHandler.getProxiedContainers(), fallback);
                    } else {
                        //Fallback handling if it is not our fluid handler (probably never gets used)
                        for (int tank = 0, size = fluidHandler.size(); tank < size; tank++) {
                            FluidResource resource = fluidHandler.getResource(tank);
                            addFluidInfo(info, resource, fluidHandler.getAmountAsLong(tank), fluidHandler.getCapacityAsLong(tank, resource), fallback);
                        }
                    }
                } else if (structure != null && structure.isFormed()) {
                    //Special handling to allow viewing the fluid in a multiblock when looking at things other than the ports
                    addFluidTanks(info, structure.getFluidTanks(), FluidResource.EMPTY);
                }
            }
            //Chemicals
            addChemicalInfo(level, pos, state, tile, structure, info);
        }
    }

    private static void addFluidTanks(LookingAtHelper info, List<? extends IResourceContainer<FluidResource>> fluidTanks, FluidResource fallback) {
        for (IResourceContainer<FluidResource> fluidTank : fluidTanks) {
            if (fluidTank instanceof FluidTankWrapper wrapper && wrapper.getMergedTank().getCurrentType() == CurrentType.CHEMICAL) {
                //Skip if the tank is on a chemical
                continue;
            }
            FluidResource storedType = fluidTank.resource();
            addFluidInfo(info, storedType, fluidTank.amountAsLong(), fluidTank.capacityAsLong(storedType), fallback);
        }
    }

    private static void addFluidInfo(LookingAtHelper info, FluidResource fluidType, long stored, long capacity, FluidResource fallback) {
        if (!fluidType.isEmpty()) {
            info.addText(MekanismLang.LIQUID.translate(fluidType));
        } else if (!fallback.isEmpty()) {
            info.addText(MekanismLang.LIQUID.translate(fallback));
        }
        info.addFluidElement(new FluidElement(fluidType, stored, capacity));
    }

    private static void displayEnergy(LookingAtHelper info, EnergyHandler energyHandler) {
        info.addEnergyElement(new EnergyElement(energyHandler.getAmountAsLong(), energyHandler.getCapacityAsLong()));
    }

    private static void addChemicalInfo(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity tile, @Nullable MultiblockData structure, LookingAtHelper info) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapabilityIfLoaded(level, pos, state, tile, null);
        if (handler != null) {
            ChemicalResource fallback = ChemicalResource.EMPTY;
            if (tile instanceof TileEntityPressurizedTube tube && tube.getTransmitter().hasTransmitterNetwork()) {
                ChemicalNetwork network = tube.getTransmitter().getTransmitterNetwork();
                if (!network.getLastType().isEmpty()) {
                    fallback = network.getLastType();
                }
            }
            if (handler instanceof ProxyResourceHandler<ChemicalResource, ?> proxiedHandler) {
                addChemicalTanks(info, proxiedHandler.getProxiedContainers(), fallback);
            } else {
                for (int i = 0, size = handler.size(); i < size; i++) {
                    ChemicalResource resource = handler.getResource(i);
                    addChemicalInfo(info, resource, handler.getAmountAsLong(i), handler.getCapacityAsLong(i, resource), fallback);
                }
            }
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the chemicals in a multiblock when looking at things other than the ports
            addChemicalTanks(info, structure.getChemicalTanks(), ChemicalResource.EMPTY);
        }
    }

    private static void addChemicalTanks(LookingAtHelper info, List<? extends IResourceContainer<ChemicalResource>> chemicalTanks, ChemicalResource fallback) {
        for (IResourceContainer<ChemicalResource> tank : chemicalTanks) {
            if (tank instanceof ChemicalTankWrapper tankWrapper && tankWrapper.getMergedTank().getCurrentType() != CurrentType.CHEMICAL) {
                //Skip if the tank is not displaying chemicals
                continue;
            }
            ChemicalResource resource = tank.resource();
            addChemicalInfo(info, resource, tank.amountAsLong(), tank.capacityAsLong(resource), fallback);
        }
    }

    private static void addChemicalInfo(LookingAtHelper info, ChemicalResource chemicalType, long stored, long capacity, ChemicalResource fallback) {
        if (!chemicalType.isEmpty()) {
            info.addText(MekanismLang.CHEMICAL.translate(chemicalType));
        } else if (!fallback.isEmpty()) {
            info.addText(MekanismLang.CHEMICAL.translate(fallback));
        }
        info.addChemicalElement(new ChemicalElement(chemicalType, stored, capacity));
    }
}