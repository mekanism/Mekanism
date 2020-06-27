package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiAmplifierTab extends GuiInsetElement<TileEntityLaserAmplifier> {

    private static final ResourceLocation OFF = MekanismUtils.getResource(ResourceType.GUI, "amplifier_off.png");
    private static final ResourceLocation ENTITY = MekanismUtils.getResource(ResourceType.GUI, "amplifier_entity.png");
    private static final ResourceLocation CONTENTS = MekanismUtils.getResource(ResourceType.GUI, "amplifier_contents.png");

    public GuiAmplifierTab(IGuiWrapper gui, TileEntityLaserAmplifier tile) {
        super(OFF, gui, tile, -26, 138, 26, 18, true);
    }

    @Override
    protected ResourceLocation getOverlay() {
        switch (tile.outputMode) {
            case ENTITY_DETECTION:
                return ENTITY;
            case ENERGY_CONTENTS:
                return CONTENTS;
        }
        return super.getOverlay();
    }

    @Override
    public void func_230443_a_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        displayTooltip(matrix, MekanismLang.REDSTONE_OUTPUT.translate(tile.outputMode), mouseX, mouseY);
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_MODE, tile));
    }
}