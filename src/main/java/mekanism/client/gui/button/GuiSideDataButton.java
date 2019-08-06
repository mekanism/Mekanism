package mekanism.client.gui.button;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.SideData;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSideDataButton extends GuiButton {

    private final Supplier<SideData> sideDataSupplier;
    private final Supplier<EnumColor> colorSupplier;
    private final ResourceLocation resourceLocation;
    private final int slotPosMapIndex;

    public GuiSideDataButton(int id, int x, int y, ResourceLocation resource, int slotPosMapIndex, Supplier<SideData> sideDataSupplier, Supplier<EnumColor> colorSupplier) {
        super(id, x, y, 14, 14, "");
        this.resourceLocation = resource;
        this.slotPosMapIndex = slotPosMapIndex;
        this.sideDataSupplier = sideDataSupplier;
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            mc.getTextureManager().bindTexture(this.resourceLocation);
            SideData data = sideDataSupplier.get();
            if (data == TileComponentConfig.EMPTY) {
                drawTexturedModalRect(this.x, this.y, 176, 28, this.width, this.height);
            } else {
                EnumColor color = getColor();
                boolean doColor = color != null && color != EnumColor.GREY;
                if (doColor) {
                    MekanismRenderer.color(getColor());
                }
                drawTexturedModalRect(this.x, this.y, 176, this.hovered ? 0 : 14, this.width, this.height);
                if (doColor) {
                    MekanismRenderer.resetColor();
                }
            }
        }
    }

    public int getSlotPosMapIndex() {
        return this.slotPosMapIndex;
    }

    public SideData getSideData() {
        return this.sideDataSupplier.get();
    }

    public EnumColor getColor() {
        return this.colorSupplier.get();
    }
}