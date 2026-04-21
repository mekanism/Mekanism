package mekanism.common.upgrade;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;

public class MachineUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final IEnergyContainer energyContainer;
    public final int[] progress;
    public final boolean sorting;
    public final EnergyInventorySlot energySlot;
    public final List<IInventorySlot> inputSlots;
    public final List<IInventorySlot> outputSlots;
    public final CompoundTag components;

    //Machine Constructor
    public MachineUpgradeData(HolderLookup.Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int operatingTicks, EnergyInventorySlot energySlot,
          InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components) {
        this(provider, redstone, controlType, energyContainer, new int[]{operatingTicks}, energySlot, Collections.singletonList(inputSlot),
              Collections.singletonList(outputSlot), false, components);
    }

    //Machine Factory Constructor
    public MachineUpgradeData(HolderLookup.Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int[] progress, EnergyInventorySlot energySlot,
          List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting, List<ITileComponent> components) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.energyContainer = energyContainer;
        this.progress = progress;
        this.energySlot = energySlot;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.sorting = sorting;

        //todo - 26.1 - make the constructor take a ProblemReporter.PathElement from the tile
        ProblemReporter.Collector reporter = new ProblemReporter.Collector();
        TagValueOutput output = TagValueOutput.createWithContext(reporter, provider);
        for (ITileComponent component : components) {
            component.write(output);
        }
        reporter.forEach((id, problem) -> Mekanism.logger.warn("Found MachineUpgradeData validation problem in {}: {}", id, problem.description()));
        this.components = output.buildResult();
    }
}