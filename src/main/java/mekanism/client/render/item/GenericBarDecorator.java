package mekanism.client.render.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.GuiUtils;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.ResourceContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.resource.Resource;

public class GenericBarDecorator implements IItemDecorator {

    private final List<BarRenderer> renderers;
    private final boolean horizontal;

    private GenericBarDecorator(List<BarRenderer> renderers, boolean horizontal) {
        if (renderers.isEmpty()) {
            throw new IllegalStateException("No bar renderers defined");
        }
        this.renderers = renderers;
        this.horizontal = horizontal;
    }

    @Override
    public boolean render(GuiGraphicsExtractor guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (horizontal) {
            yOffset += 12;
        } else {
            xOffset++;
        }
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
        boolean multipleBars = renderers.size() > 1;
        boolean isFirstBar = true;
        for (BarRenderer renderer : renderers) {
            if (renderer.renderBar(guiGraphics, xOffset, yOffset, horizontal, itemAccess, multipleBars, isFirstBar)) {
                if (horizontal) {
                    yOffset--;
                } else {
                    xOffset++;
                }
                isFirstBar = false;
            }
        }
        return true;
    }

    protected static <RESOURCE extends Resource> boolean renderBar(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, boolean horizontal,
          ResourceHandler<RESOURCE> handler, int index, boolean multipleBars, boolean isFirstBar, ToIntFunction<RESOURCE> color) {
        if (index != -1) {
            long amount = handler.getAmountAsLong(index);
            if (amount == 0 && !multipleBars) {
                //We don't display the bar if there is nothing stored in any of the containers, and we only have one bar
                return false;
            }
            RESOURCE resource = handler.getResource(index);
            renderBar(guiGraphics, xOffset, yOffset, horizontal, handler.getAmountAsLong(index), handler.getCapacityAsLong(index, resource), color.applyAsInt(resource), isFirstBar);
        } else if (handler.size() == 0) {
            renderBar(guiGraphics, xOffset, yOffset, horizontal, 0, 1, CommonColors.WHITE, isFirstBar);
        } else {
            return false;
        }
        return true;
    }

    private static void renderBar(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, boolean horizontal, long amount, long capacity, int color, boolean isFirstBar) {
        int barSize = StorageUtils.getBarWidth(MathUtils.divideToLevel(amount, capacity));
        if (horizontal) {
            GuiUtils.fill(guiGraphics, xOffset + 2, yOffset, Item.MAX_BAR_WIDTH, isFirstBar ? 2 : 1, CommonColors.BLACK);
            GuiUtils.fill(guiGraphics, xOffset + 2, yOffset, barSize, 1, ARGB.opaque(color));
        } else {
            GuiUtils.fill(guiGraphics, xOffset, yOffset + 2, 2, Item.MAX_BAR_WIDTH, CommonColors.BLACK);
            GuiUtils.fill(guiGraphics, xOffset, yOffset + 2 + Item.MAX_BAR_WIDTH - barSize, 1, barSize, ARGB.opaque(color));
        }
    }

    static int getDisplayTank(int tanks) {
        if (tanks == 0) {
            return -1;
        } else if (tanks > 1) {
            //Cycle through multiple tanks every second, to save some space if multiple tanks are present
            return (Minecraft.getInstance().gui.hud.getGuiTicks() / SharedConstants.TICKS_PER_SECOND) % tanks;
        }
        return 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<BarRenderer> renderers = new ArrayList<>();
        private boolean horizontal = true;

        public Builder showChemical() {
            return addRenderer((graphics, xOffset, yOffset, horizontal, itemAccess, multipleBars, isFirstBar) ->
                  renderBar(graphics, xOffset, yOffset, horizontal, ContainerType.CHEMICAL, itemAccess, multipleBars, isFirstBar));
        }

        public Builder showEnergy() {
            return addRenderer((graphics, xOffset, yOffset, horizontal, itemAccess, multipleBars, isFirstBar) -> {
                EnergyHandler handler = ContainerType.ENERGY.getCapOrUnexposed(itemAccess);
                if (handler != null) {
                    long amount = handler.getAmountAsLong();
                    //We don't display the bar if there is nothing stored in any of the containers, and we only have one bar
                    if (amount > 0 || multipleBars) {
                        GenericBarDecorator.renderBar(graphics, xOffset, yOffset, horizontal, amount, handler.getCapacityAsLong(), MekanismConfig.client.energyColor.get(), isFirstBar);
                        return true;
                    }
                }
                return false;
            });
        }

        public Builder showFluid() {
            return addRenderer((graphics, xOffset, yOffset, horizontal, itemAccess, multipleBars, isFirstBar) ->
                  renderBar(graphics, xOffset, yOffset, horizontal, ContainerType.FLUID, itemAccess, multipleBars, isFirstBar));
        }

        public Builder addRenderer(BarRenderer renderer) {
            this.renderers.add(renderer);
            return this;
        }

        public Builder vertical() {
            this.horizontal = false;
            return this;
        }

        public GenericBarDecorator build() {
            return new GenericBarDecorator(renderers, horizontal);
        }

        private static <RESOURCE extends Resource> boolean renderBar(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, boolean horizontal,
              ResourceContainerType<RESOURCE, ?> containerType, ItemAccess itemAccess, boolean multipleBars, boolean isFirstBar) {
            //Note: We just directly query the stored contents of the containers and don't care about the size of the item access
            ResourceHandler<RESOURCE> handler = containerType.getCapOrUnexposed(itemAccess);
            return handler != null && GenericBarDecorator.renderBar(guiGraphics, xOffset, yOffset, horizontal, handler, getDisplayTank(handler.size()), multipleBars,
                  isFirstBar, containerType::getRGBDurabilityForDisplay);
        }
    }

    @FunctionalInterface
    public interface BarRenderer {

        boolean renderBar(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, boolean horizontal, ItemAccess itemAccess, boolean multipleBars, boolean isFirstBar);
    }
}