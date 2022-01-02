package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

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

    public GuiSupportedUpgrades(IGuiWrapper gui, int x, int y, Set<Upgrade> supportedUpgrades) {
        super(gui, x, y, 125, ELEMENT_SIZE * calculateNeededRows() + 2);
        this.supportedUpgrades = supportedUpgrades;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        //Draw the background
        renderBackgroundTexture(matrix, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE, GuiElementHolder.HOLDER_SIZE);
        int backgroundColor = Color.argb(GuiElementHolder.getBackgroundColor()).alpha(0.5).argb();
        for (int i = 0; i < EnumUtils.UPGRADES.length; i++) {
            Upgrade upgrade = EnumUtils.UPGRADES[i];
            UpgradePos pos = getUpgradePos(i);
            int xPos = x + 1 + pos.x;
            int yPos = y + 1 + pos.y;
            gui().renderItem(matrix, UpgradeUtils.getStack(upgrade), xPos, yPos, 0.75F);
            if (!supportedUpgrades.contains(upgrade)) {
                //Make the upgrade appear faded if it is not supported
                RenderSystem.depthFunc(GL11.GL_GREATER);
                AbstractGui.fill(matrix, xPos, yPos, xPos + ELEMENT_SIZE, yPos + ELEMENT_SIZE, backgroundColor);
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
            }
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawTextScaledBound(matrix, MekanismLang.UPGRADES_SUPPORTED.translate(), relativeX + 2, relativeY + 3, titleTextColor(), 54);
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        for (int i = 0; i < EnumUtils.UPGRADES.length; i++) {
            UpgradePos pos = getUpgradePos(i);
            if (mouseX >= relativeX + 1 + pos.x && mouseX < relativeX + 1 + pos.x + ELEMENT_SIZE &&
                mouseY >= relativeY + 1 + pos.y && mouseY < relativeY + 1 + pos.y + ELEMENT_SIZE) {
                Upgrade upgrade = EnumUtils.UPGRADES[i];
                ITextComponent upgradeName = MekanismLang.UPGRADE_TYPE.translateColored(EnumColor.YELLOW, upgrade);
                List<ITextComponent> tooltip;
                if (supportedUpgrades.contains(upgrade)) {
                    tooltip = Arrays.asList(upgradeName, upgrade.getDescription());
                } else {
                    tooltip = Arrays.asList(MekanismLang.UPGRADE_NOT_SUPPORTED.translateColored(EnumColor.RED, upgradeName), upgrade.getDescription());
                }
                displayTooltips(matrix, tooltip, mouseX, mouseY, getGuiWidth());
                //We can break once we managed to find a tooltip to render
                break;
            }
        }
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

    private static class UpgradePos {

        private final int x;
        private final int y;

        public UpgradePos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}