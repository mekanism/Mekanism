package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.common.inventory.container.item.PortableItemDashboardContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiPortableItemDashboard extends GuiMekanism<PortableItemDashboardContainer> {

    public GuiPortableItemDashboard(PortableItemDashboardContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiQIOFrequencyTab(this, container.getHand()));
    }
}
