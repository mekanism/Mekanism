package mekanism.common.integration.lookingat;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.component.BlockData;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.ResourceContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.capabilities.merged.ChemicalTankWrapper;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.capabilities.proxy.ProxyResourceHandler;
import mekanism.common.entity.EntityRobit;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IMultiblockContents;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.tile.transmitter.TileEntityResourceTransmitter;
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
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

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

    public static void addInfo(LookingAtHelper info, Entity entity) {
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
            displayEnergy(info, structure.getEnergyContainer());
        }
        if (displayTanks) {
            //Fluid - only add it to our own tiles in which we disable the default display for
            if (displayFluidTanks && tile instanceof TileEntityUpdateable) {
                addResourceInfo(level, pos, state, tile, structure, info, ContainerType.FLUID, IMultiblockContents::getFluidTanks, MekanismLang.LIQUID, FluidElement::new);
            }
            //Chemicals
            addResourceInfo(level, pos, state, tile, structure, info, ContainerType.CHEMICAL, IMultiblockContents::getChemicalTanks, MekanismLang.CHEMICAL, ChemicalElement::new);
        }
    }

    private static void displayEnergy(LookingAtHelper info, @Nullable EnergyHandler energyHandler) {
        if (energyHandler != null) {
            info.addEnergyElement(new EnergyElement(energyHandler.getAmountAsLong(), energyHandler.getCapacityAsLong()));
        }
    }

    private static <RESOURCE extends Resource> void addResourceInfo(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity tile,
          @Nullable MultiblockData structure, LookingAtHelper info, ResourceContainerType<RESOURCE, ?> containerType,
          Function<IMultiblockContents, List<? extends IResourceContainer<RESOURCE>>> getMultiblockContainers, ILangEntry langEntry,
          ResourceElementCreator<RESOURCE> creator) {
        ResourceHandler<RESOURCE> handler = containerType.capability().getCapabilityIfLoaded(level, pos, state, tile, null);
        if (handler != null) {
            RESOURCE fallback = containerType.emptyResource();
            if (tile instanceof TileEntityResourceTransmitter<?, ?, ?, ?> transmitter && transmitter.getTransmitter().hasTransmitterNetwork()) {
                fallback = containerType.asResourceOrEmpty(transmitter.getTransmitter().getTransmitterNetworkNN().getLastType());
            }
            if (handler instanceof ProxyResourceHandler<RESOURCE, ?> proxiedHandler) {
                addResourceTanks(info, proxiedHandler.getProxiedContainers(), fallback, langEntry, creator);
            } else {
                for (int i = 0, size = handler.size(); i < size; i++) {
                    RESOURCE resource = handler.getResource(i);
                    addResourceInfo(info, resource, handler.getAmountAsLong(i), handler.getCapacityAsLong(i, resource), fallback, langEntry, creator);
                }
            }
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the contents in a multiblock when looking at things other than the ports
            addResourceTanks(info, getMultiblockContainers.apply(structure), containerType.emptyResource(), langEntry, creator);
        }
    }

    private static <RESOURCE extends Resource> void addResourceTanks(LookingAtHelper info, List<? extends IResourceContainer<RESOURCE>> chemicalTanks, RESOURCE fallback,
          ILangEntry langEntry, ResourceElementCreator<RESOURCE> creator) {
        for (IResourceContainer<RESOURCE> tank : chemicalTanks) {
            if (tank instanceof FluidTankWrapper wrapper && wrapper.getMergedTank().getCurrentType() == CurrentType.CHEMICAL) {
                //Skip if the tank is on a chemical
                continue;
            } else if (tank instanceof ChemicalTankWrapper tankWrapper && tankWrapper.getMergedTank().getCurrentType() != CurrentType.CHEMICAL) {
                //Skip if the tank is not displaying chemicals
                continue;
            }
            RESOURCE resource = tank.resource();
            addResourceInfo(info, resource, tank.amountAsLong(), tank.capacityAsLong(resource), fallback, langEntry, creator);
        }
    }

    private static <RESOURCE extends Resource> void addResourceInfo(LookingAtHelper info, RESOURCE type, long stored, long capacity, RESOURCE fallback,
          ILangEntry langEntry, ResourceElementCreator<RESOURCE> creator) {
        if (!type.isEmpty()) {
            info.addText(langEntry.translate(type));
        } else if (!fallback.isEmpty()) {
            info.addText(langEntry.translate(fallback));
        }
        info.addElement(creator.create(type, stored, capacity));
    }

    @FunctionalInterface
    private interface ResourceElementCreator<RESOURCE extends Resource> {

        ResourceElement<RESOURCE> create(RESOURCE resource, long stored, long capacity);
    }
}