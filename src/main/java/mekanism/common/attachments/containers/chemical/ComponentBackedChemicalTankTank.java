package mekanism.common.attachments.containers.chemical;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.world.item.ItemStack;

/**
 * Special handling for the Chemical Tank block item.
 */
@NothingNullByDefault
public class ComponentBackedChemicalTankTank extends ComponentBackedChemicalTank {

    private final boolean isCreative;

    public static ComponentBackedChemicalTankTank create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int tankIndex) {
        if (!(attachedTo.getItem() instanceof ItemBlockChemicalTank item)) {
            throw new IllegalStateException("Attached to should always be a chemical tank item");
        }
        return new ComponentBackedChemicalTankTank(attachedTo, tankIndex, item.getTier());
    }

    private ComponentBackedChemicalTankTank(ItemStack attachedTo, int tankIndex, ChemicalTankTier tier) {
        super(attachedTo, tankIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              tier::getOutput, tier::getStorage, tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null);
        isCreative = tier == ChemicalTankTier.CREATIVE;
    }

    @Override
    public ChemicalStack insert(ChemicalStack stack, Action action, AutomationType automationType) {
        return super.insert(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public ChemicalStack extract(AttachedChemicals attachedChemicals, ChemicalStack stored, long amount, Action action, AutomationType automationType) {
        return super.extract(attachedChemicals, stored, amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(AttachedChemicals, ChemicalStack, long, Action)}, as both {@link #growStack(long, Action)} and
     * {@link #shrinkStack(long, Action)} are wrapped through this method.
     */
    @Override
    public long setStackSize(AttachedChemicals attachedChemicals, ChemicalStack stored, long amount, Action action) {
        return super.setStackSize(attachedChemicals, stored, amount, action.combine(!isCreative));
    }
}