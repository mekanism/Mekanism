package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.window.GuiUpgradeWindowTab;
import mekanism.common.MekanismLang;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiMekanismTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanism<CONTAINER> {

    protected final TILE tile;
    /**
     * May be null if init hasn't been called yet. Will be null if the tile doesn't support upgrades.
     */
    @Nullable
    private GuiUpgradeWindowTab upgradeWindowTab;

    protected GuiMekanismTile(CONTAINER container, PlayerInventory inv, ITextComponent title) {
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
            upgradeWindowTab = addButton(new GuiUpgradeWindowTab(this, tile, () -> upgradeWindowTab));
        }
        if (tile.supportsRedstone()) {
            addButton(new GuiRedstoneControlTab(this, tile));
        }
        if (tile.hasSecurity()) {
            addSecurityTab();
        }
    }

    protected void addSecurityTab() {
        addButton(new GuiSecurityTab(this, tile));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.drawForegroundText(matrix, mouseX, mouseY);
        if (tile instanceof ISideConfiguration) {
            ItemStack stack = getMinecraft().player.inventory.getCarried();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                for (int i = 0; i < menu.slots.size(); i++) {
                    Slot slot = menu.slots.get(i);
                    if (isMouseOverSlot(slot, mouseX, mouseY)) {
                        DataType data = getFromSlot(slot);
                        if (data != null) {
                            EnumColor color = data.getColor();
                            displayTooltip(matrix, MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(color, data, color.getName()), mouseX - leftPos, mouseY - topPos);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void renderTitleText(MatrixStack matrix) {
        drawTitleText(matrix, tile.getName(), titleLabelY);
    }

    private DataType getFromSlot(Slot slot) {
        if (slot.index < tile.getSlots() && slot instanceof InventoryContainerSlot) {
            ISideConfiguration config = (ISideConfiguration) tile;
            ConfigInfo info = config.getConfig().getConfig(TransmissionType.ITEM);
            if (info != null) {
                Set<DataType> supportedDataTypes = info.getSupportedDataTypes();
                IInventorySlot inventorySlot = ((InventoryContainerSlot) slot).getInventorySlot();
                for (DataType type : supportedDataTypes) {
                    ISlotInfo slotInfo = info.getSlotInfo(type);
                    if (slotInfo instanceof InventorySlotInfo && ((InventorySlotInfo) slotInfo).hasSlot(inventorySlot)) {
                        return type;
                    }
                }
            }
        }
        return null;
    }
}