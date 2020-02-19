package mekanism.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.api.functions.ByteSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiTeleporterStatus extends GuiTexturedElement {

    private static final ResourceLocation NEEDS_ENERGY = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "teleporter_needs_energy.png");
    private static final ResourceLocation NO_FRAME = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "teleporter_no_frame.png");
    private static final ResourceLocation NO_FREQUENCY = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "teleporter_no_frequency.png");
    private static final ResourceLocation NO_LINK = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "teleporter_no_link.png");
    private static final ResourceLocation READY = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "teleporter_ready.png");

    private final BooleanSupplier hasFrequency;
    private final ByteSupplier statusSupplier;

    public GuiTeleporterStatus(IGuiWrapper gui, BooleanSupplier hasFrequency, ByteSupplier statusSupplier) {
        super(NO_FREQUENCY, gui, 6, 6, 18, 18);
        this.hasFrequency = hasFrequency;
        this.statusSupplier = statusSupplier;
    }

    @Override
    protected int getYImage(boolean hovering) {
        return 1;
    }

    @Override
    protected ResourceLocation getResource() {
        if (hasFrequency.getAsBoolean()) {
            switch (statusSupplier.getAsByte()) {
                case 1:
                    return READY;
                case 2:
                    return NO_FRAME;
                case 4:
                    return NEEDS_ENERGY;
                case 3:
                default:
                    return NO_LINK;
            }
        }
        return NO_FREQUENCY;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        super.renderButton(mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        blit(x, y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(getStatusDisplay(), mouseX, mouseY);
    }

    private ITextComponent getStatusDisplay() {
        if (hasFrequency.getAsBoolean()) {
            switch (statusSupplier.getAsByte()) {
                case 1:
                    return MekanismLang.TELEPORTER_READY.translateColored(EnumColor.DARK_GREEN);
                case 2:
                    return MekanismLang.TELEPORTER_NO_FRAME.translateColored(EnumColor.DARK_RED);
                case 4:
                    return MekanismLang.TELEPORTER_NEEDS_ENERGY.translateColored(EnumColor.DARK_RED);
                case 3:
                default:
                    return MekanismLang.TELEPORTER_NO_LINK.translateColored(EnumColor.DARK_RED);
            }
        }
        return MekanismLang.NO_FREQUENCY.translateColored(EnumColor.DARK_RED);
    }
}