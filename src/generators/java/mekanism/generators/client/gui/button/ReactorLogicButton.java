package mekanism.generators.client.gui.button;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.button.MekanismButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.util.ResourceLocation;

public class ReactorLogicButton extends MekanismButton {

    private static final ResourceLocation TEXTURE = MekanismGenerators.rl("gui/elements/logic_button.png");
    @Nonnull
    private final TileEntityReactorLogicAdapter tile;
    private final ReactorLogic type;

    public ReactorLogicButton(IGuiWrapper gui, int x, int y, ReactorLogic type, @Nonnull TileEntityReactorLogicAdapter tile, Runnable onPress) {
        super(gui, x, y, 128, 22, "", onPress, (onHover, xAxis, yAxis) -> gui.displayTooltip(type.getDescription(), xAxis, yAxis));
        this.tile = tile;
        this.type = type;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        MekanismRenderer.bindTexture(TEXTURE);
        MekanismRenderer.color(EnumColor.RED);
        blit(this.x, this.y, 0, type == tile.logicType ? 22 : 0, this.width, this.height, 128, 44);
        MekanismRenderer.resetColor();
    }

    public ReactorLogic getType() {
        return this.type;
    }
}