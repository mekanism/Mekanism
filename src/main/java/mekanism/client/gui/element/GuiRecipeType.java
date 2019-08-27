package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiRecipeType extends GuiTileEntityElement<TileEntityFactory> {

    public GuiRecipeType(IGuiWrapper gui, TileEntityFactory tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "recipe_type.png"), gui, def, tile, 176, 70, 26, 63);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(x, y, 0, 0, width, height);
        int displayInt = tileEntity.getScaledRecipeProgress(15);
        guiObj.drawTexturedRect(width + 5, height + 24, width, 0, 10, displayInt);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    //TODO: Figure out what the point of the below was
    /*@Override
    public void preMouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inBounds(mouseX, mouseY)) {
            offsetX(26);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inBounds(mouseX, mouseY)) {
            offsetX(-26);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }*/
}