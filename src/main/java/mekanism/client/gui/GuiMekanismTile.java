package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.window.GuiUpgradeWindowTab;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class GuiMekanismTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanism<CONTAINER> {

    protected final TILE tile;
    /**
     * May be null if init hasn't been called yet. Will be null if the tile doesn't support upgrades.
     */
    @Nullable
    private GuiUpgradeWindowTab upgradeWindowTab;

    protected GuiMekanismTile(CONTAINER container, Inventory inv, Component title) {
        super(container, inv, title);
        tile = container.getTileEntity();
    }

    public TILE getTileEntity() {
        return tile;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addGenericTabs();
    }

    protected void addGenericTabs() {
        if (tile.supportsUpgrades()) {
            upgradeWindowTab = addRenderableWidget(new GuiUpgradeWindowTab(this, tile, () -> upgradeWindowTab));
        }
        if (tile.supportsRedstone()) {
            addRenderableWidget(new GuiRedstoneControlTab(this, tile));
        }
        //Note: We check if the capability is present rather than calling hasSecurity so that we don't add the tab to the security desk
        if (tile.getCapability(Capabilities.SECURITY_OBJECT).isPresent()) {
            addSecurityTab();
        }
    }

    protected void addSecurityTab() {
        addRenderableWidget(new GuiSecurityTab(this, tile));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.drawForegroundText(matrix, mouseX, mouseY);
        if (tile instanceof ISideConfiguration) {
            ItemStack stack = getMinecraft().player.containerMenu.getCarried();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                for (int i = 0; i < menu.slots.size(); i++) {
                    Slot slot = menu.slots.get(i);
                    if (isMouseOverSlot(slot, mouseX, mouseY)) {
                        DataType data = getFromSlot(slot);
                        if (data != null) {
                            EnumColor color = data.getColor();
                            displayTooltips(matrix, mouseX - leftPos, mouseY - topPos, MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(color, data, color.getName()));
                        }
                        break;
                    }
                }
            }
        }
    }

    private DataType getFromSlot(Slot slot) {
        if (slot.index < tile.getSlots() && slot instanceof InventoryContainerSlot containerSlot) {
            ISideConfiguration config = (ISideConfiguration) tile;
            ConfigInfo info = config.getConfig().getConfig(TransmissionType.ITEM);
            if (info != null) {
                Set<DataType> supportedDataTypes = info.getSupportedDataTypes();
                IInventorySlot inventorySlot = containerSlot.getInventorySlot();
                for (DataType type : supportedDataTypes) {
                    ISlotInfo slotInfo = info.getSlotInfo(type);
                    if (slotInfo instanceof InventorySlotInfo inventorySlotInfo && inventorySlotInfo.hasSlot(inventorySlot)) {
                        return type;
                    }
                }
            }
        }
        return null;
    }
}