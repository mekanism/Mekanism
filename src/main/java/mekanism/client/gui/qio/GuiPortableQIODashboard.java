package mekanism.client.gui.qio;

import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiPortableQIODashboard extends GuiQIOItemViewer<PortableQIODashboardContainer> {

    public GuiPortableQIODashboard(PortableQIODashboardContainer container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiQIOFrequencyTab(this, menu.getHand()));
    }

    @Override
    public GuiQIOItemViewer<PortableQIODashboardContainer> recreate(PortableQIODashboardContainer container) {
        return new GuiPortableQIODashboard(container, inv, title);
    }

    @Override
    public FrequencyIdentity getFrequency() {
        ItemStack stack = menu.getStack();
        if (stack.isEmpty()) {//Note: This shouldn't be empty, but we validate it just in case
            return null;
        }
        FrequencyAware<QIOFrequency> frequencyAware = stack.get(MekanismDataComponents.QIO_FREQUENCY);
        return frequencyAware == null ? null : frequencyAware.identity().orElse(null);
    }
}
