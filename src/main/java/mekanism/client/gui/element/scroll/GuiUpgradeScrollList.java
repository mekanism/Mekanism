package mekanism.client.gui.element.scroll;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GuiUpgradeScrollList extends GuiInstallableScrollList<Upgrade> {

    private final Map<Upgrade, Tooltip> tooltips = new EnumMap<>(Upgrade.class);
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
    protected void setSelected(@Nullable Upgrade newSelection) {
        if (selectedType != newSelection) {
            selectedType = newSelection;
            onSelectionChange.run();
        }
    }

    @Override
    protected List<Upgrade> getCurrentInstalled() {
        return new ArrayList<>(component.getInstalledTypes());
    }

    @Override
    protected void drawName(GuiGraphicsExtractor guiGraphics, Upgrade upgrade, int y) {
        drawNameText(guiGraphics, y, upgrade.getTranslatedName(), titleTextColor(), 1F);
    }

    @Override
    protected ItemStack getRenderStack(Upgrade upgrade) {
        return UpgradeUtils.getStack(upgrade);
    }

    @Nullable
    @Override
    protected EnumColor getColor(Upgrade upgrade) {
        return upgrade.getColor();
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        if (mouseX >= getX() + 1 && mouseX < getX() + barXShift - 1) {
            List<Upgrade> currentInstalled = getCurrentInstalled();
            int currentSelection = getCurrentSelection();
            for (int i = 0, focused = getFocusedElements(); i < focused; i++) {
                int index = currentSelection + i;
                if (index > currentInstalled.size() - 1) {
                    break;
                }
                Upgrade upgrade = currentInstalled.get(index);
                int multipliedElement = elementHeight * i;
                if (mouseY >= getY() + 1 + multipliedElement && mouseY < getY() + 1 + multipliedElement + elementHeight) {
                    cachedTooltipRect = new ScreenRectangle(getX() + 1, getY() + 1 + multipliedElement, barXShift - 2, elementHeight);
                    setTooltip(tooltips.computeIfAbsent(upgrade, u -> TooltipUtils.create(u.getDescription())));
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
        Upgrade selection = getSelection();
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