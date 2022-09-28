package mekanism.client.gui.item;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.NotNull;

public class GuiSeismicReader extends GuiMekanism<SeismicReaderContainer> {

    private final List<BlockState> blockList = new ArrayList<>();
    private final Object2IntMap<Block> frequencies = new Object2IntOpenHashMap<>();
    private final int minHeight;
    private MekanismButton upButton;
    private MekanismButton downButton;
    private GuiScrollBar scrollBar;

    public GuiSeismicReader(SeismicReaderContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = 147;
        imageHeight = 182;
        this.minHeight = inv.player.level.getMinBuildHeight();
        BlockPos pos = inv.player.blockPosition();
        //Calculate all the blocks in the column
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(pos.getX(), minHeight, pos.getZ()), pos)) {
            blockList.add(inv.player.level.getBlockState(p));
        }
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 7, 11, 63, 49));
        addRenderableWidget(new GuiInnerScreen(this, 74, 11, 51, 159));
        scrollBar = addRenderableWidget(new GuiScrollBar(this, 126, 25, 131, blockList::size, () -> 1));
        addRenderableWidget(new GuiArrowSelection(this, 76, 81, () -> {
            int currentLayer = scrollBar.getCurrentSelection();
            if (currentLayer >= 0) {
                return blockList.get(blockList.size() - 1 - currentLayer).getBlock().getName();
            }
            return null;
        }));
        upButton = addRenderableWidget(new MekanismImageButton(this, 126, 11, 14,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "up.png"), () -> scrollBar.adjustScroll(1)));
        downButton = addRenderableWidget(new MekanismImageButton(this, 126, 156, 14,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "down.png"), () -> scrollBar.adjustScroll(-1)));
        updateEnabledButtons();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        int currentLayer = scrollBar.getCurrentSelection();
        upButton.active = currentLayer > 0;
        downButton.active = currentLayer + 1 < blockList.size();
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        int currentLayer = blockList.size() - scrollBar.getCurrentSelection() - 1;
        //Render the layer text scaled, so that it does not start overlapping past 100
        drawTextScaledBound(matrix, TextComponentUtil.build(minHeight + currentLayer), 111, 87, screenTextColor(), 13);

        //TODO - V11: Eventually instead of just rendering the item stacks, it would be nice to be able to render the actual vertical column of blocks
        //Render the item stacks or fluids
        for (int i = 0; i < 9; i++) {
            int layer = currentLayer + (i - 4);
            if (0 <= layer && layer < blockList.size()) {
                BlockState state = blockList.get(layer);
                ItemStack stack = new ItemStack(state.getBlock());
                RenderTarget renderTarget;
                if (stack.isEmpty()) {
                    Fluid fluid = Fluids.EMPTY;
                    if (state.getBlock() instanceof LiquidBlock liquidBlock) {
                        fluid = liquidBlock.getFluid();
                    } else if (state.getBlock() instanceof IFluidBlock fluidBlock) {
                        fluid = fluidBlock.getFluid();
                    } else if (state.getBlock() instanceof BubbleColumnBlock bubbleColumn) {
                        fluid = bubbleColumn.getFluidState(state).getType();
                    }
                    if (fluid == Fluids.EMPTY) {
                        continue;
                    }
                    IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(fluid);
                    renderTarget = (poseStack, x, y) -> {
                        MekanismRenderer.color(properties.getTintColor());
                        TextureAtlasSprite texture = MekanismRenderer.getSprite(properties.getStillTexture());
                        GuiUtils.drawSprite(poseStack, x, y, 16, 16, getBlitOffset(), texture);
                        MekanismRenderer.resetColor();
                    };
                } else {
                    renderTarget = (poseStack, x, y) -> renderItem(poseStack, stack, x, y);
                }
                int renderX = 92;
                int renderY = 146 - 16 * i;
                if (i == 4) {
                    renderTarget.render(matrix, renderX, renderY);
                } else {
                    matrix.pushPose();
                    matrix.translate(renderX, renderY, 0);
                    if (i < 4) {
                        matrix.translate(1.7F, 2.5F, 0);
                    } else {
                        matrix.translate(1.5F, 0, 0);
                    }
                    matrix.scale(0.8F, 0.8F, 0.8F);
                    renderTarget.render(matrix, 0, 0);
                    matrix.popPose();
                }
            }
        }
        int frequency = 0;
        // Get the name from the stack and render it
        if (currentLayer >= 0) {
            Block block = blockList.get(currentLayer).getBlock();
            Component displayName = block.getName();
            drawTextScaledBound(matrix, displayName, 10, 16, screenTextColor(), 57);
            frequency = frequencies.computeIfAbsent(block, b -> (int) blockList.stream().filter(blockState -> b == blockState.getBlock()).count());
        }
        drawTextScaledBound(matrix, MekanismLang.ABUNDANCY.translate(frequency), 10, 26, screenTextColor(), 57);
        MekanismRenderer.resetColor();
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta) || scrollBar.adjustScroll(delta);
    }

    @FunctionalInterface
    private interface RenderTarget {

        void render(PoseStack poseStack, int x, int y);
    }
}