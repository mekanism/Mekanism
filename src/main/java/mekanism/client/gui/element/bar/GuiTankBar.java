package mekanism.client.gui.element.bar;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiTankBar.TankInfoProvider;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.network.to_server.PacketDropperUse.DropperAction;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiTankBar<STACK> extends GuiBar<TankInfoProvider<STACK>> implements IJEIIngredientHelper {

    public GuiTankBar(IGuiWrapper gui, TankInfoProvider<STACK> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(AtlasTexture.LOCATION_BLOCKS, gui, infoProvider, x, y, width, height, horizontal);
    }

    protected abstract boolean isEmpty(STACK stack);

    @Nullable
    protected abstract TankType getType(STACK stack);

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        STACK stored = getHandler().getStack();
        if (isEmpty(stored)) {
            super.renderToolTip(matrix, mouseX, mouseY);
        } else {
            displayTooltips(matrix, getTooltip(stored), mouseX, mouseY);
        }
    }

    protected List<ITextComponent> getTooltip(STACK stack) {
        List<ITextComponent> tooltips = new ArrayList<>();
        ITextComponent tooltip = getHandler().getTooltip();
        if (tooltip != null) {
            tooltips.add(tooltip);
        }
        return tooltips;
    }

    protected abstract void applyRenderColor(STACK stack);

    protected abstract TextureAtlasSprite getIcon(STACK stack);

    @Override
    protected void renderBarOverlay(MatrixStack matrix, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        STACK stored = getHandler().getStack();
        if (!isEmpty(stored)) {
            int displayInt = (int) (handlerLevel * ((horizontal ? width : height) - 2));
            if (displayInt > 0) {
                applyRenderColor(stored);
                TextureAtlasSprite icon = getIcon(stored);
                if (horizontal) {
                    drawTiledSprite(matrix, x + 1, y + 1, height - 2, displayInt, height - 2, icon, TilingDirection.DOWN_RIGHT);
                } else {
                    drawTiledSprite(matrix, x + 1, y + 1, height - 2, width - 2, displayInt, icon, TilingDirection.DOWN_RIGHT);
                }
                MekanismRenderer.resetColor();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            ItemStack stack = Minecraft.getInstance().player.inventory.getCarried();
            if (gui() instanceof GuiMekanismTile && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                TankType tankType = getType(getHandler().getStack());
                if (tankType != null) {
                    int index = getHandler().getTankIndex();
                    if (index != -1) {
                        DropperAction action;
                        if (button == 0) {
                            action = Screen.hasShiftDown() ? DropperAction.DUMP_TANK : DropperAction.FILL_DROPPER;
                        } else {
                            action = DropperAction.DRAIN_DROPPER;
                        }
                        Mekanism.packetHandler.sendToServer(new PacketDropperUse(((GuiMekanismTile<?, ?>) gui()).getTileEntity().getBlockPos(), action, tankType, index));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    @Override
    public Object getIngredient(double mouseX, double mouseY) {
        STACK stack = getHandler().getStack();
        return isEmpty(stack) ? null : stack;
    }

    //Note the GuiBar.IBarInfoHandler is needed, as it cannot compile and resolve just IBarInfoHandler
    public interface TankInfoProvider<STACK> extends GuiBar.IBarInfoHandler {

        @Nonnull
        STACK getStack();

        int getTankIndex();
    }
}