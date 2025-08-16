package mekanism.client.gui.element.custom;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.tile.TileEntityTeleporter.TeleporterStatus;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiTeleporterStatus extends GuiTexturedElement {

    private static final ResourceLocation NEEDS_ENERGY = MekanismUtils.getResource(ResourceType.GUI, "teleporter_needs_energy.png");
    private static final ResourceLocation NO_FRAME = MekanismUtils.getResource(ResourceType.GUI, "teleporter_no_frame.png");
    private static final ResourceLocation NO_FREQUENCY = MekanismUtils.getResource(ResourceType.GUI, "teleporter_no_frequency.png");
    private static final ResourceLocation NO_DESTINATION = MekanismUtils.getResource(ResourceType.GUI, "teleporter_no_link.png");
    private static final ResourceLocation READY = MekanismUtils.getResource(ResourceType.GUI, "teleporter_ready.png");
    private static final Map<TeleporterStatus, Tooltip> CACHED_TOOLTIPS = new EnumMap<>(TeleporterStatus.class);

    private final BooleanSupplier hasFrequency;
    private final Supplier<TeleporterStatus> statusSupplier;

    public GuiTeleporterStatus(IGuiWrapper gui, BooleanSupplier hasFrequency, Supplier<TeleporterStatus> statusSupplier) {
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
            return switch (statusSupplier.get()) {
                case READY -> READY;
                case NO_FRAME -> NO_FRAME;
                case NOT_ENOUGH_ENERGY -> NEEDS_ENERGY;
                default -> NO_DESTINATION;
            };
        }
        return NO_FREQUENCY;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(getResource(), relativeX, relativeY, 0, 0, width, height, width, height);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        TeleporterStatus status = hasFrequency.getAsBoolean() ? statusSupplier.get() : TeleporterStatus.NO_FREQUENCY;
        Tooltip statusDisplay = CACHED_TOOLTIPS.computeIfAbsent(status, s -> Tooltip.create(s.getTextComponent()));
        setTooltip(statusDisplay);
    }
}