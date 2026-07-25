package mekanism.client.recipe_viewer.emi;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import mekanism.client.gui.GuiMekanism;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

@Mod(value = Mekanism.MODID, dist = Dist.CLIENT, depends = MekanismHooks.EMI_MOD_ID)
public class MekanismEmiScreenHooks {

    private final Object2BooleanMap<Class<?>> emiScreens = new Object2BooleanOpenHashMap<>();

    public MekanismEmiScreenHooks() {
        //Add at lowest to add after any other places we might check
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ScreenEvent.Opening.class, this::guiOpening);
    }

    //Note: This listener is only registered if EMI is loaded
    private void guiOpening(ScreenEvent.Opening event) {
        if (event.getCurrentScreen() instanceof GuiMekanism<?> screen && !screen.switchingToRecipeViewer) {
            //If Emi is loaded, our current screen is a mekanism gui, and we aren't already switching to a recipe viewer: check if the new screen is an Emi recipe screen
            // https://github.com/emilyploszaj/emi/issues/481
            if (isEmiScreen(event.getNewScreen())) {
                //If it is mark on our current screen that we are switching to EMI
                screen.switchingToRecipeViewer = true;
            }
        }
    }

    private boolean isEmiScreen(@Nullable Screen newScreen) {
        return newScreen != null && emiScreens.computeIfAbsent(newScreen.getClass(), (Class<?> cl) -> cl.getName().startsWith("dev.emi.emi"));
    }
}