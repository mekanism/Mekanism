package mekanism.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class GuiGasMode extends MekanismImageButton {

    private static final ResourceLocation IDLE = MekanismUtils.getResource(ResourceType.GUI, "gas_mode_idle.png");
    private static final ResourceLocation EXCESS = MekanismUtils.getResource(ResourceType.GUI, "gas_mode_excess.png");
    private static final ResourceLocation DUMP = MekanismUtils.getResource(ResourceType.GUI, "gas_mode_dump.png");

    private final boolean left;
    private final Supplier<GasMode> gasModeSupplier;

    public GuiGasMode(IGuiWrapper gui, int x, int y, boolean left, Supplier<GasMode> gasModeSupplier, BlockPos pos, int tank) {
        super(gui, x, y, 10, IDLE, () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.GAS_MODE_BUTTON, pos, tank)));
        this.left = left;
        this.gasModeSupplier = gasModeSupplier;
    }

    @Override
    protected ResourceLocation getResource() {
        GasMode mode = gasModeSupplier.get();
        if (mode == GasMode.DUMPING_EXCESS) {
            return EXCESS;
        } else if (mode == GasMode.DUMPING) {
            return DUMP;
        }
        return super.getResource();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        //Draw the text next to the button
        ITextComponent component = gasModeSupplier.get().getTextComponent();
        int xPos = x - getGuiLeft();
        int yPos = y - getGuiTop();
        if (left) {
            drawTextScaledBound(matrix, component, xPos - 3 - (int) (getStringWidth(component) * getNeededScale(component, 66)), yPos + 1, titleTextColor(), 66);
        } else {
            drawTextScaledBound(matrix, component, xPos + width + 5, yPos + 1, titleTextColor(), 66);
        }
        super.renderForeground(matrix, mouseX, mouseY);
    }
}