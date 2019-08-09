package mekanism.generators.client.gui.button;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorLogicButton extends Button {

    @Nonnull
    private final TileEntityReactorLogicAdapter tile;
    private final ResourceLocation resourceLocation;
    private final ReactorLogic type;

    public GuiReactorLogicButton(int x, int y, ReactorLogic type, @Nonnull TileEntityReactorLogicAdapter tile, ResourceLocation resource, IPressable onPress) {
        super(x, y, 128, 22, "", onPress);
        this.tile = tile;
        this.type = type;
        this.resourceLocation = resource;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            MekanismRenderer.bindTexture(this.resourceLocation);
            MekanismRenderer.color(EnumColor.RED);
            drawTexturedModalRect(this.x, this.y, 0, 166 + (type == tile.logicType ? 22 : 0), this.width, this.height);
            MekanismRenderer.resetColor();
        }
    }

    public ReactorLogic getType() {
        return this.type;
    }
}