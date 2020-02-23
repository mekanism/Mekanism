package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.element.GuiArrowSelection;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class GuiSeismicReader extends GuiMekanism<SeismicReaderContainer> {

    private List<BlockState> blockList = new ArrayList<>();
    private Object2IntMap<Block> frequencies = new Object2IntOpenHashMap<>();
    private MekanismButton upButton;
    private MekanismButton downButton;

    private int currentLayer;

    public GuiSeismicReader(SeismicReaderContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize = 137;
        ySize = 182;
        PlayerEntity player = inv.player;
        BlockPos pos = player.getPosition();
        //Calculate all the blocks in the column
        for (BlockPos p : BlockPos.getAllInBoxMutable(new BlockPos(pos.getX(), 0, pos.getZ()), pos)) {
            blockList.add(player.world.getBlockState(p));
        }
        currentLayer = Math.max(0, blockList.size() - 1);
        //TODO: Add scroll bar element
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 12, 11, 51, 159));
        addButton(new GuiInnerScreen(this, 67, 11, 63, 49));
        addButton(new GuiArrowSelection(this, 14, 81, () -> currentLayer - 1 >= 0 ? blockList.get(currentLayer - 1).getBlock().getNameTextComponent() : null));
        addButton(upButton = new MekanismImageButton(this, getGuiLeft() + 70, getGuiTop() + 75, 13,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "up.png"), () -> {
            currentLayer++;
            upButton.active = currentLayer + 1 <= blockList.size();
        }));
        addButton(downButton = new MekanismImageButton(this, getGuiLeft() + 70, getGuiTop() + 92, 13,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "down.png"), () -> {
            currentLayer--;
            downButton.active = currentLayer - 1 >= 1;
        }));
        upButton.active = currentLayer + 1 <= blockList.size();
        downButton.active = currentLayer - 1 >= 1;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        //Render the layer text scaled, so that it does not start overlapping past 100
        renderScaledText(MekanismLang.GENERIC.translate(currentLayer), 49, 87, 0x00CD00, 13);

        //TODO: Eventually instead of just rendering the item stacks, it would be nice to be able to render the actual vertical column of blocks
        //Render the item stacks
        for (int i = 0; i < 9; i++) {
            int layer = currentLayer + (i - 5);
            if (0 <= layer && layer < blockList.size()) {
                BlockState state = blockList.get(layer);
                ItemStack stack = new ItemStack(state.getBlock());
                int renderX = 30;
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
            renderScaledText(displayName, 70, 16, 0x00CD00, 57);
            frequency = frequencies.computeIntIfAbsent(block, b -> (int) blockList.stream().filter(blockState -> b == blockState.getBlock()).count());
        }
        renderScaledText(MekanismLang.ABUNDANCY.translate(frequency), 70, 26, 0x00CD00, 57);
        MekanismRenderer.resetColor();
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void onClose() {
        super.onClose();
        blockList.clear();
    }
}