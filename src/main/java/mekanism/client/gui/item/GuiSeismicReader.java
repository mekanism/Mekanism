package mekanism.client.gui.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.APILang;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiArrowSelection;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
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

    private final List<BlockState> blockList = new ArrayList<>();
    private final Object2IntMap<Block> frequencies = new Object2IntOpenHashMap<>();
    private MekanismButton upButton;
    private MekanismButton downButton;
    private GuiScrollBar scrollBar;

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
        addButton(scrollBar = new GuiScrollBar(this, 126, 25, 131, blockList::size, () -> 1));
        addButton(new GuiArrowSelection(this, 76, 81, () -> {
            int currentLayer = scrollBar.getCurrentSelection();
            if (currentLayer >= 0) {
                return blockList.get(blockList.size() - 1 - currentLayer).getBlock().getTranslatedName();
            }
            return null;
        }));
        addButton(upButton = new MekanismImageButton(this, guiLeft + 126, guiTop + 11, 14,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "up.png"), () -> scrollBar.adjustScroll(1)));
        addButton(downButton = new MekanismImageButton(this, guiLeft + 126, guiTop + 156, 14,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "down.png"), () -> scrollBar.adjustScroll(-1)));
        updateEnabledButtons();
    }

    @Override
    public void tick() {
        super.tick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        int currentLayer = scrollBar.getCurrentSelection();
        upButton.active = currentLayer > 0;
        downButton.active = currentLayer + 1 < blockList.size();
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        int currentLayer = blockList.size() - scrollBar.getCurrentSelection() - 1;
        //Render the layer text scaled, so that it does not start overlapping past 100
        drawTextScaledBound(matrix, APILang.GENERIC.translate(currentLayer), 111, 87, screenTextColor(), 13);

        //TODO - V11: Eventually instead of just rendering the item stacks, it would be nice to be able to render the actual vertical column of blocks
        //Render the item stacks
        for (int i = 0; i < 9; i++) {
            int layer = currentLayer + (i - 4);
            if (0 <= layer && layer < blockList.size()) {
                BlockState state = blockList.get(layer);
                ItemStack stack = new ItemStack(state.getBlock());
                int renderX = 92;
                int renderY = 147 - 16 * i;
                if (i == 4) {
                    renderItem(matrix, stack, renderX, renderY);
                } else {
                    matrix.push();
                    matrix.translate(renderX, renderY, 0);
                    if (i < 4) {
                        matrix.translate(1.7F, 2.5F, 0);
                    } else {
                        matrix.translate(1.5F, 0, 0);
                    }
                    matrix.scale(0.8F, 0.8F, 0.8F);
                    renderItem(matrix, stack, 0, 0);
                    matrix.pop();
                }
            }
        }
        int frequency = 0;
        // Get the name from the stack and render it
        if (currentLayer >= 0) {
            Block block = blockList.get(currentLayer).getBlock();
            ITextComponent displayName = block.getTranslatedName();
            drawTextScaledBound(matrix, displayName, 10, 16, screenTextColor(), 57);
            frequency = frequencies.computeIntIfAbsent(block, b -> (int) blockList.stream().filter(blockState -> b == blockState.getBlock()).count());
        }
        drawTextScaledBound(matrix, MekanismLang.ABUNDANCY.translate(frequency), 10, 26, screenTextColor(), 57);
        MekanismRenderer.resetColor();
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }
}