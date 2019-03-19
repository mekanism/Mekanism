package mekanism.client.gui.element;

import java.util.Arrays;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.network.PacketSecurityMode.SecurityModeMessage;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityData;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.SecurityUtils;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSecurityTab extends GuiTileEntityElement<TileEntity> {

    private final EnumHand currentHand;
    private boolean isItem;

    public GuiSecurityTab(IGuiWrapper gui, TileEntity tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSecurityTab.png"), gui, def, tile);
        this.currentHand = EnumHand.MAIN_HAND;
    }

    public GuiSecurityTab(IGuiWrapper gui, ResourceLocation def, EnumHand hand) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSecurityTab.png"), gui, def, null);
        isItem = true;
        currentHand = hand;
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth + 176, guiHeight + 32, 26, 26);
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= 179 && xAxis <= 197 && yAxis >= 36 && yAxis <= 54;
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        mc.renderEngine.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth + 176, guiHeight + 32, 0, 0, 26, 26);
        SecurityMode mode = getSecurity();
        SecurityData data = MekanismClient.clientSecurityMap.get(getOwner());
        if (data != null && data.override) {
            mode = data.mode;
        }
        int renderX = 26 + (18 * mode.ordinal());
        if (getOwner() != null && getOwner().equals(mc.player.getUniqueID()) && (data == null || !data.override)) {
            guiObj.drawTexturedRect(guiWidth + 179, guiHeight + 36, renderX, inBounds(xAxis, yAxis) ? 0 : 18, 18, 18);
        } else {
            guiObj.drawTexturedRect(guiWidth + 179, guiHeight + 36, renderX, 36, 18, 18);
        }
        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        mc.renderEngine.bindTexture(RESOURCE);
        if (inBounds(xAxis, yAxis)) {
            String securityDisplay = isItem ? SecurityUtils.getSecurityDisplay(getItem(), Side.CLIENT)
                  : SecurityUtils.getSecurityDisplay(tileEntity, Side.CLIENT);
            String securityText = EnumColor.GREY + LangUtils.localize("gui.security") + ": " + securityDisplay;
            String ownerText = SecurityUtils.getOwnerDisplay(mc.player, getOwnerUsername());
            String overrideText = EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")";

            if (isItem ? SecurityUtils.isOverridden(getItem(), Side.CLIENT)
                  : SecurityUtils.isOverridden(tileEntity, Side.CLIENT)) {
                displayTooltips(Arrays.asList(securityText, ownerText, overrideText), xAxis, yAxis);
            } else {
                displayTooltips(Arrays.asList(securityText, ownerText), xAxis, yAxis);
            }
        }
        mc.renderEngine.bindTexture(defaultLocation);
    }

    private SecurityFrequency getFrequency() {
        if (isItem) {
            if (getItem().isEmpty() || !(getItem().getItem() instanceof ISecurityItem)) {
                mc.player.closeScreen();
                return null;
            }
            return SecurityUtils.getFrequency(getOwner());
        }
        return ((ISecurityTile) tileEntity).getSecurity().getFrequency();
    }

    private SecurityMode getSecurity() {
        if (!general.allowProtection) {
            return SecurityMode.PUBLIC;
        }

        if (isItem) {
            if (getItem().isEmpty() || !(getItem().getItem() instanceof ISecurityItem)) {
                mc.player.closeScreen();
                return SecurityMode.PUBLIC;
            }

            return ((ISecurityItem) getItem().getItem()).getSecurity(getItem());
        } else {
            return ((ISecurityTile) tileEntity).getSecurity().getMode();
        }
    }

    private UUID getOwner() {
        if (isItem) {
            if (getItem().isEmpty() || !(getItem().getItem() instanceof ISecurityItem)) {
                mc.player.closeScreen();
                return null;
            }
            return ((ISecurityItem) getItem().getItem()).getOwnerUUID(getItem());
        }
        return ((ISecurityTile) tileEntity).getSecurity().getOwnerUUID();
    }

    private String getOwnerUsername() {
        if (isItem) {
            if (getItem().isEmpty() || !(getItem().getItem() instanceof ISecurityItem)) {
                mc.player.closeScreen();
                return null;
            }
            return MekanismClient.clientUUIDMap.get(((ISecurityItem) getItem().getItem()).getOwnerUUID(getItem()));
        }
        return ((ISecurityTile) tileEntity).getSecurity().getClientOwner();
    }

    private ItemStack getItem() {
        return mc.player.getHeldItem(currentHand);
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (button == 0 && general.allowProtection) {
            if (getOwner() != null && mc.player.getUniqueID().equals(getOwner())) {
                if (inBounds(xAxis, yAxis)) {
                    SecurityMode current = getSecurity();
                    int ordinalToSet =
                          current.ordinal() < (SecurityMode.values().length - 1) ? current.ordinal() + 1 : 0;

                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);

                    if (isItem) {
                        Mekanism.packetHandler
                              .sendToServer(new SecurityModeMessage(currentHand, SecurityMode.values()[ordinalToSet]));
                    } else {
                        Mekanism.packetHandler.sendToServer(
                              new SecurityModeMessage(Coord4D.get(tileEntity), SecurityMode.values()[ordinalToSet]));
                    }
                }
            }
        }
    }
}