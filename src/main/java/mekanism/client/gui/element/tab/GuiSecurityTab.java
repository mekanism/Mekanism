package mekanism.client.gui.element.tab;

import java.util.function.Supplier;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteractionEntity;
import mekanism.common.network.to_server.PacketSecurityMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class GuiSecurityTab extends GuiInsetElement<Supplier<@Nullable Object>> {

    private static final ResourceLocation PUBLIC = MekanismUtils.getResource(ResourceType.GUI, "public.png");
    private static final ResourceLocation PRIVATE = MekanismUtils.getResource(ResourceType.GUI, "private.png");
    private static final ResourceLocation PROTECTED = MekanismUtils.getResource(ResourceType.GUI, "protected.png");

    @Nullable
    private final InteractionHand currentHand;

    public GuiSecurityTab(IGuiWrapper gui, Object provider) {
        this(gui, provider, 34);
    }

    public GuiSecurityTab(IGuiWrapper gui, Object provider, int y) {//TODO - 1.20.2: Do we want to validate object is either Entity or BlockEntity?
        this(gui, () -> provider, y, null);
    }

    public GuiSecurityTab(IGuiWrapper gui, @NotNull InteractionHand hand) {
        this(gui, () -> minecraft.player.getItemInHand(hand), 34, hand);
    }

    private GuiSecurityTab(IGuiWrapper gui, Supplier<Object> provider, int y, @Nullable InteractionHand hand) {
        super(PUBLIC, gui, provider, gui.getWidth(), y, 26, 18, false);
        this.currentHand = hand;
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_SECURITY);
    }

    @Override
    protected ResourceLocation getOverlay() {
        return switch (ISecurityUtils.INSTANCE.getSecurityMode(dataSource.get(), true)) {
            case PUBLIC -> super.getOverlay();
            case PRIVATE -> PRIVATE;
            case TRUSTED -> PROTECTED;
        };
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        ISecurityObject security = Capabilities.SECURITY_OBJECT.getCapability(dataSource.get());
        if (security != null) {
            SecurityData data = SecurityUtils.get().getFinalData(security, true);
            Component securityComponent = MekanismLang.SECURITY.translateColored(EnumColor.GRAY, data.mode());
            Component ownerComponent = OwnerDisplay.of(minecraft.player, security.getOwnerUUID(), security.getOwnerName()).getTextComponent();
            if (data.override()) {
                displayTooltips(guiGraphics, mouseX, mouseY, securityComponent, ownerComponent, MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
            } else {
                displayTooltips(guiGraphics, mouseX, mouseY, securityComponent, ownerComponent);
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        Object provider = dataSource.get();
        ISecurityObject security = Capabilities.SECURITY_OBJECT.getCapability(provider);
        if (security != null && security.ownerMatches(minecraft.player)) {
            if (currentHand != null) {
                Mekanism.packetHandler().sendToServer(new PacketSecurityMode(currentHand, button == GLFW.GLFW_MOUSE_BUTTON_LEFT));
            } else if (provider instanceof BlockEntity tile) {
                Mekanism.packetHandler().sendToServer(new PacketGuiInteract(button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? GuiInteraction.NEXT_SECURITY_MODE
                                                                                                                  : GuiInteraction.PREVIOUS_SECURITY_MODE, tile));
            } else if (provider instanceof Entity entity) {
                Mekanism.packetHandler().sendToServer(new PacketGuiInteract(button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? GuiInteractionEntity.NEXT_SECURITY_MODE
                                                                                                                  : GuiInteractionEntity.PREVIOUS_SECURITY_MODE, entity));
            }
        }
    }

    @Override
    public boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }
}