package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.client.gui.button.MekanismButton;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

//TODO: Test to see if this properly was converted to GuiMekanism
public class GuiSeismicReader extends GuiMekanism<SeismicReaderContainer> {

    private Coord4D pos;
    private World worldObj;
    private List<BlockState> blockList = new ArrayList<>();
    private Rectangle tooltip;
    private MekanismButton upButton;
    private MekanismButton downButton;

    private int currentLayer;

    public GuiSeismicReader(SeismicReaderContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize = 137;
        ySize = 182;
        PlayerEntity player = inv.player;
        BlockPos position = player.getPosition();
        worldObj = player.world;
        //TODO: Does this Math.min make it so that things like CubicChunks don't work
        pos = new Coord4D(position.getX(), Math.min(255, position.getY()), position.getZ(), worldObj.getDimension().getType());
        calculate();
        currentLayer = Math.max(0, blockList.size() - 1);
        //TODO: Add scroll bar element
    }

    @Override
    public void init() {
        super.init();
        addButton(upButton = new MekanismImageButton(this, getGuiLeft() + 70, getGuiTop() + 75, 13,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "up.png"), () -> currentLayer++));
        addButton(downButton = new MekanismImageButton(this, getGuiLeft() + 70, getGuiTop() + 92, 13,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "down.png"), () -> currentLayer--));
        tooltip = new Rectangle(getGuiLeft() + 30, getGuiTop() + 82, 16, 16);
        updateEnabledButtons();
    }

    @Override
    public void tick() {
        super.tick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        upButton.active = currentLayer + 1 <= blockList.size();
        downButton.active = currentLayer - 1 >= 1;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        super.render(mouseX, mouseY, partialTick);
        int guiLeft = (width - getXSize()) / 2;
        int guiTop = (height - getYSize()) / 2;

        // Fix the overlapping if > 100
        RenderSystem.pushMatrix();
        RenderSystem.translatef(guiLeft + 48, guiTop + 87, 0);

        if (currentLayer >= 100) {
            RenderSystem.translatef(0, 1, 0);
            RenderSystem.scalef(0.7F, 0.7F, 0.7F);
        }

        drawString(MekanismLang.GENERIC.translate(currentLayer), 0, 0, 0xAFAFAF);
        RenderSystem.popMatrix();

        // Render the item stacks
        for (int i = 0; i < 9; i++) {
            int centralX = guiLeft + 32, centralY = guiTop + 103;
            int layer = currentLayer + (i - 5);
            if (0 <= layer && layer < blockList.size()) {
                BlockState state = blockList.get(layer);
                ItemStack stack = new ItemStack(state.getBlock());
                RenderSystem.pushMatrix();
                RenderSystem.translatef(centralX - 2, centralY - i * 16 + (22 * 2), 0);
                if (i < 4) {
                    RenderSystem.translatef(0.2F, 2.5F, 0);
                }
                if (i != 4) {
                    RenderSystem.translatef(1.5F, 0, 0);
                    RenderSystem.scalef(0.8F, 0.8F, 0.8F);
                }
                renderItem(stack, 0, 0);
                RenderSystem.popMatrix();
            }
        }

        int frequency = 0;
        // Get the name from the stack and render it
        if (currentLayer - 1 >= 0) {
            BlockState state = blockList.get(currentLayer - 1);
            ItemStack nameStack = new ItemStack(state.getBlock());
            ITextComponent displayName = nameStack.getDisplayName();
            int lengthX = getStringWidth(displayName);
            float renderScale = lengthX > 53 ? 53f / lengthX : 1.0f;

            RenderSystem.pushMatrix();
            RenderSystem.translatef(guiLeft + 72, guiTop + 16, 0);
            RenderSystem.scalef(renderScale, renderScale, renderScale);
            drawString(displayName, 0, 0, 0x919191);
            RenderSystem.popMatrix();

            if (tooltip.intersects(new Rectangle(mouseX, mouseY, 1, 1))) {
                minecraft.textureManager.bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "guitooltips.png"));
                int fontLengthX = lengthX + 5;
                int renderX = mouseX + 10, renderY = mouseY - 5;
                RenderSystem.pushMatrix();
                blit(renderX, renderY, 0, 0, fontLengthX, 16);
                blit(renderX + fontLengthX, renderY, 0, 16, 2, 16);
                drawString(displayName, renderX + 4, renderY + 4, 0x919191);
                RenderSystem.popMatrix();
            }

            frequency = (int) blockList.stream().filter(blockState -> state.getBlock() == blockState.getBlock()).count();
        }

        RenderSystem.pushMatrix();
        RenderSystem.translatef(guiLeft + 72, guiTop + 26, 0);
        RenderSystem.scalef(0.7F, 0.7F, 0.7F);
        drawString(MekanismLang.ABUNDANCY.translate(frequency), 0, 0, 0x919191);
        RenderSystem.popMatrix();
        MekanismRenderer.resetColor();
    }

    @Override
    public void onClose() {
        super.onClose();
        blockList.clear();
    }

    public void calculate() {
        for (BlockPos p = new BlockPos(pos.x, 0, pos.z); p.getY() < pos.y; p = p.up()) {
            blockList.add(worldObj.getBlockState(p));
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "seismic_reader.png");
    }
}