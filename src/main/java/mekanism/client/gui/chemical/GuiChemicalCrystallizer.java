package mekanism.client.gui.chemical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.OreGas;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.ContainerChemicalCrystallizer;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.oredict.OreDictionary;

@OnlyIn(Dist.CLIENT)
public class GuiChemicalCrystallizer extends GuiMekanismTile<TileEntityChemicalCrystallizer> {

    private List<ItemStack> iterStacks = new ArrayList<>();
    private ItemStack renderStack = ItemStack.EMPTY;
    private int stackSwitch = 0;
    private int stackIndex = 0;
    private Gas prevGas;

    public GuiChemicalCrystallizer(PlayerInventory inventory, TileEntityChemicalCrystallizer tile) {
        super(tile, new ContainerChemicalCrystallizer(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 160, 23));
        addGuiElement(new GuiSideConfigurationTab(this, tileEntity, resource));
        addGuiElement(new GuiTransporterConfigTab(this, 34, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.using"), ": ", EnergyDisplay.of(tileEntity.getEnergyPerTick()), "/t"),
              TextComponentUtil.build(Translation.of("mekanism.gui.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));
        addGuiElement(new GuiGasGauge(() -> tileEntity.inputTank, GuiGauge.Type.STANDARD, this, resource, 5, 4));
        addGuiElement(new GuiSlot(SlotType.EXTRA, this, resource, 5, 64).with(SlotOverlay.PLUS));
        addGuiElement(new GuiSlot(SlotType.POWER, this, resource, 154, 4).with(SlotOverlay.POWER));
        addGuiElement(new GuiSlot(SlotType.OUTPUT, this, resource, 130, 56));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getScaledProgress();
            }
        }, ProgressBar.LARGE_RIGHT, this, resource, 51, 60));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 37, 4, 0x404040);
        GasStack gasStack = tileEntity.inputTank.getGas();
        if (gasStack != null) {
            drawString(TextComponentUtil.build(gasStack), 29, 15, 0x00CD00);
            if (gasStack.getGas() instanceof OreGas) {
                drawString(TextComponentUtil.build("(", Translation.of(((OreGas) gasStack.getGas()).getOreTranslationKey()), ")"), 29, 24, 0x00CD00);
            } else {
                CrystallizerRecipe recipe = tileEntity.getRecipe();
                if (recipe == null) {
                    drawString(TextComponentUtil.build("(", Translation.of("mekanism.gui.noRecipe"), ")"), 29, 24, 0x00CD00);
                } else {
                    drawString(TextComponentUtil.build("(", recipe.recipeOutput.output.getDisplayName(), ")"), 29, 24, 0x00CD00);
                }
            }
        }
        renderItem(renderStack, 131, 14);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalCrystallizer.png");
    }

    private Gas getInputGas() {
        return tileEntity.inputTank.getGas() != null ? tileEntity.inputTank.getGas().getGas() : null;
    }

    private void resetStacks() {
        iterStacks.clear();
        renderStack = ItemStack.EMPTY;
        stackSwitch = 0;
        stackIndex = -1;
    }

    @Override
    public void tick() {
        super.tick();

        if (prevGas != getInputGas()) {
            prevGas = getInputGas();
            boolean reset = false;
            if (prevGas == null || !(prevGas instanceof OreGas) || !((OreGas) prevGas).isClean()) {
                reset = true;
                resetStacks();
            }
            if (!reset) {
                OreGas gas = (OreGas) prevGas;
                String oreDictName = "ore" + gas.getName().substring(5);
                updateStackList(oreDictName);
            }
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

    private void updateStackList(String oreName) {
        if (iterStacks == null) {
            iterStacks = new ArrayList<>();
        } else {
            iterStacks.clear();
        }

        List<String> keys = new ArrayList<>();
        for (String s : OreDictionary.getOreNames()) {
            if (oreName.equals(s) || oreName.equals("*")) {
                keys.add(s);
            } else {
                boolean endsWith = oreName.endsWith("*");
                boolean startsWith = oreName.startsWith("*");
                if (endsWith && !startsWith) {
                    if (s.startsWith(oreName.substring(0, oreName.length() - 1))) {
                        keys.add(s);
                    }
                } else if (startsWith && !endsWith) {
                    if (s.endsWith(oreName.substring(1))) {
                        keys.add(s);
                    }
                } else if (startsWith) {
                    if (s.contains(oreName.substring(1, oreName.length() - 1))) {
                        keys.add(s);
                    }
                }
            }
        }

        for (String key : keys) {
            for (ItemStack stack : OreDictionary.getOres(key, false)) {
                ItemStack toAdd = stack.copy();
                if (!iterStacks.contains(stack) && toAdd.getItem() instanceof BlockItem) {
                    iterStacks.add(stack.copy());
                }
            }
        }
        stackSwitch = 0;
        stackIndex = -1;
    }
}