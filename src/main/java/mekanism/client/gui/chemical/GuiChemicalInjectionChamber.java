package mekanism.client.gui.chemical;

import mekanism.client.gui.GuiAdvancedElectricMachine;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.recipe.machines.InjectionRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiChemicalInjectionChamber extends GuiAdvancedElectricMachine<InjectionRecipe> {

    public GuiChemicalInjectionChamber(PlayerInventory inventory, TileEntityAdvancedElectricMachine<InjectionRecipe> tile) {
        super(inventory, tile);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.YELLOW;
    }
}