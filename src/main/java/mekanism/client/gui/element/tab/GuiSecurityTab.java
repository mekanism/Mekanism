package mekanism.client.gui.element.tab;

import java.util.Arrays;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTileEntityElement;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketSecurityMode;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityData;
import mekanism.common.security.SecurityFrequency;
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
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSecurityTab<TILE extends TileEntity & ISecurityTile> extends GuiTileEntityElement<TILE> {

    private final Hand currentHand;
    private boolean isItem;

    public GuiSecurityTab(IGuiWrapper gui, TILE tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSecurityTab.png"), gui, def, tile);
        this.currentHand = Hand.MAIN_HAND;
    }

    public GuiSecurityTab(IGuiWrapper gui, ResourceLocation def, Hand hand) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSecurityTab.png"), gui, def, null);
        isItem = true;
        currentHand = hand;
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth + 176, guiHeight + 32, 26, 26);
    }

    @Override
    protected boolean inBounds(double xAxis, double yAxis) {
        return xAxis >= 179 && xAxis <= 197 && yAxis >= 36 && yAxis <= 54;
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth + 176, guiHeight + 32, 0, 0, 26, 26);
        SecurityMode mode = getSecurity();
        SecurityData data = MekanismClient.clientSecurityMap.get(getOwner());
        if (data != null && data.override) {
            mode = data.mode;
        }
        int renderX = 26 + (18 * mode.ordinal());
        if (getOwner() != null && getOwner().equals(minecraft.player.getUniqueID()) && (data == null || !data.override)) {
            guiObj.drawTexturedRect(guiWidth + 179, guiHeight + 36, renderX, inBounds(xAxis, yAxis) ? 0 : 18, 18, 18);
        } else {
            guiObj.drawTexturedRect(guiWidth + 179, guiHeight + 36, renderX, 36, 18, 18);
        }
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        minecraft.textureManager.bindTexture(RESOURCE);
        if (inBounds(xAxis, yAxis)) {
            ITextComponent securityComponent = TextComponentUtil.build(EnumColor.GRAY, Translation.of("mekanism.gui.security"), ": ",
                  isItem ? SecurityUtils.getSecurity(getItem(), Dist.CLIENT) : SecurityUtils.getSecurity(tileEntity, Dist.CLIENT));
            ITextComponent ownerComponent = OwnerDisplay.of(minecraft.player, getOwner(), getOwnerUsername()).getTextComponent();
            if (isItem ? SecurityUtils.isOverridden(getItem(), Dist.CLIENT) : SecurityUtils.isOverridden(tileEntity, Dist.CLIENT)) {
                displayTooltips(Arrays.asList(securityComponent, ownerComponent,
                      TextComponentUtil.build(EnumColor.RED, "(", Translation.of("mekanism.gui.overridden"), ")")
                ), xAxis, yAxis);
            } else {
                displayTooltips(Arrays.asList(securityComponent, ownerComponent), xAxis, yAxis);
            }
        }
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    //TODO: Is this used by anything, or more accurately SecurityFrequency in general
    private SecurityFrequency getFrequency() {
        if (isItem) {
            if (getItem().isEmpty() || !(getItem().getItem() instanceof ISecurityItem)) {
                minecraft.player.closeScreen();
                return null;
            }
            return SecurityUtils.getFrequency(getOwner());
        }
        return tileEntity.getSecurity().getFrequency();
    }

    private SecurityMode getSecurity() {
        if (!MekanismConfig.general.allowProtection.get()) {
            return SecurityMode.PUBLIC;
        }

        if (isItem) {
            if (getItem().isEmpty() || !(getItem().getItem() instanceof ISecurityItem)) {
                minecraft.player.closeScreen();
                return SecurityMode.PUBLIC;
            }
            return ((ISecurityItem) getItem().getItem()).getSecurity(getItem());
        }
        return tileEntity.getSecurity().getMode();
    }

    private UUID getOwner() {
        if (isItem) {
            if (getItem().isEmpty() || !(getItem().getItem() instanceof ISecurityItem)) {
                minecraft.player.closeScreen();
                return null;
            }
            return ((ISecurityItem) getItem().getItem()).getOwnerUUID(getItem());
        }
        return tileEntity.getSecurity().getOwnerUUID();
    }

    private String getOwnerUsername() {
        if (isItem) {
            if (getItem().isEmpty() || !(getItem().getItem() instanceof ISecurityItem)) {
                minecraft.player.closeScreen();
                return null;
            }
            return MekanismClient.clientUUIDMap.get(((ISecurityItem) getItem().getItem()).getOwnerUUID(getItem()));
        }
        return tileEntity.getSecurity().getClientOwner();
    }

    private ItemStack getItem() {
        return minecraft.player.getHeldItem(currentHand);
    }

    @Override
    public boolean preMouseClicked(double mouseX, double mouseY, int button) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && MekanismConfig.general.allowProtection.get()) {
            if (getOwner() != null && minecraft.player.getUniqueID().equals(getOwner())) {
                if (inBounds(mouseX, mouseY)) {
                    SecurityMode current = getSecurity();
                    int ordinalToSet = current.ordinal() < (SecurityMode.values().length - 1) ? current.ordinal() + 1 : 0;

                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                    if (isItem) {
                        Mekanism.packetHandler.sendToServer(new PacketSecurityMode(currentHand, SecurityMode.values()[ordinalToSet]));
                    } else {
                        Mekanism.packetHandler.sendToServer(new PacketSecurityMode(Coord4D.get(tileEntity), SecurityMode.values()[ordinalToSet]));
                    }
                }
            }
        }
    }
}