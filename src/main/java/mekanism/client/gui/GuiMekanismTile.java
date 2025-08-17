package mekanism.client.gui;

import mekanism.api.security.IBlockSecurityUtils;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.window.GuiUpgradeWindowTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

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
            //TODO - 1.21: The warning for redstone can overlap with elements in the following guis:
            // - Chemical tank: Armor
            // - Heat generator: heat tab
            // - Quantum Entangloporter: heat tab
            // Warning:
            // super(DISABLED, gui, tile, gui.getXSize(), x : 137, y : 26, innerSize : 18, left : false);


            addRenderableWidget(new GuiRedstoneControlTab(this, tile)
                .warning(WarningType.REDSTONE_SIGNAL_ABSENT, () -> tile.getControlType() == RedstoneControl.HIGH && !tile.isRedstoneActivated())
                .warning(WarningType.REDSTONE_SIGNAL_PRESENT, () -> tile.getControlType() == RedstoneControl.LOW && !tile.isRedstoneActivated())
                .warning(WarningType.REDSTONE_PULSE_REQUIRED, () -> tile.getControlType() == RedstoneControl.PULSE && !tile.isRedstoneActivated()));
        }
        //Note: We check if the capability is present rather than calling hasSecurity so that we don't add the tab to the security desk
        if (tile.getLevel() != null && IBlockSecurityUtils.INSTANCE.securityCapability(tile.getLevel(), tile.getBlockPos(), tile) != null) {
            addSecurityTab();
        }
    }

    protected void addSecurityTab() {
        addRenderableWidget(new GuiSecurityTab(this, tile));
    }
}