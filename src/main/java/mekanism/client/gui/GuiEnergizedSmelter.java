package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.electric.EnergizedSmelterContainer;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiEnergizedSmelter extends GuiElectricMachine<SmeltingRecipe, TileEntityEnergizedSmelter, EnergizedSmelterContainer> {

    public GuiEnergizedSmelter(EnergizedSmelterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.GREEN;
    }
}