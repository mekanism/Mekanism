package mekanism.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.client.gui.button.GuiButtonSeismicReader;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.Rectangle;

//TODO: Switch Screen to GuiMekanism
@OnlyIn(Dist.CLIENT)
public class GuiSeismicReader extends Screen {

    private final ItemStack itemStack;
    private Coord4D pos;
    private int xSize = 137;
    private int ySize = 182;
    private World worldObj;
    private List<Pair<Integer, Block>> blockList = new ArrayList<>();
    private Rectangle tooltip;
    private Button upButton;
    private Button downButton;

    private int currentLayer;

    /*public GuiSeismicReader(SeismicReaderContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }*/

    public GuiSeismicReader(World world, Coord4D coord, ItemStack stack) {
        super(TextComponentUtil.translate("mekanism.container.seismic_reader"));
        pos = new Coord4D(coord.x, Math.min(255, coord.y), coord.z, world.getDimension().getType());
        worldObj = world;
        itemStack = stack;
        calculate();
        currentLayer = Math.max(0, blockList.size() - 1);
    }

    @Override
    public void init() {
        super.init();
        int guiLeft = (width - xSize) / 2;
        int guiTop = (height - ySize) / 2;
        buttons.add(upButton = new GuiButtonSeismicReader(guiLeft + 70, guiTop + 75, 13, 13, 137, 0, getGuiLocation(),
              onPress -> currentLayer++));
        buttons.add(downButton = new GuiButtonSeismicReader(guiLeft + 70, guiTop + 92, 13, 13, 150, 0, getGuiLocation(),
              onPress -> currentLayer--));
        tooltip = new Rectangle(guiLeft + 30, guiTop + 82, 16, 16);
        updateEnabledButtons();
    }

    @Override
    public void tick() {
        super.tick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        upButton.active = currentLayer + 1 <= blockList.size() - 1;
        downButton.active = currentLayer - 1 >= 1;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        int guiLeft = (width - xSize) / 2;
        int guiTop = (height - ySize) / 2;
        minecraft.textureManager.bindTexture(getGuiLocation());
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Fix the overlapping if > 100
        GlStateManager.pushMatrix();
        GlStateManager.translatef(guiLeft + 48, guiTop + 87, 0);

        if (currentLayer >= 100) {
            GlStateManager.translatef(0, 1, 0);
            GlStateManager.translatef(0.7F, 0.7F, 0.7F);
        }

        font.drawString(String.format("%s", currentLayer), 0, 0, 0xAFAFAF);
        GlStateManager.popMatrix();

        // Render the item stacks
        for (int i = 0; i < 9; i++) {
            int centralX = guiLeft + 32, centralY = guiTop + 103;
            int layer = currentLayer + (i - 5);
            if (0 <= layer && layer < blockList.size()) {
                Pair<Integer, Block> integerBlockPair = blockList.get(layer);
                ItemStack stack = new ItemStack(integerBlockPair.getRight(), 1, integerBlockPair.getLeft());
                GlStateManager.pushMatrix();
                GlStateManager.translatef(centralX - 2, centralY - i * 16 + (22 * 2), 0);
                if (i < 4) {
                    GlStateManager.translatef(0.2F, 2.5F, 0);
                }
                if (i != 4) {
                    GlStateManager.translatef(1.5F, 0, 0);
                    GlStateManager.translatef(0.8F, 0.8F, 0.8F);
                }
                renderItem(stack, 0, 0);
                GlStateManager.popMatrix();
            }
        }

        int frequency = 0;
        // Get the name from the stack and render it
        if (currentLayer - 1 >= 0) {
            Pair<Integer, Block> integerBlockPair = blockList.get(currentLayer - 1);
            ItemStack nameStack = new ItemStack(integerBlockPair.getRight(), 1, integerBlockPair.getLeft());
            String renderString = nameStack.getDisplayName();

            String capitalised = renderString.substring(0, 1).toUpperCase() + renderString.substring(1);
            int lengthX = font.getStringWidth(capitalised);
            float renderScale = lengthX > 53 ? 53f / lengthX : 1.0f;

            GlStateManager.pushMatrix();
            GlStateManager.translatef(guiLeft + 72, guiTop + 16, 0);
            GlStateManager.translatef(renderScale, renderScale, renderScale);
            font.drawString(capitalised, 0, 0, 0x919191);
            GlStateManager.popMatrix();

            if (tooltip.intersects(new Rectangle(mouseX, mouseY, 1, 1))) {
                minecraft.textureManager.bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiTooltips.png"));
                int fontLengthX = font.getStringWidth(capitalised) + 5;
                int renderX = mouseX + 10, renderY = mouseY - 5;
                GlStateManager.pushMatrix();
                blit(renderX, renderY, 0, 0, fontLengthX, 16);
                blit(renderX + fontLengthX, renderY, 0, 16, 2, 16);
                font.drawString(capitalised, renderX + 4, renderY + 4, 0x919191);
                GlStateManager.popMatrix();
            }

            for (Pair<Integer, Block> pair : blockList) {
                Block block = integerBlockPair.getRight();
                if (pair.getRight() == block && Objects.equals(pair.getLeft(), integerBlockPair.getLeft())) {
                    frequency++;
                }
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.translatef(guiLeft + 72, guiTop + 26, 0);
        GlStateManager.translatef(0.7F, 0.7F, 0.7F);
        //TODO:
        ITextComponent component = TextComponentUtil.build(Translation.of("gui.abundancy"), ": " + frequency);
        font.drawString(component.getFormattedText(), 0, 0, 0x919191);
        GlStateManager.popMatrix();
        MekanismRenderer.resetColor();
        super.render(mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        super.onClose();
        blockList.clear();
    }

    public void calculate() {
        for (BlockPos p = new BlockPos(pos.x, 0, pos.z); p.getY() < pos.y; p = p.up()) {
            BlockState state = worldObj.getBlockState(p);
            Block block = state.getBlock();
            int metadata = block.getMetaFromState(state);
            blockList.add(Pair.of(metadata, block));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis) {
        if (!stack.isEmpty()) {
            try {
                GlStateManager.pushMatrix();
                GlStateManager.enableDepthTest();
                RenderHelper.enableGUIStandardItemLighting();
                itemRenderer.renderItemAndEffectIntoGUI(stack, xAxis, yAxis);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableDepthTest();
                GlStateManager.popMatrix();
            } catch (Exception e) {
                Mekanism.logger.error("Failed to render stack into gui: " + stack, e);
            }
        }
    }

    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicReader.png");
    }
}