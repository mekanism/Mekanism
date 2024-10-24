package mekanism.client.gui.item;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.text.TextComponentUtil;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class GuiSeismicReader extends GuiMekanism<SeismicReaderContainer> {

    private final List<BlockInfo<?>> blockList = new ArrayList<>();
    private final Reference2IntMap<Block> blockFrequencies = new Reference2IntOpenHashMap<>();
    private final Reference2IntMap<FluidType> fluidFrequencies = new Reference2IntOpenHashMap<>();
    private final int minHeight;
    private MekanismButton upButton;
    private MekanismButton downButton;
    private GuiScrollBar scrollBar;

    public GuiSeismicReader(SeismicReaderContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = 150;
        imageHeight = 182;
        Player player = inv.player;
        Level level = player.level();
        this.minHeight = level.getMinBuildHeight();
        BlockPos pos = player.blockPosition();
        //Calculate all the blocks in the column
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(pos.getX(), minHeight, pos.getZ()), pos)) {
            BlockState state = level.getBlockState(p);
            if (state.isAir()) {//Ensure all types of air are treated as air for calculations
                state = Blocks.AIR.defaultBlockState();
            }
            Block block = state.getBlock();
            blockFrequencies.mergeInt(block, 1, Integer::sum);
            //Try to get the clone item stack as maybe it has one, though it might not have a corresponding block
            ItemStack stack = state.getCloneItemStack(new BlockHitResult(p.getCenter().relative(Direction.UP, 0.5), Direction.UP, p, false), level, p, player);
            if (stack.isEmpty()) {
                Fluid fluid = Fluids.EMPTY;
                if (block instanceof LiquidBlock liquidBlock) {
                    fluid = liquidBlock.fluid;
                } else if (block instanceof BubbleColumnBlock) {
                    fluid = level.getFluidState(p).getType();
                }
                if (fluid == Fluids.EMPTY) {
                    blockList.add(new BlockInfo<>(state, state, null));
                } else {
                    FluidType fluidType = fluid.getFluidType();
                    blockList.add(new BlockInfo<>(state, fluidType, (graphics, f, x, y) -> {
                        IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(f);
                        MekanismRenderer.color(graphics, properties.getTintColor());
                        TextureAtlasSprite texture = MekanismRenderer.getSprite(properties.getStillTexture());
                        graphics.blit(x, y, 0, 16, 16, texture);
                        MekanismRenderer.resetColor(graphics);
                    }));
                    fluidFrequencies.mergeInt(fluidType, 1, Integer::sum);
                }
            } else {
                blockList.add(new BlockInfo<>(state, stack, this::renderItem));
                FluidState fluid = state.getFluidState();
                if (!fluid.isEmpty()) {//Take the fluid into account for frequency count
                    //TODO: Do we want to render the fact that it is fluid logged in some way?
                    fluidFrequencies.mergeInt(fluid.getFluidType(), 1, Integer::sum);
                }
            }
        }
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 5, 11, 69, 50, () -> {
            // Get the name from the stack and render it
            int currentLayer = getCurrentLayer();
            if (currentLayer >= 0) {
                List<Component> text = new ArrayList<>(4);
                BlockInfo<?> blockInfo = blockList.get(currentLayer);
                BlockState state = blockInfo.state();
                Block block = state.getBlock();
                if (!(block instanceof LiquidBlock)) {
                    //If the block is a liquid, let the fluid handling display and calculate the quantity
                    //Note: Bubble columns, still get counted so that we display it is a bubble column, and how many there are
                    //TODO: Do we want to try and make it so that the first few lines of the block's name wraps instead of scrolls
                    // for very long names like waxed oxidized copper stairs?
                    text.add(block.getName());
                    text.add(MekanismLang.ABUNDANCY.translate(blockFrequencies.getInt(block)));
                }
                if (blockInfo.type() instanceof FluidType fluidType) {//TODO: Improve this so it actually displays for fluid logged blocks
                    text.add(fluidType.getDescription());
                    text.add(MekanismLang.ABUNDANCY.translate(fluidFrequencies.getInt(fluidType)));
                }
                return text;
            }
            return Collections.emptyList();
        }).padding(3));
        addRenderableWidget(new GuiInnerScreen(this, 77, 11, 51, 160));
        scrollBar = addRenderableWidget(new GuiScrollBar(this, 129, 25, 132, blockList::size, () -> 1));
        addRenderableWidget(new GuiArrowSelection(this, 79, 81, () -> TextComponentUtil.build(minHeight + getCurrentLayer())));
        upButton = addRenderableWidget(new MekanismImageButton(this, 129, 11, 14,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "up.png"), (element, mouseX, mouseY) -> scrollBar.adjustScroll(1)));
        downButton = addRenderableWidget(new MekanismImageButton(this, 129, 157, 14,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "down.png"), (element, mouseX, mouseY) -> scrollBar.adjustScroll(-1)));
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

    private int getCurrentLayer() {
        return blockList.size() - scrollBar.getCurrentSelection() - 1;
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //TODO - V11: Eventually instead of just rendering the item stacks, it would be nice to be able to render the actual vertical column of blocks
        //Render the item stacks or fluids
        int currentLayer = getCurrentLayer();
        for (int i = 0; i < 9; i++) {
            int layer = currentLayer + (i - 4);
            if (0 <= layer && layer < blockList.size()) {
                BlockInfo<?> info = blockList.get(layer);
                if (info.renderTarget == null) {
                    continue;
                }
                int renderX = 95;
                int renderY = 146 - 16 * i;
                if (i == 4) {
                    info.render(guiGraphics, renderX, renderY);
                } else {
                    PoseStack pose = guiGraphics.pose();
                    pose.pushPose();
                    pose.translate(renderX, renderY, 0);
                    if (i < 4) {
                        pose.translate(1.7F, 2.5F, 0);
                    } else {
                        pose.translate(1.5F, 0, 0);
                    }
                    pose.scale(0.8F, 0.8F, 0.8F);
                    info.render(guiGraphics, 0, 0);
                    pose.popPose();
                }
            }
        }
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta) || scrollBar.adjustScroll(yDelta);
    }

    private record BlockInfo<TYPE>(BlockState state, TYPE type, RenderTarget<TYPE> renderTarget) {

        public void render(GuiGraphics guiGraphics, int x, int y) {
            renderTarget.render(guiGraphics, type, x, y);
        }
    }

    @FunctionalInterface
    private interface RenderTarget<TYPE> {

        void render(GuiGraphics guiGraphics, TYPE type, int x, int y);
    }
}