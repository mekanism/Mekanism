package mekanism.common.tier;

import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import net.minecraft.util.ResourceLocation;

public enum FactoryTier implements ITier {
    BASIC(3, new ResourceLocation(Mekanism.MODID, "gui/factory/GuiBasicFactory.png"), MachineType.BASIC_FACTORY),
    ADVANCED(5, new ResourceLocation(Mekanism.MODID, "gui/factory/GuiAdvancedFactory.png"), MachineType.ADVANCED_FACTORY),
    ELITE(7, new ResourceLocation(Mekanism.MODID, "gui/factory/GuiEliteFactory.png"), MachineType.ELITE_FACTORY);

    public final MachineType machineType;
    public final int processes;
    public final ResourceLocation guiLocation;
    private final BaseTier baseTier;

    FactoryTier(int process, ResourceLocation gui, MachineType machineTypeIn) {
        processes = process;
        guiLocation = gui;
        machineType = machineTypeIn;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }
}