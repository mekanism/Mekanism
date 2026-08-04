package mekanism.client.key;

import mekanism.client.gui.GuiRadialSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class RadialConflictContext implements IKeyConflictContext {

    public static final RadialConflictContext INSTANCE = new RadialConflictContext();

    private RadialConflictContext() {
    }

    @Override
    public boolean isActive() {
        Screen screen = Minecraft.getInstance().gui.screen();
        return screen == null || screen instanceof GuiRadialSelector;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return other == KeyConflictContext.IN_GAME || other == KeyConflictContext.GUI || /*other == KeyConflictContext.GUI_AND_IN_GAME ||*/ other == this;
    }

    //TODO: https://github.com/neoforged/NeoForge/pull/3331 ??
    /*@Override
    public boolean requiresExactKeyModifierNone() {
        return Minecraft.getInstance().gui.screen() instanceof GuiRadialSelector;
    }*/
}