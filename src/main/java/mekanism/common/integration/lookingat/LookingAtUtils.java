package mekanism.common.integration.lookingat;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.ChemicalTankWrapper;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.functions.TriConsumer;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler;
import mekanism.common.entity.EntityRobit;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utils for simplifying the code for interacting with various mods that you look at things for (TOP, and Hwyla)
 */
public class LookingAtUtils {

    public static final ResourceLocation ENERGY = Mekanism.rl("energy");
    public static final ResourceLocation FLUID = Mekanism.rl("fluid");
    public static final ResourceLocation GAS = Mekanism.rl("gas");
    public static final ResourceLocation INFUSE_TYPE = Mekanism.rl("infuse_type");
    public static final ResourceLocation PIGMENT = Mekanism.rl("pigment");
    public static final ResourceLocation SLURRY = Mekanism.rl("slurry");

    private LookingAtUtils() {
    }

    @Nullable
    private static MultiblockData getMultiblock(@NotNull BlockEntity tile) {
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

    public static void addInfo(LookingAtHelper info, @NotNull BlockEntity tile, boolean displayTanks, boolean displayFluidTanks) {
        MultiblockData structure = getMultiblock(tile);
        Optional<IStrictEnergyHandler> energyCapability = CapabilityUtils.getCapability(tile, Capabilities.STRICT_ENERGY, null).resolve();
        if (energyCapability.isPresent()) {
            displayEnergy(info, energyCapability.get());
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the energy of multiblock's when looking at things other than the ports
            displayEnergy(info, structure);
        }
        if (displayTanks) {
            //Fluid - only add it to our own tiles in which we disable the default display for
            if (displayFluidTanks && tile instanceof TileEntityUpdateable) {
                Optional<IFluidHandler> fluidCapability = CapabilityUtils.getCapability(tile, ForgeCapabilities.FLUID_HANDLER, null).resolve();
                if (fluidCapability.isPresent()) {
                    displayFluid(info, fluidCapability.get());
                } else if (structure != null && structure.isFormed()) {
                    //Special handling to allow viewing the fluid in a multiblock when looking at things other than the ports
                    displayFluid(info, structure);
                }
            }
            //Chemicals
            addInfo(tile, structure, Capabilities.GAS_HANDLER, multiblock -> multiblock.getGasTanks(null), info, MekanismLang.GAS, Current.GAS, CurrentType.GAS);
            addInfo(tile, structure, Capabilities.INFUSION_HANDLER, multiblock -> multiblock.getInfusionTanks(null), info, MekanismLang.INFUSE_TYPE, Current.INFUSION, CurrentType.INFUSION);
            addInfo(tile, structure, Capabilities.PIGMENT_HANDLER, multiblock -> multiblock.getPigmentTanks(null), info, MekanismLang.PIGMENT, Current.PIGMENT, CurrentType.PIGMENT);
            addInfo(tile, structure, Capabilities.SLURRY_HANDLER, multiblock -> multiblock.getSlurryTanks(null), info, MekanismLang.SLURRY, Current.SLURRY, CurrentType.SLURRY);
        }
    }

    private static void displayFluid(LookingAtHelper info, IFluidHandler fluidHandler) {
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
                addFluidInfo(info, fluidTank.getFluid(), fluidTank.getCapacity());
            }
        } else {
            //Fallback handling if it is not our fluid handler (probably never gets used)
            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                addFluidInfo(info, fluidHandler.getFluidInTank(tank), fluidHandler.getTankCapacity(tank));
            }
        }
    }

    private static void addFluidInfo(LookingAtHelper info, FluidStack fluidInTank, int capacity) {
        if (!fluidInTank.isEmpty()) {
            info.addText(MekanismLang.LIQUID.translate(fluidInTank));
        }
        info.addFluidElement(fluidInTank, capacity);
    }

    private static void displayEnergy(LookingAtHelper info, IStrictEnergyHandler energyHandler) {
        int containers = energyHandler.getEnergyContainerCount();
        for (int container = 0; container < containers; container++) {
            info.addEnergyElement(energyHandler.getEnergy(container), energyHandler.getMaxEnergy(container));
        }
    }

    @SuppressWarnings("unchecked")
    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>,
          HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void addInfo(BlockEntity tile, @Nullable MultiblockData structure, Capability<HANDLER> capability,
          Function<MultiblockData, List<TANK>> multiBlockToTanks, LookingAtHelper info, ILangEntry langEntry, Current matchingCurrent, CurrentType matchingCurrentType) {
        Optional<HANDLER> cap = CapabilityUtils.getCapability(tile, capability, null).resolve();
        if (cap.isPresent()) {
            HANDLER handler = cap.get();
            if (handler instanceof ProxyChemicalHandler) {
                List<TANK> tanks = ((ProxyChemicalHandler<CHEMICAL, STACK, ?>) handler).getTanksIfMekanism();
                if (!tanks.isEmpty()) {
                    //If there are any tanks add them and then exit, otherwise continue on assuming it is not a mekanism handler that is wrapped
                    for (TANK tank : tanks) {
                        addChemicalTankInfo(info, langEntry, tank, matchingCurrent, matchingCurrentType);
                    }
                    return;
                }
            }
            if (handler instanceof IMekanismChemicalHandler) {
                IMekanismChemicalHandler<CHEMICAL, STACK, TANK> mekHandler = (IMekanismChemicalHandler<CHEMICAL, STACK, TANK>) handler;
                for (TANK tank : mekHandler.getChemicalTanks(null)) {
                    addChemicalTankInfo(info, langEntry, tank, matchingCurrent, matchingCurrentType);
                }
            } else {
                for (int i = 0; i < handler.getTanks(); i++) {
                    addChemicalInfo(info, langEntry, handler.getChemicalInTank(i), handler.getTankCapacity(i));
                }
            }
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the chemicals in a multiblock when looking at things other than the ports
            for (TANK tank : multiBlockToTanks.apply(structure)) {
                addChemicalTankInfo(info, langEntry, tank, matchingCurrent, matchingCurrentType);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void addChemicalTankInfo(
          LookingAtHelper info, ILangEntry langEntry, TANK chemicalTank, Current matchingCurrent, CurrentType matchingCurrentType) {
        if (chemicalTank instanceof ChemicalTankWrapper) {
            MergedChemicalTank mergedTank = ((ChemicalTankWrapper<CHEMICAL, STACK>) chemicalTank).getMergedTank();
            if (mergedTank instanceof MergedTank tank) {
                //If we are also support fluid, only show if we are the correct type
                if (tank.getCurrentType() != matchingCurrentType) {
                    //Skip if the tank is not the correct chemical type (fluid is default for merged tanks when empty)
                    return;
                }
            } else {
                Current current = mergedTank.getCurrent();
                if (current == Current.EMPTY) {
                    if (matchingCurrent != Current.GAS) {
                        //Skip tanks if overall it is empty, and it is not the gas tank
                        return;
                    }
                } else if (current != matchingCurrent) {
                    //Skip if the tank is on the wrong type of chemical
                    return;
                }
            }
        }
        addChemicalInfo(info, langEntry, chemicalTank.getStack(), chemicalTank.getCapacity());
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void addChemicalInfo(LookingAtHelper info, ILangEntry langEntry,
          STACK chemicalInTank, long capacity) {
        if (!chemicalInTank.isEmpty()) {
            info.addText(langEntry.translate(chemicalInTank.getType()));
        }
        info.addChemicalElement(chemicalInTank, capacity);
    }

    public static void appendHwylaTooltip(CompoundTag data, Consumer<Component> textConsumer, TriConsumer<Component, LookingAtElement, ResourceLocation> elementConsumer) {
        if (data.contains(NBTConstants.MEK_DATA, Tag.TAG_LIST)) {
            Component lastText = null;
            //Copy the data we need and have from the server and pass it on to the tooltip rendering
            ListTag list = data.getList(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag elementData = list.getCompound(i);
                LookingAtElement element;
                ResourceLocation name;
                if (elementData.contains(HwylaLookingAtHelper.TEXT, Tag.TAG_STRING)) {
                    Component text = Component.Serializer.fromJson(elementData.getString(HwylaLookingAtHelper.TEXT));
                    if (text != null) {
                        if (lastText != null) {
                            //Fallback to printing the last text
                            textConsumer.accept(lastText);
                        }
                        lastText = text;
                    }
                    continue;
                } else if (elementData.contains(NBTConstants.ENERGY_STORED, Tag.TAG_STRING)) {
                    element = new EnergyElement(FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.ENERGY_STORED), true),
                          FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.MAX), true));
                    name = LookingAtUtils.ENERGY;
                } else if (elementData.contains(NBTConstants.FLUID_STORED, Tag.TAG_COMPOUND)) {
                    element = new FluidElement(FluidStack.loadFluidStackFromNBT(elementData.getCompound(NBTConstants.FLUID_STORED)), elementData.getInt(NBTConstants.MAX));
                    name = LookingAtUtils.FLUID;
                } else if (elementData.contains(HwylaLookingAtHelper.CHEMICAL_STACK, Tag.TAG_COMPOUND)) {
                    ChemicalStack<?> chemicalStack;
                    CompoundTag chemicalData = elementData.getCompound(HwylaLookingAtHelper.CHEMICAL_STACK);
                    if (chemicalData.contains(NBTConstants.GAS_NAME, Tag.TAG_STRING)) {
                        chemicalStack = GasStack.readFromNBT(chemicalData);
                        name = LookingAtUtils.GAS;
                    } else if (chemicalData.contains(NBTConstants.INFUSE_TYPE_NAME, Tag.TAG_STRING)) {
                        chemicalStack = InfusionStack.readFromNBT(chemicalData);
                        name = LookingAtUtils.INFUSE_TYPE;
                    } else if (chemicalData.contains(NBTConstants.PIGMENT_NAME, Tag.TAG_STRING)) {
                        chemicalStack = PigmentStack.readFromNBT(chemicalData);
                        name = LookingAtUtils.PIGMENT;
                    } else if (chemicalData.contains(NBTConstants.SLURRY_NAME, Tag.TAG_STRING)) {
                        chemicalStack = SlurryStack.readFromNBT(chemicalData);
                        name = LookingAtUtils.SLURRY;
                    } else {//Unknown chemical
                        continue;
                    }
                    element = new ChemicalElement(chemicalStack, elementData.getLong(NBTConstants.MAX));
                } else {//Skip, unknown
                    continue;
                }
                elementConsumer.accept(lastText, element, name);
                lastText = null;
            }
            if (lastText != null) {
                textConsumer.accept(lastText);
            }
        }
    }
}