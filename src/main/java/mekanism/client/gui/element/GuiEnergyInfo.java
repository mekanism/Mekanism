package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiEnergyInfo extends GuiElement {

    private final IInfoHandler infoHandler;

    public GuiEnergyInfo(IInfoHandler handler, IGuiWrapper gui, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "energy_info.png"), gui, def, -26, 138, 26, 26);
        infoHandler = handler;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        List<ITextComponent> info = new ArrayList<>(infoHandler.getInfo());
        info.add(TextComponentUtil.build(Translation.of("gui.mekanism.unit"), ": ", MekanismConfig.general.energyUnit.get()));
        displayTooltips(info, mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        MekanismConfig.general.energyUnit.set(EnergyType.values()[(MekanismConfig.general.energyUnit.get().ordinal() + 1) % EnergyType.values().length]);
    }
}