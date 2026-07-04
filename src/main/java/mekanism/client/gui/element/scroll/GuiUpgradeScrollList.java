package mekanism.client.gui.element.scroll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.text.EnumColor;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.tile.component.TileComponentUpgrade;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GuiUpgradeScrollList extends GuiInstallableScrollList<Holder<Upgrade>> {

    private final Map<Holder<Upgrade>, Tooltip> tooltips = new HashMap<>();
    private final TileComponentUpgrade component;
    private final Runnable onSelectionChange;

    public GuiUpgradeScrollList(IGuiWrapper gui, int x, int y, int height, TileComponentUpgrade component, Runnable onSelectionChange) {
        super(gui, x, y, 108, height, GuiElementHolder.HOLDER);
        this.component = component;
        this.onSelectionChange = onSelectionChange;
    }

    @Override
    protected int getMaxElements() {
        return component.getInstalledTypes().size();
    }

    @Override
    protected void setSelected(@Nullable Holder<Upgrade> newSelection) {
        if (selectedType != newSelection) {
            selectedType = newSelection;
            onSelectionChange.run();
        }
    }

    @Override
    protected List<Holder<Upgrade>> getCurrentInstalled() {
        return new ArrayList<>(component.getInstalledTypes());
    }

    @Override
    protected void drawName(GuiGraphicsExtractor guiGraphics, Holder<Upgrade> upgrade, int y) {
        drawNameText(guiGraphics, y, upgrade.value().displayName(), titleTextColor(), 1F);
    }

    @Override
    protected ItemStack getRenderStack(Holder<Upgrade> upgrade) {
        return IUpgradeHelper.INSTANCE.asStack(upgrade);
    }

    @Nullable
    @Override
    protected EnumColor getColor(Holder<Upgrade> upgrade) {
        return upgrade.value().color();
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        if (mouseX >= getX() + 1 && mouseX < getX() + barXShift - 1) {
            List<Holder<Upgrade>> currentInstalled = getCurrentInstalled();
            int currentSelection = getCurrentSelection();
            for (int i = 0, focused = getFocusedElements(); i < focused; i++) {
                int index = currentSelection + i;
                if (index > currentInstalled.size() - 1) {
                    break;
                }
                Holder<Upgrade> upgrade = currentInstalled.get(index);
                int multipliedElement = elementHeight * i;
                if (mouseY >= getY() + 1 + multipliedElement && mouseY < getY() + 1 + multipliedElement + elementHeight) {
                    cachedTooltipRect = new ScreenRectangle(getX() + 1, getY() + 1 + multipliedElement, barXShift - 2, elementHeight);
                    setTooltip(tooltips.computeIfAbsent(upgrade, u -> TooltipUtils.create(u.value().description())));
                    return;
                }
            }
        }
        cachedTooltipRect = null;
        clearTooltip();
    }

    @Override
    public void renderElements(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Draw elements
        Holder<Upgrade> selection = getSelection();
        if (selection != null && component.getUpgrades(selection) == 0) {
            clearSelection();
        }
        super.renderElements(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        GuiUpgradeScrollList old = (GuiUpgradeScrollList) element;
        selectedType = old.selectedType;
        //Ensure that it knows about there being a selection
        onSelectionChange.run();
    }
}