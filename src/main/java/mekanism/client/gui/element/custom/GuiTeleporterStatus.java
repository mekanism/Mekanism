package mekanism.client.gui.element.custom;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityTeleporter.TeleporterStatus;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class GuiTeleporterStatus extends GuiTexturedElement {

    private static final Identifier NEEDS_ENERGY = Mekanism.rl("teleporter/needs_energy");
    private static final Identifier NO_FRAME = Mekanism.rl("teleporter/no_frame");
    private static final Identifier NO_FREQUENCY = Mekanism.rl("teleporter/no_frequency");
    private static final Identifier NO_DESTINATION = Mekanism.rl("teleporter/no_link");
    private static final Identifier READY = Mekanism.rl("teleporter/ready");
    private static final Map<TeleporterStatus, Tooltip> CACHED_TOOLTIPS = new EnumMap<>(TeleporterStatus.class);

    private final BooleanSupplier hasFrequency;
    private final Supplier<TeleporterStatus> statusSupplier;

    public GuiTeleporterStatus(IGuiWrapper gui, BooleanSupplier hasFrequency, Supplier<TeleporterStatus> statusSupplier) {
        super(NO_FREQUENCY, gui, 6, 6, 18, 18);
        this.hasFrequency = hasFrequency;
        this.statusSupplier = statusSupplier;
        setButtonBackground(ButtonBackground.DEFAULT);
    }

    @Nullable
    @Override
    protected Identifier getButtonVariant(boolean hoveredOrFocused) {
        return ButtonBackground.DEFAULT.base();//TODO - 26.2: check me
    }

    @Override
    protected Identifier getResource() {
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
    public void updateTooltip(int mouseX, int mouseY) {
        TeleporterStatus status = hasFrequency.getAsBoolean() ? statusSupplier.get() : TeleporterStatus.NO_FREQUENCY;
        Tooltip statusDisplay = CACHED_TOOLTIPS.computeIfAbsent(status, s -> Tooltip.create(s.getTextComponent()));
        setTooltip(statusDisplay);
    }
}