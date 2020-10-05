package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.lib.security.ISecurityTile.SecurityMode;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.PacketSecurityMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;

public class GuiSecurityTab<TILE extends TileEntity & ISecurityTile> extends GuiInsetElement<TILE> {

    private static final ResourceLocation PUBLIC = MekanismUtils.getResource(ResourceType.GUI, "public.png");
    private static final ResourceLocation PRIVATE = MekanismUtils.getResource(ResourceType.GUI, "private.png");
    private static final ResourceLocation PROTECTED = MekanismUtils.getResource(ResourceType.GUI, "protected.png");

    private final Hand currentHand;
    private boolean isItem;

    public GuiSecurityTab(IGuiWrapper gui, TILE tile) {
        super(PUBLIC, gui, tile, gui.getWidth(), 34, 26, 18, false);
        this.currentHand = Hand.MAIN_HAND;
    }

    public GuiSecurityTab(IGuiWrapper gui, Hand hand) {
        super(PUBLIC, gui, null, gui.getWidth(), 34, 26, 18, false);
        isItem = true;
        currentHand = hand;
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_SECURITY);
    }

    @Override
    protected ResourceLocation getOverlay() {
        SecurityMode mode = getSecurity();
        SecurityData data = MekanismClient.clientSecurityMap.get(getOwner());
        if (data != null && data.override) {
            mode = data.mode;
        }
        if (mode == SecurityMode.PRIVATE) {
            return PRIVATE;
        } else if (mode == SecurityMode.TRUSTED) {
            return PROTECTED;
        }
        return super.getOverlay();
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        ITextComponent securityComponent = MekanismLang.SECURITY.translateColored(EnumColor.GRAY, isItem ? SecurityUtils.getSecurity(getItem(), Dist.CLIENT)
                                                                                                         : SecurityUtils.getSecurity(tile, Dist.CLIENT));
        ITextComponent ownerComponent = OwnerDisplay.of(minecraft.player, getOwner(), getOwnerUsername()).getTextComponent();
        if (isItem ? SecurityUtils.isOverridden(getItem(), Dist.CLIENT) : SecurityUtils.isOverridden(tile, Dist.CLIENT)) {
            displayTooltips(matrix, Arrays.asList(securityComponent, ownerComponent, MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED)), mouseX, mouseY);
        } else {
            displayTooltips(matrix, Arrays.asList(securityComponent, ownerComponent), mouseX, mouseY);
        }
    }

    private SecurityMode getSecurity() {
        if (!MekanismConfig.general.allowProtection.get()) {
            return SecurityMode.PUBLIC;
        }

        if (isItem) {
            ItemStack stack = getItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof ISecurityItem)) {
                minecraft.player.closeScreen();
                return SecurityMode.PUBLIC;
            }
            return ((ISecurityItem) stack.getItem()).getSecurity(stack);
        }
        return tile.getSecurity().getMode();
    }

    private UUID getOwner() {
        if (isItem) {
            ItemStack stack = getItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof ISecurityItem)) {
                minecraft.player.closeScreen();
                return null;
            }
            return ((ISecurityItem) stack.getItem()).getOwnerUUID(stack);
        }
        return tile.getSecurity().getOwnerUUID();
    }

    private String getOwnerUsername() {
        if (isItem) {
            ItemStack stack = getItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof ISecurityItem)) {
                minecraft.player.closeScreen();
                return null;
            }
            return MekanismClient.clientUUIDMap.get(((ISecurityItem) stack.getItem()).getOwnerUUID(stack));
        }
        return tile.getSecurity().getClientOwner();
    }

    private ItemStack getItem() {
        return minecraft.player.getHeldItem(currentHand);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (MekanismConfig.general.allowProtection.get()) {
            UUID owner = getOwner();
            if (owner != null && minecraft.player.getUniqueID().equals(owner)) {
                if (isItem) {
                    Mekanism.packetHandler.sendToServer(new PacketSecurityMode(currentHand, getSecurity().getNext()));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_SECURITY_MODE, tile));
                }
            }
        }
    }
}