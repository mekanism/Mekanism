package mekanism.client.gui.element.tab;

import java.util.Arrays;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketSecurityMode;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSecurityTab<TILE extends TileEntity & ISecurityTile> extends GuiInsetElement<TILE> {

    private final Hand currentHand;
    private boolean isItem;

    public GuiSecurityTab(IGuiWrapper gui, TILE tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "security.png"), gui, def, tile, 176, 32, 26, 18);
        this.currentHand = Hand.MAIN_HAND;
    }

    public GuiSecurityTab(IGuiWrapper gui, ResourceLocation def, Hand hand) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "security.png"), gui, def, null, 176, 32, 26, 18);
        isItem = true;
        currentHand = hand;
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(0xFFE8AA97);
    }

    @Override
    protected int getYOffset(double mouseX, double mouseY) {
        UUID owner = getOwner();
        if (owner != null && owner.equals(minecraft.player.getUniqueID())) {
            SecurityData data = MekanismClient.clientSecurityMap.get(owner);
            if (data == null || !data.override) {
                return super.getYOffset(mouseX, mouseY);
            }
        }
        return 36;
    }

    @Override
    protected int getXOffset() {
        SecurityMode mode = getSecurity();
        SecurityData data = MekanismClient.clientSecurityMap.get(getOwner());
        if (data != null && data.override) {
            mode = data.mode;
        }
        return innerSize * mode.ordinal();
    }

    @Override
    protected int getTextureWidth() {
        return 54;
    }

    @Override
    protected int getTextureHeight() {
        return 54;
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        ITextComponent securityComponent = TextComponentUtil.build(EnumColor.GRAY, Translation.of("gui.mekanism.security"), ": ",
              isItem ? SecurityUtils.getSecurity(getItem(), Dist.CLIENT) : SecurityUtils.getSecurity(tileEntity, Dist.CLIENT));
        ITextComponent ownerComponent = OwnerDisplay.of(minecraft.player, getOwner(), getOwnerUsername()).getTextComponent();
        if (isItem ? SecurityUtils.isOverridden(getItem(), Dist.CLIENT) : SecurityUtils.isOverridden(tileEntity, Dist.CLIENT)) {
            displayTooltips(Arrays.asList(securityComponent, ownerComponent,
                  TextComponentUtil.build(EnumColor.RED, "(", Translation.of("gui.mekanism.overridden"), ")")
            ), mouseX, mouseY);
        } else {
            displayTooltips(Arrays.asList(securityComponent, ownerComponent), mouseX, mouseY);
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
        return tileEntity.getSecurity().getMode();
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
        return tileEntity.getSecurity().getOwnerUUID();
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
        return tileEntity.getSecurity().getClientOwner();
    }

    private ItemStack getItem() {
        return minecraft.player.getHeldItem(currentHand);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (MekanismConfig.general.allowProtection.get()) {
            UUID owner = getOwner();
            if (owner != null && minecraft.player.getUniqueID().equals(owner)) {
                SecurityMode current = getSecurity();
                int ordinalToSet = current.ordinal() < (SecurityMode.values().length - 1) ? current.ordinal() + 1 : 0;
                SecurityMode mode = SecurityMode.values()[ordinalToSet];
                if (isItem) {
                    Mekanism.packetHandler.sendToServer(new PacketSecurityMode(currentHand, mode));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketSecurityMode(Coord4D.get(tileEntity), mode));
                }
            }
        }
    }
}