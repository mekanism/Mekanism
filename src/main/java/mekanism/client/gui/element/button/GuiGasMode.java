package mekanism.client.gui.element.button;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class GuiGasMode extends MekanismImageButton {

    private static final ResourceLocation IDLE = MekanismUtils.getResource(ResourceType.GUI, "gas_mode_idle.png");
    private static final ResourceLocation EXCESS = MekanismUtils.getResource(ResourceType.GUI, "gas_mode_excess.png");
    private static final ResourceLocation DUMP = MekanismUtils.getResource(ResourceType.GUI, "gas_mode_dump.png");

    private final Tooltip dumpExcess;
    private final Tooltip dump;
    private final TextAlignment textSide;
    private final Supplier<GasMode> gasModeSupplier;

    public GuiGasMode(IGuiWrapper gui, int x, int y, boolean left, Supplier<GasMode> gasModeSupplier, BlockPos pos, int tank) {
        this(gui, x, y, left, gasModeSupplier, pos, tank, null, null);
    }

    public GuiGasMode(IGuiWrapper gui, int x, int y, boolean left, Supplier<GasMode> gasModeSupplier, BlockPos pos, int tank, Tooltip dumpExcess, Tooltip dump) {
        super(gui, x, y, 10, IDLE, (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.GAS_MODE_BUTTON, pos, tank)));
        this.textSide = left ? TextAlignment.RIGHT : TextAlignment.LEFT;
        this.gasModeSupplier = gasModeSupplier;
        this.dumpExcess = dumpExcess;
        this.dump = dump;
    }

    @Override
    protected ResourceLocation getResource() {
        return switch (gasModeSupplier.get()) {
            case DUMPING_EXCESS -> EXCESS;
            case DUMPING -> DUMP;
            default -> super.getResource();
        };
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //Draw the text next to the button
        int start = textSide == TextAlignment.RIGHT ? -69 : getWidth();
        drawScrollingString(guiGraphics, gasModeSupplier.get().getTextComponent(), start, 1, textSide, titleTextColor(), 69, 2, false);
        super.renderForeground(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        if (dumpExcess != null) {
            GasMode dumpMode = gasModeSupplier.get();
            if (dumpMode != GasMode.IDLE) {
                setTooltip(dumpMode == GasMode.DUMPING_EXCESS ? dumpExcess : dump);
            } else {
                clearTooltip();
            }
        }
    }
}