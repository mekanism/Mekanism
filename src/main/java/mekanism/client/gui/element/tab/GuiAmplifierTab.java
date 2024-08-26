package mekanism.client.gui.element.tab;

import java.util.EnumMap;
import java.util.Map;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.laser.TileEntityLaserAmplifier.RedstoneOutput;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class GuiAmplifierTab extends GuiInsetElement<TileEntityLaserAmplifier> {

    private static final ResourceLocation OFF = MekanismUtils.getResource(ResourceType.GUI, "amplifier_off.png");
    private static final ResourceLocation ENTITY = MekanismUtils.getResource(ResourceType.GUI, "amplifier_entity.png");
    private static final ResourceLocation CONTENTS = MekanismUtils.getResource(ResourceType.GUI, "amplifier_contents.png");

    private final Map<RedstoneOutput, Tooltip> tooltips = new EnumMap<>(RedstoneOutput.class);

    public GuiAmplifierTab(IGuiWrapper gui, TileEntityLaserAmplifier tile) {
        super(OFF, gui, tile, gui.getXSize(), 109, 26, 18, false);
    }

    @Override
    protected ResourceLocation getOverlay() {
        return switch (dataSource.getOutputMode()) {
            case ENTITY_DETECTION -> ENTITY;
            case ENERGY_CONTENTS -> CONTENTS;
            default -> super.getOverlay();
        };
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(tooltips.computeIfAbsent(dataSource.getOutputMode(), mode -> TooltipUtils.create(MekanismLang.REDSTONE_OUTPUT.translate(mode))));
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        PacketUtils.sendToServer(new PacketGuiInteract(button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? GuiInteraction.NEXT_MODE : GuiInteraction.PREVIOUS_MODE, dataSource));
    }

    @Override
    public boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_LASER_AMPLIFIER);
    }
}