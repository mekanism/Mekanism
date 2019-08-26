package mekanism.generators.client.gui.button;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.MekanismButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReactorLogicButton extends MekanismButton {

    @Nonnull
    private final TileEntityReactorLogicAdapter tile;
    private final ResourceLocation resourceLocation;
    private final ReactorLogic type;

    public ReactorLogicButton(int x, int y, ReactorLogic type, @Nonnull TileEntityReactorLogicAdapter tile, ResourceLocation resource, IPressable onPress, IHoverable onHover) {
        super(x, y, 128, 22, "", onPress, onHover);
        this.tile = tile;
        this.type = type;
        this.resourceLocation = resource;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        MekanismRenderer.bindTexture(this.resourceLocation);
        MekanismRenderer.color(EnumColor.RED);
        blit(this.x, this.y, 0, 166 + (type == tile.logicType ? 22 : 0), this.width, this.height);
        MekanismRenderer.resetColor();
    }

    public ReactorLogic getType() {
        return this.type;
    }
}