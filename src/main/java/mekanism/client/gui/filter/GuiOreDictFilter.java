package mekanism.client.gui.filter;

import java.io.IOException;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.IOreDictFilter;
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
public abstract class GuiOreDictFilter<FILTER extends IOreDictFilter, TILE extends TileEntityContainerBlock> extends
      GuiFilter<TILE> {

    protected String status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
    protected ItemStack renderStack = ItemStack.EMPTY;
    protected List<ItemStack> iterStacks;
    protected GuiTextField oreDictText;
    protected boolean isNew = false;
    protected int stackSwitch = 0;
    protected int stackIndex = 0;
    protected int ticker = 0;
    protected FILTER origFilter;
    protected FILTER filter;

    protected GuiOreDictFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    protected abstract void updateStackList(String oreName);

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
        oreDictText = new GuiTextField(2, fontRenderer, guiWidth + 35, guiHeight + 47, 95, 12);
        oreDictText.setMaxStringLength(TransporterFilter.MAX_LENGTH);
        oreDictText.setFocused(true);
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!oreDictText.isFocused() || i == Keyboard.KEY_ESCAPE) {
            super.keyTyped(c, i);
        }
        if (oreDictText.isFocused() && i == Keyboard.KEY_RETURN) {
            setOreDictKey();
            return;
        }
        if (Character.isLetter(c) || Character.isDigit(c) || TransporterFilter.SPECIAL_CHARS.contains(c)
              || isTextboxKey(c, i)) {
            oreDictText.textboxKeyTyped(c, i);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            if (!oreDictText.getText().isEmpty()) {
                setOreDictKey();
            }
            if (filter.getOreDictName() != null && !filter.getOreDictName().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler
                          .sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(0);
            } else {
                status = EnumColor.DARK_RED + LangUtils.localize("gui.oredictFilter.noKey");
                ticker = 20;
            }
        } else if (guibutton.id == 1) {
            Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(0);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        oreDictText.updateCursorCounter();
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

    protected void setOreDictKey() {
        String oreName = oreDictText.getText();
        if (oreName.isEmpty()) {
            status = EnumColor.DARK_RED + LangUtils.localize("gui.oredictFilter.noKey");
            return;
        } else if (oreName.equals(filter.getOreDictName())) {
            status = EnumColor.DARK_RED + LangUtils.localize("gui.oredictFilter.sameKey");
            return;
        }
        updateStackList(oreName);
        filter.setOreDictName(oreName);
        oreDictText.setText("");
    }
}