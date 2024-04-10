package mekanism.client.gui.element.custom;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.MultiLineTooltip;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiSupportedUpgrades extends GuiElement {

    private static final int ELEMENT_SIZE = 12;
    private static final int FIRST_ROW_ROOM = (123 - 55) / ELEMENT_SIZE;
    private static final int ROW_ROOM = 123 / ELEMENT_SIZE;

    public static int calculateNeededRows() {
        int count = EnumUtils.UPGRADES.length;
        if (count <= FIRST_ROW_ROOM) {
            return 1;
        }
        count -= FIRST_ROW_ROOM;
        return 2 + count / ROW_ROOM;
    }

    private final Set<Upgrade> supportedUpgrades;

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;
    @Nullable
    private ScreenRectangle cachedTooltipRect;

    public GuiSupportedUpgrades(IGuiWrapper gui, int x, int y, Set<Upgrade> supportedUpgrades) {
        super(gui, x, y, 125, ELEMENT_SIZE * calculateNeededRows() + 2);
        this.supportedUpgrades = supportedUpgrades;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        //Draw the background
        renderBackgroundTexture(guiGraphics, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE, GuiElementHolder.HOLDER_SIZE);
        int backgroundColor = Color.argb(GuiElementHolder.getBackgroundColor()).alpha(0.5).argb();
        for (int i = 0; i < EnumUtils.UPGRADES.length; i++) {
            Upgrade upgrade = EnumUtils.UPGRADES[i];
            UpgradePos pos = getUpgradePos(i);
            int xPos = relativeX + 1 + pos.x;
            int yPos = relativeY + 1 + pos.y;
            gui().renderItem(guiGraphics, UpgradeUtils.getStack(upgrade), xPos, yPos, 0.75F);
            if (!supportedUpgrades.contains(upgrade)) {
                //Make the upgrade appear faded if it is not supported
                guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), xPos, yPos, xPos + ELEMENT_SIZE, yPos + ELEMENT_SIZE, backgroundColor);
            }
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTextScaledBound(guiGraphics, MekanismLang.UPGRADES_SUPPORTED.translate(), relativeX + 2, relativeY + 3, titleTextColor(), 54);
    }

    @NotNull
    @Override
    protected ScreenRectangle getTooltipRectangle(int mouseX, int mouseY) {
        return cachedTooltipRect == null ? super.getTooltipRectangle(mouseX, mouseY) : cachedTooltipRect;
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        for (int i = 0; i < EnumUtils.UPGRADES.length; i++) {
            UpgradePos pos = getUpgradePos(i);
            if (mouseX >= getX() + 1 + pos.x && mouseX < getX() + 1 + pos.x + ELEMENT_SIZE &&
                mouseY >= getY() + 1 + pos.y && mouseY < getY() + 1 + pos.y + ELEMENT_SIZE) {
                Upgrade upgrade = EnumUtils.UPGRADES[i];
                Component upgradeName = MekanismLang.UPGRADE_TYPE.translateColored(EnumColor.YELLOW, upgrade);
                List<Component> info;
                if (supportedUpgrades.contains(upgrade)) {
                    info = List.of(upgradeName, upgrade.getDescription());
                } else {
                    info = List.of(MekanismLang.UPGRADE_NOT_SUPPORTED.translateColored(EnumColor.RED, upgradeName), upgrade.getDescription());
                }
                if (!info.equals(lastInfo)) {
                    lastInfo = info;
                    lastTooltip = MultiLineTooltip.createMulti(info);
                    //Note: We only have to update the tooltip rect if the tooltip changed as we know none of the elements share the same tooltips
                    cachedTooltipRect = new ScreenRectangle(getX() + 1 + pos.x, getY() + 1 + pos.y, ELEMENT_SIZE, ELEMENT_SIZE);
                }
                setTooltip(lastTooltip);
                //We can break once we managed to find a tooltip to render
                return;
            }
        }
        lastInfo = Collections.emptyList();
        cachedTooltipRect = null;
        setTooltip(lastTooltip = null);
    }

    private UpgradePos getUpgradePos(int index) {
        int row = index < FIRST_ROW_ROOM ? 0 : 1 + (index - FIRST_ROW_ROOM) / ROW_ROOM;
        if (row == 0) {
            //First row has x start a lot further in
            return new UpgradePos(55 + (index % FIRST_ROW_ROOM) * ELEMENT_SIZE, 0);
        }
        //Shift the index so that we don't have to deal with the weird first row in terms of counting
        index -= FIRST_ROW_ROOM;
        return new UpgradePos((index % ROW_ROOM) * ELEMENT_SIZE, row * ELEMENT_SIZE);
    }

    private record UpgradePos(int x, int y) {
    }
}