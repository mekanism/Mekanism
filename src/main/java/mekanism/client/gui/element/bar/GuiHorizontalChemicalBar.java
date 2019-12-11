package mekanism.client.gui.element.bar;

import mekanism.api.chemical.Chemical;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar.ChemicalInfoProvider;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiHorizontalChemicalBar<CHEMICAL extends Chemical<CHEMICAL>> extends GuiHorizontalBar<ChemicalInfoProvider<CHEMICAL>> {

    private static final ResourceLocation BAR = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "large_horizontal_bar.png");
    private static final int texWidth = 160;
    private static final int texHeight = 5;

    public GuiHorizontalChemicalBar(IGuiWrapper gui, ChemicalInfoProvider<CHEMICAL> infoProvider, ResourceLocation def, int x, int y) {
        this(gui, infoProvider, def, x, y, texWidth + 2);
    }

    public GuiHorizontalChemicalBar(IGuiWrapper gui, ChemicalInfoProvider<CHEMICAL> infoProvider, ResourceLocation def, int x, int y, int width) {
        super(AtlasTexture.LOCATION_BLOCKS_TEXTURE, gui, infoProvider, def, x, y, width, texHeight + 2);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        CHEMICAL type = getHandler().getType();
        if (!type.isEmptyType()) {
            int displayInt = (int) (getHandler().getLevel() * (width - 2));
            MekanismRenderer.color(type);
            guiObj.drawTexturedRectFromIcon(x + 1, y + 1, MekanismRenderer.getChemicalTexture(type), displayInt, texHeight);
            MekanismRenderer.resetColor();
        }
    }

    @Override
    public void renderBar() {
        //TODO: Use the bar in GuiHorizontalBar and make it scale to be the correct size
        minecraft.textureManager.bindTexture(BAR);
        //TODO: Stretch it properly
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, texWidth + 2, texHeight + 2);
    }
}