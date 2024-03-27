package mekanism.client.gui.item;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
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
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.IFluidBlock;
import org.jetbrains.annotations.NotNull;

public class GuiSeismicReader extends GuiMekanism<SeismicReaderContainer> {

    private final List<BlockInfo> blockList = new ArrayList<>();
    private final Reference2IntMap<Block> frequencies = new Reference2IntOpenHashMap<>();
    private final int minHeight;
    private MekanismButton upButton;
    private MekanismButton downButton;
    private GuiScrollBar scrollBar;

    public GuiSeismicReader(SeismicReaderContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = 147;
        imageHeight = 182;
        Player player = inv.player;
        Level level = player.level();
        this.minHeight = level.getMinBuildHeight();
        BlockPos pos = player.blockPosition();
        //Calculate all the blocks in the column
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(pos.getX(), minHeight, pos.getZ()), pos)) {
            BlockState state = level.getBlockState(p);
            //Try to get the clone item stack as maybe it has one, though it might not have a corresponding block
            ItemStack stack = state.getCloneItemStack(new BlockHitResult(p.getCenter().relative(Direction.UP, 0.5), Direction.UP, p, false), level, p, player);
            blockList.add(new BlockInfo(state, stack));
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
                return blockList.get(blockList.size() - 1 - currentLayer).block().getName();
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
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int currentLayer = blockList.size() - scrollBar.getCurrentSelection() - 1;
        //Render the layer text scaled, so that it does not start overlapping past 100
        drawTextScaledBound(guiGraphics, TextComponentUtil.build(minHeight + currentLayer), 111, 87, screenTextColor(), 13);

        //TODO - V11: Eventually instead of just rendering the item stacks, it would be nice to be able to render the actual vertical column of blocks
        //Render the item stacks or fluids
        for (int i = 0; i < 9; i++) {
            int layer = currentLayer + (i - 4);
            if (0 <= layer && layer < blockList.size()) {
                BlockInfo info = blockList.get(layer);
                RenderTarget renderTarget;
                if (info.stackRepresentation().isEmpty()) {
                    BlockState state = info.state();
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
                    renderTarget = (graphics, x, y) -> {
                        MekanismRenderer.color(guiGraphics, properties.getTintColor());
                        TextureAtlasSprite texture = MekanismRenderer.getSprite(properties.getStillTexture());
                        guiGraphics.blit(x, y, 0, 16, 16, texture);
                        MekanismRenderer.resetColor(guiGraphics);
                    };
                } else {
                    renderTarget = (graphics, x, y) -> renderItem(graphics, info.stackRepresentation(), x, y);
                }
                int renderX = 92;
                int renderY = 146 - 16 * i;
                if (i == 4) {
                    renderTarget.render(guiGraphics, renderX, renderY);
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
                    renderTarget.render(guiGraphics, 0, 0);
                    pose.popPose();
                }
            }
        }
        int frequency = 0;
        // Get the name from the stack and render it
        if (currentLayer >= 0) {
            Block block = blockList.get(currentLayer).block();
            Component displayName = block.getName();
            drawTextScaledBound(guiGraphics, displayName, 10, 16, screenTextColor(), 57);
            frequency = frequencies.computeIfAbsent(block, (Block b) -> (int) blockList.stream().filter(info -> info.state().is(b)).count());
        }
        drawTextScaledBound(guiGraphics, MekanismLang.ABUNDANCY.translate(frequency), 10, 26, screenTextColor(), 57);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta) || scrollBar.adjustScroll(yDelta);
    }

    private record BlockInfo(BlockState state, ItemStack stackRepresentation) {

        public Block block() {
            return state.getBlock();
        }
    }

    @FunctionalInterface
    private interface RenderTarget {

        void render(GuiGraphics guiGraphics, int x, int y);
    }
}