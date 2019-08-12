package mekanism.client.gui;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.IModGuiFactory;

@OnlyIn(Dist.CLIENT)
public class ConfigGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft instance) {
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public Screen createConfigGui(Screen parentScreen) {
        return new GuiMekanismConfig(parentScreen);
    }
}