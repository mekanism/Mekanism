package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.Translation;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiEnergyInfo extends GuiElement {

    private final IInfoHandler infoHandler;

    public GuiEnergyInfo(IInfoHandler handler, IGuiWrapper gui, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiEnergyInfo.png"), gui, def);
        infoHandler = handler;
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + 138, 26, 26);
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= -21 && xAxis <= -3 && yAxis >= 142 && yAxis <= 160;
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 138, 0, 0, 26, 26);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        if (inBounds(xAxis, yAxis)) {
            List<ITextComponent> info = new ArrayList<>(infoHandler.getInfo());
            info.add(TextComponentUtil.build(Translation.of("mekanism.gui.unit"), ": ", MekanismConfig.current().general.energyUnit.val()));
            displayTooltips(info, xAxis, yAxis);
        }
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (button == 0 && inBounds(xAxis, yAxis)) {
            MekanismConfig.current().general.energyUnit.set(EnergyType.values()[(MekanismConfig.current().general.energyUnit.val().ordinal() + 1) % EnergyType.values().length]);
        }
    }
}