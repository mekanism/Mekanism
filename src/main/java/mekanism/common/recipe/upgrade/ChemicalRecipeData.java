package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalRecipeData implements RecipeUpgradeData<ChemicalRecipeData> {

    protected final List<IChemicalTank> tanks;

    public ChemicalRecipeData(List<IChemicalTank> tanks) {
        this.tanks = tanks;
    }

    @Nullable
    @Override
    public ChemicalRecipeData merge(ChemicalRecipeData other) {
        List<IChemicalTank> allTanks = new ArrayList<>(tanks);
        allTanks.addAll(other.tanks);
        return new ChemicalRecipeData(allTanks);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (this.tanks.isEmpty()) {
            return true;
        }
        //TODO: Improve the logic used so that it tries to batch similar types of chemicals together first
        // and maybe make it try multiple slot combinations
        IMekanismChemicalHandler outputHandler = ContainerType.CHEMICAL.createHandler(stack);
        if (outputHandler == null) {
            //Something went wrong, fail
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            for (IChemicalTank tank : this.tanks) {
                if (!tank.isEmpty()) {
                    ChemicalResource fluidType = tank.getResource();
                    int toInsert = tank.amountAsInt();
                    //Insert into the output using manual as the automation type
                    if (outputHandler.insert(fluidType, toInsert, transaction, AutomationType.MANUAL) < toInsert) {
                        //If we have a remainder something failed so bail
                        return false;
                    }
                }
            }
            transaction.commit();
            return true;
        }
    }
}