package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiUpgradeScrollList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiUpgradeManagement extends GuiMekanismTile<TileEntityMekanism, MekanismTileContainer<TileEntityMekanism>> {

    private MekanismButton removeButton;
    private GuiUpgradeScrollList scrollList;
    private int supportedIndex;
    private int delay;

    public GuiUpgradeManagement(MekanismTileContainer<TileEntityMekanism> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(scrollList = new GuiUpgradeScrollList(this, 24, 6, 66, 50, tile.getComponent()));
        addButton(new GuiElementHolder(this, 24, 56, 125, 14));
        addButton(new GuiInnerScreen(this, 90, 6, 59, 50));
        addButton(new GuiProgress(() -> tile.getComponent().getScaledUpgradeProgress(), ProgressType.INSTALLING, this, 154, 26));
        addButton(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 6, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile))));
        addButton(removeButton = new MekanismImageButton(this, getGuiLeft() + 136, getGuiTop() + 57, 12, getButtonLocation("remove_upgrade"), () -> {
            if (scrollList.hasSelection()) {
                Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.REMOVE_UPGRADE, tile, scrollList.getSelection().ordinal()));
            }
        }));
        updateEnabledButtons();
    }

    @Override
    public void tick() {
        super.tick();
        if (delay < 40) {
            delay++;
        } else {
            delay = 0;
            supportedIndex = ++supportedIndex % tile.getComponent().getSupportedTypes().size();
        }
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        removeButton.active = scrollList.hasSelection();
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        drawString(matrix, MekanismLang.UPGRADES_SUPPORTED.translate(), 26, 59, titleTextColor());
        if (scrollList.hasSelection()) {
            Upgrade selectedType = scrollList.getSelection();
            int amount = tile.getComponent().getUpgrades(selectedType);
            renderText(matrix, MekanismLang.UPGRADE_TYPE.translate(selectedType), 92, 8, 0.6F);
            renderText(matrix, MekanismLang.UPGRADE_COUNT.translate(amount, selectedType.getMax()), 92, 16, 0.6F);
            int text = 0;
            for (ITextComponent component : UpgradeUtils.getInfo(tile, selectedType)) {
                renderText(matrix, component, 92, 22 + (6 * text++), 0.6F);
            }
        } else {
            renderText(matrix, MekanismLang.UPGRADE_NO_SELECTION.translate(), 92, 8, 0.8F);
        }
        //TODO: Move this into a gui element
        Set<Upgrade> supportedTypes = tile.getComponent().getSupportedTypes();
        if (!supportedTypes.isEmpty()) {
            Upgrade[] supported = supportedTypes.toArray(new Upgrade[0]);
            if (supported.length > supportedIndex) {
                renderUpgrade(matrix, supported[supportedIndex], 80, 57, 0.8F);
                drawString(matrix, TextComponentUtil.build(supported[supportedIndex]), 96, 59, titleTextColor());
            }
        }
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private void renderText(MatrixStack matrix, ITextComponent component, int x, int y, float size) {
        matrix.push();
        matrix.scale(size, size, size);
        drawString(matrix, component, (int) ((1F / size) * x), (int) ((1F / size) * y), screenTextColor());
        matrix.pop();
    }

    private void renderUpgrade(MatrixStack matrix, Upgrade type, int x, int y, float size) {
        renderItem(matrix, UpgradeUtils.getStack(type), (int) (x / size), (int) (y / size), size);
    }
}