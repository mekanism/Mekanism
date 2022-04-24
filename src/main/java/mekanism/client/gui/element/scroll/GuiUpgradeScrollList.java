package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import java.util.function.ObjIntConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Upgrade;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.resources.ResourceLocation;

public class GuiUpgradeScrollList extends GuiScrollList {

    private static final ResourceLocation UPGRADE_SELECTION = MekanismUtils.getResource(ResourceType.GUI, "upgrade_selection.png");
    private static final int TEXTURE_WIDTH = 58;
    private static final int TEXTURE_HEIGHT = 36;

    private final TileComponentUpgrade component;
    private final Runnable onSelectionChange;
    @Nullable
    private Upgrade selectedType;

    public GuiUpgradeScrollList(IGuiWrapper gui, int x, int y, int width, int height, TileComponentUpgrade component, Runnable onSelectionChange) {
        super(gui, x, y, width, height, TEXTURE_HEIGHT / 3, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE);
        this.component = component;
        this.onSelectionChange = onSelectionChange;
    }

    private Set<Upgrade> getCurrentUpgrades() {
        return component.getInstalledTypes();
    }

    @Override
    protected int getMaxElements() {
        return getCurrentUpgrades().size();
    }

    @Override
    public boolean hasSelection() {
        return selectedType != null;
    }

    @Override
    protected void setSelected(int index) {
        Set<Upgrade> currentUpgrades = getCurrentUpgrades();
        if (index >= 0 && index < currentUpgrades.size()) {
            Upgrade newSelection = currentUpgrades.toArray(new Upgrade[0])[index];
            if (selectedType != newSelection) {
                selectedType = newSelection;
                onSelectionChange.run();
            }
        }
    }

    @Nullable
    public Upgrade getSelection() {
        return selectedType;
    }

    @Override
    public void clearSelection() {
        if (selectedType != null) {
            selectedType = null;
            onSelectionChange.run();
        }
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        forEachUpgrade((upgrade, multipliedElement) -> drawTextScaledBound(matrix, TextComponentUtil.build(upgrade), relativeX + 13, relativeY + 3 + multipliedElement,
              titleTextColor(), 44));
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        if (mouseX >= x + 1 && mouseX < x + barXShift - 1) {
            forEachUpgrade((upgrade, multipliedElement) -> {
                if (mouseY >= y + 1 + multipliedElement && mouseY < y + 1 + multipliedElement + elementHeight) {
                    displayTooltips(matrix, mouseX, mouseY, upgrade.getDescription());
                }
            });
        }
    }

    @Override
    public void renderElements(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        //Draw elements
        if (hasSelection() && component.getUpgrades(getSelection()) == 0) {
            clearSelection();
        }
        RenderSystem.setShaderTexture(0, UPGRADE_SELECTION);
        forEachUpgrade((upgrade, multipliedElement) -> {
            int shiftedY = y + 1 + multipliedElement;
            int j = 1;
            if (upgrade == getSelection()) {
                j = 2;
            } else if (mouseX >= x + 1 && mouseX < barX - 1 && mouseY >= shiftedY && mouseY < shiftedY + elementHeight) {
                j = 0;
            }
            MekanismRenderer.color(upgrade.getColor());
            blit(matrix, x + 1, shiftedY, 0, elementHeight * j, TEXTURE_WIDTH, elementHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            MekanismRenderer.resetColor();
        });
        //Note: This needs to be in its own loop as rendering the items is likely to cause the texture manager to be bound to a different texture
        // and thus would make the selection area background get all screwed up
        forEachUpgrade((upgrade, multipliedElement) -> gui().renderItem(matrix, UpgradeUtils.getStack(upgrade), x + 3, y + 3 + multipliedElement, 0.5F));
    }

    private void forEachUpgrade(ObjIntConsumer<Upgrade> consumer) {
        Upgrade[] upgrades = getCurrentUpgrades().toArray(new Upgrade[0]);
        int currentSelection = getCurrentSelection();
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = currentSelection + i;
            if (index > upgrades.length - 1) {
                break;
            }
            consumer.accept(upgrades[index], elementHeight * i);
        }
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        GuiUpgradeScrollList old = (GuiUpgradeScrollList) element;
        selectedType = old.selectedType;
    }
}