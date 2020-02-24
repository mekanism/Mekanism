package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.element.GuiArrowSelection;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiScrollBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class GuiSeismicReader extends GuiMekanism<SeismicReaderContainer> {

    private List<BlockState> blockList = new ArrayList<>();
    private Object2IntMap<Block> frequencies = new Object2IntOpenHashMap<>();
    private MekanismButton upButton;
    private MekanismButton downButton;
    private double scroll;

    public GuiSeismicReader(SeismicReaderContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize = 147;
        ySize = 182;
        BlockPos pos = inv.player.getPosition();
        //Calculate all the blocks in the column
        for (BlockPos p : BlockPos.getAllInBoxMutable(new BlockPos(pos.getX(), 0, pos.getZ()), pos)) {
            blockList.add(inv.player.world.getBlockState(p));
        }
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 7, 11, 63, 49));
        addButton(new GuiInnerScreen(this, 74, 11, 51, 159));
        addButton(new GuiArrowSelection(this, 76, 81, () -> {
            int currentLayer = getCurrentLayer();
            if (currentLayer - 1 >= 0) {
                return blockList.get(currentLayer - 1).getBlock().getNameTextComponent();
            }
            return null;
        }));
        //Scroll bar
        addButton(new GuiScrollBar(this, 126, 25, 131, this::needsScrollBars, () -> scroll, value -> {
            scroll = value;
            updateButtons();
        }));
        addButton(upButton = new MekanismImageButton(this, getGuiLeft() + 126, getGuiTop() + 11, 14,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "up.png"), () -> adjustScroll(1)));
        addButton(downButton = new MekanismImageButton(this, getGuiLeft() + 126, getGuiTop() + 156, 14,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "down.png"), () -> adjustScroll(-1)));
        updateButtons();
    }

    private void updateButtons() {
        upButton.active = scroll > 0;
        downButton.active = scroll < 1;
    }

    private int getCurrentLayer() {
        if (needsScrollBars()) {
            int size = blockList.size();
            return size - (int) ((size - 0.5) * scroll);
        }
        return 1;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int currentLayer = getCurrentLayer();
        //Render the layer text scaled, so that it does not start overlapping past 100
        renderScaledText(MekanismLang.GENERIC.translate(currentLayer), 111, 87, 0x00CD00, 13);

        //TODO: Eventually instead of just rendering the item stacks, it would be nice to be able to render the actual vertical column of blocks
        //Render the item stacks
        for (int i = 0; i < 9; i++) {
            int layer = currentLayer + (i - 5);
            if (0 <= layer && layer < blockList.size()) {
                BlockState state = blockList.get(layer);
                ItemStack stack = new ItemStack(state.getBlock());
                int renderX = 92;
                int renderY = 147 - 16 * i;
                if (i == 4) {
                    renderItem(stack, renderX, renderY);
                } else {
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef(renderX, renderY, 0);
                    if (i < 4) {
                        RenderSystem.translatef(1.7F, 2.5F, 0);
                    } else {
                        RenderSystem.translatef(1.5F, 0, 0);
                    }
                    RenderSystem.scalef(0.8F, 0.8F, 0.8F);
                    renderItem(stack, 0, 0);
                    RenderSystem.popMatrix();
                }
            }
        }
        int frequency = 0;
        // Get the name from the stack and render it
        if (currentLayer - 1 >= 0) {
            Block block = blockList.get(currentLayer - 1).getBlock();
            ITextComponent displayName = block.getNameTextComponent();
            renderScaledText(displayName, 10, 16, 0x00CD00, 57);
            frequency = frequencies.computeIntIfAbsent(block, b -> (int) blockList.stream().filter(blockState -> b == blockState.getBlock()).count());
        }
        renderScaledText(MekanismLang.ABUNDANCY.translate(frequency), 10, 26, 0x00CD00, 57);
        MekanismRenderer.resetColor();
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void onClose() {
        super.onClose();
        blockList.clear();
    }

    private boolean needsScrollBars() {
        return blockList.size() > 1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta != 0 && needsScrollBars()) {
            adjustScroll(delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void adjustScroll(double delta) {
        int j = blockList.size() - 1;
        if (delta > 0) {
            delta = 1;
        } else {
            delta = -1;
        }
        scroll = (float) (scroll - delta / j);
        if (scroll < 0.0F) {
            scroll = 0.0F;
        } else if (scroll > 1.0F) {
            scroll = 1.0F;
        }
        updateButtons();
    }
}