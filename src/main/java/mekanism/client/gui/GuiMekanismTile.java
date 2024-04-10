package mekanism.client.gui;

import java.util.Set;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.IBlockSecurityUtils;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiMekanismTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanism<CONTAINER> {

    protected final TILE tile;
    /**
     * May be null if init hasn't been called yet. Will be null if the tile doesn't support upgrades.
     */
    @Nullable
    private GuiUpgradeWindowTab upgradeWindowTab;

    @Nullable
    private Component lastInfo = null;
    @Nullable
    private Tooltip lastTooltip;

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
        if (tile.getLevel() != null && IBlockSecurityUtils.INSTANCE.securityCapability(tile.getLevel(), tile.getBlockPos(), tile) != null) {
            addSecurityTab();
        }
    }

    protected void addSecurityTab() {
        addRenderableWidget(new GuiSecurityTab(this, tile));
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        //TODO: Can we move this into GuiSlot#updateTooltip to mirror how we do it for GuiGauge?
        // would potentially let us have an even more accurate screen rectangle, especially for sawmill output
        if (tile instanceof ISideConfiguration) {
            ItemStack stack = getCarriedItem();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                Slot slot = getSlotUnderMouse();
                if (slot != null) {
                    DataType data = getFromSlot(slot);
                    if (data != null) {
                        EnumColor color = data.getColor();
                        Component info = MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(color, data, color.getName());
                        if (!info.equals(lastInfo)) {
                            lastInfo = info;
                            lastTooltip = Tooltip.create(info);
                        }
                        if (lastTooltip != null) {
                            lastTooltip.refreshTooltipForNextRenderPass(true, true,
                                  ScreenRectangle.of(ScreenAxis.HORIZONTAL, slot.x, slot.y, 16, 16));
                            return;
                        }
                    }
                }
            }
        }
        lastInfo = null;
        lastTooltip = null;
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