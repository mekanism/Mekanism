package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.frequency.IFrequencyItem;
import mekanism.common.inventory.container.item.PortableItemDashboardContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiPortableItemDashboard extends GuiQIOItemViewer<PortableItemDashboardContainer> {

    public GuiPortableItemDashboard(PortableItemDashboardContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiQIOFrequencyTab(this, container.getHand()));
    }

    @Override
    public GuiQIOItemViewer<PortableItemDashboardContainer> recreate(PortableItemDashboardContainer container) {
        return new GuiPortableItemDashboard(container, playerInventory, title);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText(getName(), 4);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public FrequencyIdentity getFrequency() {
        return ((IFrequencyItem) container.getStack().getItem()).getFrequency(container.getStack());
    }

    private ITextComponent getName() {
        return container.getStack().getDisplayName();
    }
}
