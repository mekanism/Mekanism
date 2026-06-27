package mekanism.client.gui.element.bar;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.IResourceContainer;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiTankBar.ResourceTankInfoProvider;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import mekanism.common.MekanismLang;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.network.to_server.PacketDropperUse.DropperAction;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

public abstract class GuiTankBar<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> extends GuiBar<ResourceTankInfoProvider<RESOURCE, CONTAINER>> implements IRecipeViewerIngredientHelper {

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    public GuiTankBar(IGuiWrapper gui, ResourceTankInfoProvider<RESOURCE, CONTAINER> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(gui, infoProvider, x, y, width, height, horizontal);
    }

    protected abstract TankType getType();

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        CONTAINER container = getHandler().getContainer();
        List<Component> info = getContentsTooltips(container.resource(), container.amountAsLong(), TooltipContext.of(minecraft.level), minecraft.player,
              minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        if (!info.equals(lastInfo)) {
            lastInfo = info;
            lastTooltip = TooltipUtils.create(info);
        }
        setTooltip(lastTooltip);
    }

    protected List<Component> getContentsTooltips(RESOURCE type, long amount, TooltipContext context, @Nullable Player player, TooltipFlag tooltipFlag) {
        List<Component> tooltips = new ArrayList<>();
        if (type.isEmpty()) {
            tooltips.add(MekanismLang.EMPTY.translate());
        } else if (amount == Long.MAX_VALUE) {
            tooltips.add(MekanismLang.GENERIC_STORED.translate(type, MekanismLang.INFINITE));
        } else {
            tooltips.add(MekanismLang.GENERIC_STORED_MB.translate(type, TextUtils.format(amount)));
        }
        return tooltips;
    }

    protected abstract int getRenderColor(RESOURCE resource);

    protected abstract TextureAtlasSprite getIcon(RESOURCE resource);

    @Override
    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        CONTAINER container = getHandler().getContainer();
        if (!container.isEmpty()) {
            RESOURCE stored = container.resource();
            TextureAtlasSprite icon = getIcon(stored);
            int targetSize = calculateSize(handlerLevel, (horizontal ? width : height) - 2);
            if (horizontal) {
                GuiUtils.drawTiledSprite(guiGraphics, relativeX + 1, relativeY + 1, targetSize, height - 2, icon, TilingDirection.DOWN_RIGHT, getRenderColor(stored));
            } else {
                GuiUtils.drawTiledSprite(guiGraphics, relativeX + 1, relativeY + 1, height - 2, width - 2, targetSize, icon, TilingDirection.DOWN_RIGHT, getRenderColor(stored));
            }
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        ItemStack stack = gui().getCarriedItem();
        if (gui() instanceof GuiMekanismTile && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
            int index = getHandler().getContainerIndex();
            if (index != -1) {
                DropperAction action;
                if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                    action = event.hasShiftDown() ? DropperAction.DUMP_TANK : DropperAction.FILL_DROPPER;
                } else { //InputConstants.MOUSE_BUTTON_RIGHT
                    action = DropperAction.DRAIN_DROPPER;
                }
                PacketUtils.sendToServer(new PacketDropperUse(action, getType(), index));
            }
        }
    }

    @Override
    public boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    protected abstract Object toIngredientStack(RESOURCE resource, long amount);

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        CONTAINER container = getHandler().getContainer();
        return container.isEmpty() ? Optional.empty() : Optional.of(toIngredientStack(container.resource(), container.amountAsLong()));
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        return new Rect2i(getX() + 1, getY() + 1, width - 2, height - 2);
    }

    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> ResourceTankInfoProvider<RESOURCE, CONTAINER> getProvider(
          CONTAINER container, List<? extends CONTAINER> containers) {
        return new ResourceTankInfoProvider<>() {
            @Override
            public CONTAINER getContainer() {
                return container;
            }

            @Override
            public int getContainerIndex() {
                return containers.indexOf(container);
            }
        };
    }

    //Note the GuiBar.IBarInfoHandler is needed, as it cannot compile and resolve just IBarInfoHandler
    public interface ResourceTankInfoProvider<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> extends GuiBar.IBarInfoHandler {

        CONTAINER getContainer();

        int getContainerIndex();

        @Override
        default double getLevel() {
            CONTAINER container = getContainer();
            return MathUtils.divideToLevel(container.amountAsLong(), container.capacityAsLong(container.resource()));
        }
    }
}