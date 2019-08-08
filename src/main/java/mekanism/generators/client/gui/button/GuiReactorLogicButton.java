package mekanism.generators.client.gui.button;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorLogicButton extends Button {

    @Nonnull
    private final TileEntityReactorLogicAdapter tile;
    private final ResourceLocation resourceLocation;
    private final ReactorLogic type;

    public GuiReactorLogicButton(int id, int x, int y, ReactorLogic type, @Nonnull TileEntityReactorLogicAdapter tile, ResourceLocation resource) {
        super(id, x, y, 128, 22, "");
        this.tile = tile;
        this.type = type;
        this.resourceLocation = resource;
    }

    @Override
    public void drawButton(@Nonnull Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            minecraft.getTextureManager().bindTexture(this.resourceLocation);
            MekanismRenderer.color(EnumColor.RED);
            drawTexturedModalRect(this.x, this.y, 0, 166 + (type == tile.logicType ? 22 : 0), this.width, this.height);
            MekanismRenderer.resetColor();
        }
    }

    public ReactorLogic getType() {
        return this.type;
    }
}