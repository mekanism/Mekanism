package mekanism.client.gui.element.custom;

import java.util.function.BooleanSupplier;
import mekanism.api.functions.ByteSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiTeleporterStatus extends GuiTexturedElement {

    private static final ResourceLocation NEEDS_ENERGY = MekanismUtils.getResource(ResourceType.GUI, "teleporter_needs_energy.png");
    private static final ResourceLocation NO_FRAME = MekanismUtils.getResource(ResourceType.GUI, "teleporter_no_frame.png");
    private static final ResourceLocation NO_FREQUENCY = MekanismUtils.getResource(ResourceType.GUI, "teleporter_no_frequency.png");
    private static final ResourceLocation NO_LINK = MekanismUtils.getResource(ResourceType.GUI, "teleporter_no_link.png");
    private static final ResourceLocation READY = MekanismUtils.getResource(ResourceType.GUI, "teleporter_ready.png");

    private final BooleanSupplier hasFrequency;
    private final ByteSupplier statusSupplier;

    public GuiTeleporterStatus(IGuiWrapper gui, BooleanSupplier hasFrequency, ByteSupplier statusSupplier) {
        super(NO_FREQUENCY, gui, 6, 6, 18, 18);
        this.hasFrequency = hasFrequency;
        this.statusSupplier = statusSupplier;
        setButtonBackground(ButtonBackground.DEFAULT);
    }

    @Override
    protected int getButtonTextureY(boolean hoveredOrFocused) {
        return 1;
    }

    @Override
    protected ResourceLocation getResource() {
        if (hasFrequency.getAsBoolean()) {
            return switch (statusSupplier.getAsByte()) {
                case 1 -> READY;
                case 2 -> NO_FRAME;
                case 4 -> NEEDS_ENERGY;
                default -> NO_LINK;
            };
        }
        return NO_FREQUENCY;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(getResource(), getX(), getY(), 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        displayTooltips(guiGraphics, mouseX, mouseY, getStatusDisplay());
    }

    private Component getStatusDisplay() {
        if (hasFrequency.getAsBoolean()) {
            return switch (statusSupplier.getAsByte()) {
                case 1 -> MekanismLang.TELEPORTER_READY.translateColored(EnumColor.DARK_GREEN);
                case 2 -> MekanismLang.TELEPORTER_NO_FRAME.translateColored(EnumColor.DARK_RED);
                case 4 -> MekanismLang.TELEPORTER_NEEDS_ENERGY.translateColored(EnumColor.DARK_RED);
                default -> MekanismLang.TELEPORTER_NO_LINK.translateColored(EnumColor.DARK_RED);
            };
        }
        return MekanismLang.NO_FREQUENCY.translateColored(EnumColor.DARK_RED);
    }
}