package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.inventory.container.tile.filter.DMItemStackFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiMItemStackFilter extends GuiItemStackFilter<MItemStackFilter, TileEntityDigitalMiner, DMItemStackFilterContainer> {

    private Button fuzzyButton;

    public GuiMItemStackFilter(DMItemStackFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void addButtons() {
        addButton(saveButton = new TranslationButton(guiLeft + 27, guiTop + 62, 60, 20, "gui.mekanism.save", onPress -> {
            if (!filter.getItemStack().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
            } else {
                status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.itemFilter.noItem"));
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(guiLeft + 89, guiTop + 62, 60, 20, "gui.mekanism.delete", onPress -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
        }));
        addButton(new MekanismImageButton(guiLeft + 5, guiTop + 5, 11, 14, getButtonLocation("back"),
              onPress -> sendPacketToServer(isNew ? ClickedTileButton.DM_SELECT_FILTER_TYPE : ClickedTileButton.DIGITAL_MINER_CONFIG)));
        addButton(new MekanismImageButton(guiLeft + 148, guiTop + 45, 14, 16, getButtonLocation("exclamation"),
              onPress -> filter.requireStack = !filter.requireStack,
              getOnHoverReplace(filter)));
        addButton(fuzzyButton = new MekanismImageButton(guiLeft + 15, guiTop + 45, 14, getButtonLocation("fuzzy"),
              onPress -> filter.fuzzy = !filter.fuzzy,
              getOnHover(TextComponentUtil.build(Translation.of("gui.mekanism.digitalMiner.fuzzyMode"), ": ", YesNo.of(filter.fuzzy)))));
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        if (!filter.getItemStack().isEmpty()) {
            renderScaledText(filter.getItemStack().getDisplayName(), 35, 41, 0x00CD00, 107);
        }
        renderItem(filter.getItemStack(), 12, 19);
        renderItem(filter.replaceStack, 149, 19);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - guiLeft;
            double yAxis = mouseY - guiTop;
            if (overTypeInput(xAxis, yAxis)) {
                ItemStack stack = minecraft.player.inventory.getItemStack();
                if (!stack.isEmpty() && !InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    if (stack.getItem() instanceof BlockItem) {
                        if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                            filter.setItemStack(stack.copy());
                            filter.getItemStack().setCount(1);
                        }
                    }
                } else if (stack.isEmpty() && InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    filter.setItemStack(ItemStack.EMPTY);
                }
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            } else {
                minerFilterClickCommon(xAxis, yAxis, filter);
            }
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "miner_filter.png");
    }
}