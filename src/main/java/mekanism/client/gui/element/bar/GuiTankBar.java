package mekanism.client.gui.element.bar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiTankBar.TankInfoProvider;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.network.to_server.PacketDropperUse.DropperAction;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class GuiTankBar<STACK> extends GuiBar<TankInfoProvider<STACK>> implements IRecipeViewerIngredientHelper {

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    public GuiTankBar(IGuiWrapper gui, TankInfoProvider<STACK> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(TextureAtlas.LOCATION_BLOCKS, gui, infoProvider, x, y, width, height, horizontal);
    }

    protected abstract boolean isEmpty(STACK stack);

    @Nullable
    protected abstract TankType getType(STACK stack);

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        STACK stored = getHandler().getStack();
        if (isEmpty(stored)) {
            super.updateTooltip(mouseX, mouseY);
        } else {
            List<Component> info = getTooltip(stored);
            if (!info.equals(lastInfo)) {
                lastInfo = info;
                lastTooltip = TooltipUtils.create(info);
            }
            setTooltip(lastTooltip);
        }
    }

    protected List<Component> getTooltip(STACK stack) {
        List<Component> tooltips = new ArrayList<>();
        Component tooltip = getHandler().getTooltip();
        if (tooltip != null) {
            tooltips.add(tooltip);
        }
        return tooltips;
    }

    protected abstract void applyRenderColor(GuiGraphics guiGraphics, STACK stack);

    protected abstract TextureAtlasSprite getIcon(STACK stack);

    @Override
    protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        STACK stored = getHandler().getStack();
        if (!isEmpty(stored)) {
            int displayInt = (int) (handlerLevel * ((horizontal ? width : height) - 2));
            if (displayInt > 0) {
                applyRenderColor(guiGraphics, stored);
                TextureAtlasSprite icon = getIcon(stored);
                if (horizontal) {
                    drawTiledSprite(guiGraphics, relativeX + 1, relativeY + 1, height - 2, displayInt, height - 2, icon, TilingDirection.DOWN_RIGHT);
                } else {
                    drawTiledSprite(guiGraphics, relativeX + 1, relativeY + 1, height - 2, width - 2, displayInt, icon, TilingDirection.DOWN_RIGHT);
                }
                MekanismRenderer.resetColor(guiGraphics);
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        ItemStack stack = gui().getCarriedItem();
        if (gui() instanceof GuiMekanismTile<?, ?> gui && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
            TankType tankType = getType(getHandler().getStack());
            if (tankType != null) {
                int index = getHandler().getTankIndex();
                if (index != -1) {
                    DropperAction action;
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        action = Screen.hasShiftDown() ? DropperAction.DUMP_TANK : DropperAction.FILL_DROPPER;
                    } else { //GLFW.GLFW_MOUSE_BUTTON_RIGHT
                        action = DropperAction.DRAIN_DROPPER;
                    }
                    PacketUtils.sendToServer(new PacketDropperUse(gui.getTileEntity().getBlockPos(), action, tankType, index));
                }
            }
        }
    }

    @Override
    public boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        STACK stack = getHandler().getStack();
        return isEmpty(stack) ? Optional.empty() : Optional.of(stack);
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        return new Rect2i(getX() + 1, getY() + 1, width - 2, height - 2);
    }

    //Note the GuiBar.IBarInfoHandler is needed, as it cannot compile and resolve just IBarInfoHandler
    public interface TankInfoProvider<STACK> extends GuiBar.IBarInfoHandler {

        @NotNull
        STACK getStack();

        int getTankIndex();
    }
}