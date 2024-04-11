package mekanism.client.gui.element.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.ITypedSecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.tab.GuiSecurityTab.SecurityInfoProvider;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteractionEntity;
import mekanism.common.network.to_server.PacketItemGuiInteract;
import mekanism.common.network.to_server.PacketItemGuiInteract.ItemGuiInteraction;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class GuiSecurityTab extends GuiInsetElement<SecurityInfoProvider<?>> {

    private static final ResourceLocation PUBLIC = MekanismUtils.getResource(ResourceType.GUI, "public.png");
    private static final ResourceLocation PRIVATE = MekanismUtils.getResource(ResourceType.GUI, "private.png");
    private static final ResourceLocation PROTECTED = MekanismUtils.getResource(ResourceType.GUI, "protected.png");

    @Nullable
    private final InteractionHand currentHand;
    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    public GuiSecurityTab(IGuiWrapper gui, BlockEntity tile) {
        this(gui, tile, 34);
    }

    public GuiSecurityTab(IGuiWrapper gui, BlockEntity tile, int y) {
        this(gui, SecurityInfoProvider.create(tile), y, null);
    }

    public GuiSecurityTab(IGuiWrapper gui, Entity entity, int y) {
        this(gui, new SecurityInfoProvider<>(IEntitySecurityUtils.INSTANCE, () -> entity), y);
    }

    public GuiSecurityTab(IGuiWrapper gui, SecurityInfoProvider<?> provider, int y) {
        this(gui, provider, y, null);
    }

    public GuiSecurityTab(IGuiWrapper gui, @NotNull InteractionHand hand) {
        this(gui, new SecurityInfoProvider<>(IItemSecurityUtils.INSTANCE, () -> minecraft.player.getItemInHand(hand)), 34, hand);
    }

    private GuiSecurityTab(IGuiWrapper gui, SecurityInfoProvider<?> provider, int y, @Nullable InteractionHand hand) {
        super(PUBLIC, gui, provider, gui.getXSize(), y, 26, 18, false);
        this.currentHand = hand;
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_SECURITY);
    }

    @Override
    protected ResourceLocation getOverlay() {
        return switch (dataSource.securityMode()) {
            case PUBLIC -> super.getOverlay();
            case PRIVATE -> PRIVATE;
            case TRUSTED -> PROTECTED;
        };
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        ISecurityObject security = dataSource.securityObject();
        if (security != null) {
            SecurityData data = SecurityUtils.get().getFinalData(security, true);
            List<Component> info = new ArrayList<>(3);
            info.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, data.mode()));
            info.add(OwnerDisplay.of(minecraft.player, security.getOwnerUUID(), security.getOwnerName()).getTextComponent());
            if (data.override()) {
                info.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
            }
            if (!info.equals(lastInfo)) {
                lastInfo = info;
                lastTooltip = TooltipUtils.create(info);
            }
        } else {
            lastTooltip = null;
            lastInfo = Collections.emptyList();
        }
        setTooltip(lastTooltip);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        ISecurityObject security = dataSource.securityObject();
        if (security != null && security.ownerMatches(minecraft.player)) {
            if (currentHand != null) {
                PacketUtils.sendToServer(new PacketItemGuiInteract(button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? ItemGuiInteraction.NEXT_SECURITY_MODE
                                                                                                         : ItemGuiInteraction.PREVIOUS_SECURITY_MODE, currentHand));
            } else {
                Object provider = dataSource.objectSupplier.get();
                if (provider instanceof BlockEntity tile) {
                    PacketUtils.sendToServer(new PacketGuiInteract(button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? GuiInteraction.NEXT_SECURITY_MODE
                                                                                                         : GuiInteraction.PREVIOUS_SECURITY_MODE, tile));
                } else if (provider instanceof Entity entity) {
                    PacketUtils.sendToServer(new PacketGuiInteract(button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? GuiInteractionEntity.NEXT_SECURITY_MODE
                                                                                                         : GuiInteractionEntity.PREVIOUS_SECURITY_MODE, entity));
                }
            }
        }
    }

    @Override
    public boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    public record SecurityInfoProvider<OBJECT>(Supplier<OBJECT> objectSupplier, Function<OBJECT, @Nullable ISecurityObject> securityProvider,
                                               Function<OBJECT, @Nullable IOwnerObject> ownerProvider) {

        public SecurityInfoProvider(ITypedSecurityUtils<OBJECT> securityUtils, Supplier<OBJECT> objectSupplier) {
            this(objectSupplier, securityUtils::securityCapability, securityUtils::ownerCapability);
        }

        public static SecurityInfoProvider<BlockEntity> create(BlockEntity tile) {
            return new SecurityInfoProvider<>(
                  () -> tile,
                  t -> {
                      if (t.getLevel() == null) {
                          return null;
                      }
                      return IBlockSecurityUtils.INSTANCE.securityCapability(t.getLevel(), t.getBlockPos(), t);
                  },
                  t -> {
                      if (t.getLevel() == null) {
                          return null;
                      }
                      return IBlockSecurityUtils.INSTANCE.ownerCapability(t.getLevel(), t.getBlockPos(), t);
                  }
            );
        }

        @Nullable
        public ISecurityObject securityObject() {
            return securityProvider.apply(objectSupplier.get());
        }

        SecurityMode securityMode() {
            OBJECT object = objectSupplier.get();
            return ISecurityUtils.INSTANCE.getSecurityMode(object, securityProvider, ownerProvider, true);
        }
    }
}