package mekanism.client.gui.filter;

import java.io.IOException;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public abstract class GuiModIDFilter<FILTER extends IModIDFilter, TILE extends TileEntityContainerBlock> extends
      GuiFilter<TILE> {

    protected String status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
    protected ItemStack renderStack = ItemStack.EMPTY;
    protected List<ItemStack> iterStacks;
    protected GuiTextField modIDText;
    protected boolean isNew = false;
    protected int stackSwitch = 0;
    protected int stackIndex = 0;
    protected FILTER origFilter;
    protected FILTER filter;
    protected int ticker = 0;

    protected GuiModIDFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    protected abstract void updateStackList(String modName);

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!modIDText.isFocused() || i == Keyboard.KEY_ESCAPE) {
            super.keyTyped(c, i);
        }
        if (modIDText.isFocused() && i == Keyboard.KEY_RETURN) {
            setModID();
            return;
        }
        if (Character.isLetter(c) || Character.isDigit(c) || TransporterFilter.SPECIAL_CHARS.contains(c)
              || isTextboxKey(c, i)) {
            modIDText.textboxKeyTyped(c, i);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        modIDText.updateCursorCounter();
        if (ticker > 0) {
            ticker--;
        } else {
            status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
        }
        if (stackSwitch > 0) {
            stackSwitch--;
        }
        if (stackSwitch == 0 && iterStacks != null && iterStacks.size() > 0) {
            stackSwitch = 20;
            if (stackIndex == -1 || stackIndex == iterStacks.size() - 1) {
                stackIndex = 0;
            } else if (stackIndex < iterStacks.size() - 1) {
                stackIndex++;
            }
            renderStack = iterStacks.get(stackIndex);
        } else if (iterStacks != null && iterStacks.size() == 0) {
            renderStack = ItemStack.EMPTY;
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            if (!modIDText.getText().isEmpty()) {
                setModID();
            }
            if (filter.getModID() != null && !filter.getModID().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler
                          .sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(0);
            } else {
                status = EnumColor.DARK_RED + LangUtils.localize("gui.modIDFilter.noID");
                ticker = 20;
            }
        } else if (guibutton.id == 1) {
            Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(0);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        buttonList.clear();
        addButtons(guiWidth, guiHeight);
        if (isNew) {
            buttonList.get(1).enabled = false;
        }
        modIDText = new GuiTextField(2, fontRenderer, guiWidth + 35, guiHeight + 47, 95, 12);
        modIDText.setMaxStringLength(TransporterFilter.MAX_LENGTH);
        modIDText.setFocused(true);
    }

    protected void setModID() {
        String modName = modIDText.getText();
        if (modName.isEmpty()) {
            status = EnumColor.DARK_RED + LangUtils.localize("gui.modIDFilter.noID");
            return;
        } else if (modName.equals(filter.getModID())) {
            status = EnumColor.DARK_RED + LangUtils.localize("gui.modIDFilter.sameID");
            return;
        }
        updateStackList(modName);
        filter.setModID(modName);
        modIDText.setText("");
    }
}