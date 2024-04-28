package mekanism.common.attachments.containers.chemical.pigment;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedChemicalTankPigmentTank extends ComponentBackedPigmentTank {

    private final boolean isCreative;

    public static ComponentBackedChemicalTankPigmentTank create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int tankIndex) {
        if (!(attachedTo.getItem() instanceof ItemBlockChemicalTank item)) {
            throw new IllegalStateException("Attached to should always be a chemical tank item");
        }
        return new ComponentBackedChemicalTankPigmentTank(attachedTo, tankIndex, item.getTier());
    }

    private ComponentBackedChemicalTankPigmentTank(ItemStack attachedTo, int tankIndex, ChemicalTankTier tier) {
        super(attachedTo, tankIndex, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue,
              tier::getOutput, tier::getStorage, tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null);
        isCreative = tier == ChemicalTankTier.CREATIVE;
    }

    @Override
    public PigmentStack insert(PigmentStack stack, Action action, AutomationType automationType) {
        return super.insert(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public PigmentStack extract(AttachedPigments attachedPigments, PigmentStack stored, long amount, Action action, AutomationType automationType) {
        return super.extract(attachedPigments, stored, amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(AttachedPigments, PigmentStack, long, Action)}, as both {@link #growStack(long, Action)} and
     * {@link #shrinkStack(long, Action)} are wrapped through this method.
     */
    @Override
    public long setStackSize(AttachedPigments attachedPigments, PigmentStack stored, long amount, Action action) {
        return super.setStackSize(attachedPigments, stored, amount, action.combine(!isCreative));
    }
}