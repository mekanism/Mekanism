package mekanism.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.inventory.container.tile.UpgradeManagementContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketRemoveUpgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Switch this to being GuiMekanismTile??
@OnlyIn(Dist.CLIENT)
public class GuiUpgradeManagement<TILE extends TileEntityMekanism & IUpgradeTile> extends GuiMekanism<UpgradeManagementContainer<TILE>> {

    private Button backButton;
    private Button removeButton;
    private TILE tileEntity;
    @Nullable
    private Upgrade selectedType;
    private boolean isDragging = false;
    private double dragOffset = 0;
    private int supportedIndex;
    private int delay;
    private double scroll;

    public GuiUpgradeManagement(UpgradeManagementContainer<TILE> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        tileEntity = container.getTileEntity();
    }

    @Override
    public void init() {
        super.init();
        addButton(backButton = new GuiButtonDisableableImage(guiLeft + 6, guiTop + 6, 14, 14, 176, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tileEntity.getPos()))));
        addButton(removeButton = new GuiButtonDisableableImage(guiLeft + 136, guiTop + 57, 12, 12, 190, 12, -12, 12, getGuiLocation(),
              onPress -> {
                  if (selectedType != null) {
                      Mekanism.packetHandler.sendToServer(new PacketRemoveUpgrade(Coord4D.get(tileEntity), selectedType));
                  }
              }));
        updateEnabledButtons();
    }

    private boolean overUpgradeType(double xAxis, double yAxis, int xPos, int yPos) {
        return xAxis >= xPos && xAxis <= xPos + 58 && yAxis >= yPos && yAxis <= yPos + 12;
    }

    @Override
    public void tick() {
        super.tick();
        if (delay < 40) {
            delay++;
        } else {
            delay = 0;
            supportedIndex = ++supportedIndex % tileEntity.getComponent().getSupportedTypes().size();
        }
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        removeButton.active = selectedType != null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        minecraft.textureManager.bindTexture(getGuiLocation());
        drawTexturedRect(84, 8 + getScroll(), 202, 0, 4, 4);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.upgrades.supported"), ":"), 26, 59, 0x404040);
        if (selectedType == null) {
            renderText(TextComponentUtil.build(Translation.of("mekanism.gui.upgrades.noSelection"), "."), 92, 8, 0.8F, true);
        } else {
            int amount = tileEntity.getComponent().getUpgrades(selectedType);
            renderText(TextComponentUtil.build(selectedType, " ", Translation.of("mekanism.gui.upgrade")), 92, 8, 0.6F, true);
            renderText(TextComponentUtil.build(Translation.of("mekanism.gui.upgrades.amount"), ": " + amount + "/" + selectedType.getMax()), 92, 16, 0.6F, true);
            int text = 0;
            for (ITextComponent component : selectedType.getInfo(tileEntity)) {
                renderText(component, 92, 22 + (6 * text++), 0.6F, true);
            }
        }
        Set<Upgrade> supportedTypes = tileEntity.getComponent().getSupportedTypes();
        if (!supportedTypes.isEmpty()) {
            Upgrade[] supported = supportedTypes.toArray(new Upgrade[0]);
            if (supported.length > supportedIndex) {
                renderUpgrade(supported[supportedIndex], 80, 57, 0.8F, true);
                drawString(TextComponentUtil.build(supported[supportedIndex]), 96, 59, 0x404040);
            }
        }
        Upgrade[] upgrades = getCurrentUpgrades().toArray(new Upgrade[0]);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        for (int i = 0; i < 4; i++) {
            int index = getUpgradeIndex() + i;
            if (index > upgrades.length - 1) {
                break;
            }
            Upgrade upgrade = upgrades[index];
            int xPos = 25;
            int yPos = 7 + (i * 12);
            drawString(TextComponentUtil.build(upgrade), xPos + 12, yPos + 2, 0x404040);
            renderUpgrade(upgrade, xPos + 2, yPos + 2, 0.5F, true);
            if (overUpgradeType(xAxis, yAxis, xPos, yPos)) {
                displayTooltip(TextComponentUtil.build(Translation.of(upgrade.getDescription()), upgrade.getStack()), xAxis, yAxis);
            }
        }

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    private void renderText(ITextComponent component, int x, int y, float size, boolean scale) {
        //TODO: Is this fine?
        renderText(component.getFormattedText(), x, y, size, scale);
    }

    private void renderText(String text, int x, int y, float size, boolean scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(size, size, size);
        drawString(text, scale ? (int) ((1F / size) * x) : x, scale ? (int) ((1F / size) * y) : y, 0x00CD00);
        GlStateManager.popMatrix();
    }

    private void renderUpgrade(Upgrade type, int x, int y, float size, boolean scale) {
        if (scale) {
            renderItem(type.getStack(), (int) ((float) x / size), (int) ((float) y / size), size);
        } else {
            renderItem(type.getStack(), x, y, size);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tileEntity.getComponent().getScaledUpgradeProgress(14);
        drawTexturedRect(guiLeft + 154, guiTop + 26, 176, 28, 10, displayInt);
        if (selectedType != null && tileEntity.getComponent().getUpgrades(selectedType) == 0) {
            selectedType = null;
        }
        Upgrade[] upgrades = getCurrentUpgrades().toArray(new Upgrade[0]);
        for (int i = 0; i < 4; i++) {
            int index = getUpgradeIndex() + i;
            if (index > upgrades.length - 1) {
                break;
            }
            Upgrade upgrade = upgrades[index];
            int xPos = 25;
            int yPos = 7 + (i * 12);
            int yRender;
            if (upgrade == selectedType) {
                yRender = 166 + 24;
            } else if (overUpgradeType(xAxis, yAxis, xPos, yPos)) {
                yRender = 166;
            } else {
                yRender = 166 + 12;
            }
            MekanismRenderer.color(upgrade.getColor(), 1.0F, 2.5F);
            drawTexturedRect(guiLeft + xPos, guiTop + yPos, 0, yRender, 58, 12);
            MekanismRenderer.resetColor();
        }
    }

    private Set<Upgrade> getCurrentUpgrades() {
        return tileEntity.getComponent().getInstalledTypes();
    }

    public int getScroll() {
        return Math.max(Math.min((int) (scroll * 42), 42), 0);
    }

    public int getUpgradeIndex() {
        if (getCurrentUpgrades().size() <= 4) {
            return 0;
        }
        return (int) ((getCurrentUpgrades().size() * scroll) - (4F / (float) getCurrentUpgrades().size()) * scroll);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        //TODO: mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance. look closer
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        if (isDragging) {
            double yAxis = mouseY - (height - ySize) / 2D;
            scroll = Math.min(Math.max((yAxis - 8 - dragOffset) / 42F, 0), 1);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int type) {
        super.mouseReleased(mouseX, mouseY, type);
        if (type == 0 && isDragging) {
            dragOffset = 0;
            isDragging = false;
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "upgrade_management.png");
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - guiLeft;
            double yAxis = mouseY - guiTop;
            if (xAxis >= 84 && xAxis <= 88 && yAxis >= getScroll() + 8 && yAxis <= getScroll() + 8 + 4) {
                if (getCurrentUpgrades().size() > 4) {
                    dragOffset = yAxis - (getScroll() + 8);
                    isDragging = true;
                } else {
                    scroll = 0;
                }
            }
            int counter = 0;
            for (Upgrade upgrade : getCurrentUpgrades()) {
                int xPos = 25;
                int yPos = 7 + (counter++ * 12);
                if (overUpgradeType(xAxis, yAxis, xPos, yPos)) {
                    selectedType = upgrade;
                    break;
                }
            }
        }
        return true;
    }
}