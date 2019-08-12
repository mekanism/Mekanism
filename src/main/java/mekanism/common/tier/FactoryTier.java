package mekanism.common.tier;

import mekanism.common.Mekanism;
import net.minecraft.util.ResourceLocation;

public enum FactoryTier implements ITier {
    BASIC(3, new ResourceLocation(Mekanism.MODID, "gui/factory/GuiBasicFactory.png")),
    ADVANCED(5, new ResourceLocation(Mekanism.MODID, "gui/factory/GuiAdvancedFactory.png")),
    ELITE(7, new ResourceLocation(Mekanism.MODID, "gui/factory/GuiEliteFactory.png"));

    public final int processes;
    public final ResourceLocation guiLocation;
    private final BaseTier baseTier;

    FactoryTier(int process, ResourceLocation gui) {
        processes = process;
        guiLocation = gui;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }
}