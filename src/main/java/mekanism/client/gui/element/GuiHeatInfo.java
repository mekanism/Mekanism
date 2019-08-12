package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TempType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiHeatInfo extends GuiElement {

    private final IInfoHandler infoHandler;

    public GuiHeatInfo(IInfoHandler handler, IGuiWrapper gui, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiHeatInfo.png"), gui, def);
        infoHandler = handler;
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + 138, 26, 26);
    }

    @Override
    protected boolean inBounds(double xAxis, double yAxis) {
        return xAxis >= -21 && xAxis <= -3 && yAxis >= 116 && yAxis <= 134;
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 112, 0, 0, 26, 26);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        if (inBounds(xAxis, yAxis)) {
            List<ITextComponent> info = new ArrayList<>(infoHandler.getInfo());
            info.add(TextComponentUtil.build(Translation.of("mekanism.gui.unit"), ": ", MekanismConfig.current().general.tempUnit.val()));
            displayTooltips(info, xAxis, yAxis);
        }
    }

    @Override
    public boolean preMouseClicked(double mouseX, double mouseY, int button) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inBounds(mouseX, mouseY)) {
            MekanismConfig.current().general.tempUnit.set(TempType.values()[(MekanismConfig.current().general.tempUnit.val().ordinal() + 1) % TempType.values().length]);
        }
    }
}