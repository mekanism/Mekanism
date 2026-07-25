package mekanism.client.recipe_viewer.jei;

import mekanism.client.gui.GuiMekanism;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mezz.jei.api.runtime.IRecipesGui;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Mekanism.MODID, dist = Dist.CLIENT, depends = MekanismHooks.JEI_MOD_ID)
public class MekanismJeiScreenHooks {

    public MekanismJeiScreenHooks() {
        //Add at lowest to add after any other places we might check
        //Add at low instead of lowest so that it runs before the more expensive EMI check if both emi and jei are present
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ScreenEvent.Opening.class, this::guiOpening);
    }

    //Note: This listener is only registered if JEI is loaded
    private void guiOpening(ScreenEvent.Opening event) {
        //If JEI is loaded and our current screen is a mekanism gui, check if the new screen is a JEI recipe screen
        if (event.getCurrentScreen() instanceof GuiMekanism<?> screen && event.getNewScreen() instanceof IRecipesGui) {
            //If it is mark on our current screen that we are switching to JEI
            screen.switchingToRecipeViewer = true;
        }
    }
}