package mekanism.client.gui;

import mekanism.api.security.IBlockSecurityUtils;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.window.GuiRecipeBookWindowTab;
import mekanism.client.gui.element.tab.window.GuiUpgradeWindowTab;
import mekanism.client.gui.recipe_book.MekanismRecipeBookComponent;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public abstract class GuiMekanismTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanism<CONTAINER> {

    protected final TILE tile;
    /// May be null if init hasn't been called yet. Will be null if the tile doesn't support upgrades.
    @UnknownNullability
    private GuiUpgradeWindowTab upgradeWindowTab;
    /// May be null if init hasn't been called yet. Will be null if the tile doesn't support the recipe book.
    @UnknownNullability
    private GuiRecipeBookWindowTab recipeBookWindowTab;

    protected GuiMekanismTile(CONTAINER container, Inventory inv, Component title) {
        this(container, inv, title, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    protected GuiMekanismTile(CONTAINER container, Inventory inv, Component title, int imageWidth, int imageHeight) {
        tile = container.getTileEntity();
        super(container, inv, title, imageWidth, imageHeight);
        this.recipeBookComponent = createRecipeBookComponent();
    }

    @Nullable
    protected MekanismRecipeBookComponent<?> createRecipeBookComponent() {
        if (tile instanceof IRecipeLookupHandler<?> recipeLookupHandler) {
            return new MekanismRecipeBookComponent<>(menu, recipeLookupHandler, registryAccess(), this::font);
        }
        return null;
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
        if (recipeBookComponent != null) {
            recipeBookWindowTab = addRenderableWidget(new GuiRecipeBookWindowTab(this, recipeBookComponent, tile instanceof TileEntityPrecisionSawmill, () -> recipeBookWindowTab));
        }
        if (tile.supportsRedstone()) {
            //TODO - 26.3: The warning for redstone can overlap with elements in the following guis:
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

    @Override
    public Level getLevel() {
        Level level = tile.getLevel();
        return level == null ? super.getLevel() : level;
    }
}