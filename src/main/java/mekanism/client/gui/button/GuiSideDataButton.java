package mekanism.client.gui.button;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.SideData;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSideDataButton extends Button {

    private final Supplier<SideData> sideDataSupplier;
    private final Supplier<EnumColor> colorSupplier;
    private final ResourceLocation resourceLocation;
    private final int slotPosMapIndex;

    public GuiSideDataButton(int x, int y, ResourceLocation resource, int slotPosMapIndex, Supplier<SideData> sideDataSupplier, Supplier<EnumColor> colorSupplier, IPressable pressable) {
        super(x, y, 14, 14, "", pressable);
        this.resourceLocation = resource;
        this.slotPosMapIndex = slotPosMapIndex;
        this.sideDataSupplier = sideDataSupplier;
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            MekanismRenderer.bindTexture(this.resourceLocation);
            SideData data = sideDataSupplier.get();
            if (data == TileComponentConfig.EMPTY) {
                drawTexturedModalRect(this.x, this.y, 176, 28, this.width, this.height);
            } else {
                EnumColor color = getColor();
                boolean doColor = color != null && color != EnumColor.GREY;
                if (doColor) {
                    MekanismRenderer.color(getColor());
                }
                drawTexturedModalRect(this.x, this.y, 176, isMouseOver(mouseX, mouseY) ? 0 : 14, this.width, this.height);
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