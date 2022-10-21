package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiSecurityLight;
import mekanism.client.gui.element.GuiTextureOnlyElement;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.to_server.PacketAddTrusted;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiSecurityDesk extends GuiMekanismTile<TileEntitySecurityDesk, MekanismTileContainer<TileEntitySecurityDesk>> {

    private static final ResourceLocation PUBLIC = MekanismUtils.getResource(ResourceType.GUI, "public.png");
    private static final ResourceLocation PRIVATE = MekanismUtils.getResource(ResourceType.GUI, "private.png");
    private MekanismButton removeButton;
    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton trustedButton;
    private MekanismButton overrideButton;
    private GuiTextScrollList scrollList;
    private GuiTextField trustedField;

    public GuiSecurityDesk(MekanismTileContainer<TileEntitySecurityDesk> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 64;
        inventoryLabelY = imageHeight - 94;
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        addRenderableWidget(new GuiElementHolder(this, 141, 13, 26, 37));
        addRenderableWidget(new GuiElementHolder(this, 141, 54, 26, 34));
        addRenderableWidget(new GuiElementHolder(this, 141, 92, 26, 37));
        super.addGuiElements();
        addRenderableWidget(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 17));
        addRenderableWidget(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 96));
        addRenderableWidget(new GuiSecurityLight(this, 144, 77, () -> {
            SecurityFrequency frequency = tile.getFreq();
            if (!isOwner(frequency)) {
                return 2;
            }
            return frequency.isOverridden() ? 0 : 1;
        }));
        addRenderableWidget(new GuiTextureOnlyElement(PUBLIC, this, 145, 32, 18, 18));
        addRenderableWidget(new GuiTextureOnlyElement(PRIVATE, this, 145, 111, 18, 18));
        scrollList = addRenderableWidget(new GuiTextScrollList(this, 13, 13, 122, 42));
        removeButton = addRenderableWidget(new TranslationButton(this, 13, 81, 122, 20, MekanismLang.BUTTON_REMOVE, () -> {
            int selection = scrollList.getSelection();
            if (tile.getFreq() != null && selection != -1) {
                Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.REMOVE_TRUSTED, tile, selection));
                scrollList.clearSelection();
                updateButtons();
            }
        }));
        trustedField = addRenderableWidget(new GuiTextField(this, 35, 68, 99, 11));
        trustedField.setMaxLength(PacketAddTrusted.MAX_NAME_LENGTH);
        trustedField.setBackground(BackgroundType.INNER_SCREEN);
        trustedField.setEnterHandler(this::setTrusted);
        trustedField.setInputValidator(InputValidator.USERNAME);
        trustedField.addCheckmarkButton(this::setTrusted);
        publicButton = addRenderableWidget(new MekanismImageButton(this, 13, 113, 40, 16, 40, 16, getButtonLocation("public"),
              () -> {
                  Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.SECURITY_DESK_MODE, tile, SecurityMode.PUBLIC.ordinal()));
                  updateButtons();
              }, getOnHover(MekanismLang.PUBLIC_MODE)));
        privateButton = addRenderableWidget(new MekanismImageButton(this, 54, 113, 40, 16, 40, 16, getButtonLocation("private"),
              () -> {
                  Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.SECURITY_DESK_MODE, tile, SecurityMode.PRIVATE.ordinal()));
                  updateButtons();
              }, getOnHover(MekanismLang.PRIVATE_MODE)));
        trustedButton = addRenderableWidget(new MekanismImageButton(this, 95, 113, 40, 16, 40, 16, getButtonLocation("trusted"),
              () -> {
                  Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.SECURITY_DESK_MODE, tile, SecurityMode.TRUSTED.ordinal()));
                  updateButtons();
              }, getOnHover(MekanismLang.TRUSTED_MODE)));
        overrideButton = addRenderableWidget(new MekanismImageButton(this, 146, 59, 16, 16, getButtonLocation("exclamation"),
              () -> {
                  Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.OVERRIDE_BUTTON, tile));
                  updateButtons();
              }, (onHover, matrix, mouseX, mouseY) -> {
            SecurityFrequency frequency = tile.getFreq();
            if (frequency != null) {
                displayTooltips(matrix, mouseX, mouseY, MekanismLang.SECURITY_OVERRIDE.translate(OnOff.of(frequency.isOverridden())));
            }
        }));
        updateButtons();
    }

    private boolean isOwner(@Nullable SecurityFrequency frequency) {
        return frequency != null && tile.ownerMatches(getMinecraft().player);
    }

    private void setTrusted() {
        if (isOwner(tile.getFreq())) {
            addTrusted(trustedField.getText());
            trustedField.setText("");
            updateButtons();
        }
    }

    private void addTrusted(String trusted) {
        if (PacketAddTrusted.validateNameLength(trusted.length())) {
            Mekanism.packetHandler().sendToServer(new PacketAddTrusted(tile.getBlockPos(), trusted));
        }
    }

    private void updateButtons() {
        SecurityFrequency freq = tile.getFreq();
        if (tile.getOwnerUUID() != null) {
            scrollList.setText(freq == null ? Collections.emptyList() : freq.getTrustedUsernameCache());
            removeButton.active = scrollList.hasSelection();
        }

        if (isOwner(freq)) {
            publicButton.active = freq.getSecurityMode() != SecurityMode.PUBLIC;
            privateButton.active = freq.getSecurityMode() != SecurityMode.PRIVATE;
            trustedButton.active = freq.getSecurityMode() != SecurityMode.TRUSTED;
            overrideButton.active = true;
        } else {
            publicButton.active = false;
            privateButton.active = false;
            trustedButton.active = false;
            overrideButton.active = false;
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateButtons();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateButtons();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        Component ownerComponent = OwnerDisplay.of(tile.getOwnerUUID(), tile.getOwnerName()).getTextComponent();
        drawString(matrix, ownerComponent, imageWidth - 7 - getStringWidth(ownerComponent), inventoryLabelY, titleTextColor());
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        drawCenteredText(matrix, MekanismLang.TRUSTED_PLAYERS.translate(), 74, 57, subheadingTextColor());
        SecurityFrequency frequency = tile.getFreq();
        if (frequency != null) {
            drawString(matrix, MekanismLang.SECURITY.translate(frequency.getSecurityMode()), 13, 103, titleTextColor());
        } else {
            drawString(matrix, MekanismLang.SECURITY_OFFLINE.translateColored(EnumColor.RED), 13, 103, titleTextColor());
        }
        drawTextScaledBound(matrix, MekanismLang.SECURITY_ADD.translate(), 13, 70, titleTextColor(), 20);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}