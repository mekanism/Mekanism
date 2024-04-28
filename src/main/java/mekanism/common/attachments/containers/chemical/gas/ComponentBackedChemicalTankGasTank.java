package mekanism.common.attachments.containers.chemical.gas;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedChemicalTankGasTank extends ComponentBackedGasTank {

    private final boolean isCreative;

    public static ComponentBackedChemicalTankGasTank create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int tankIndex) {
        if (!(attachedTo.getItem() instanceof ItemBlockChemicalTank item)) {
            throw new IllegalStateException("Attached to should always be a chemical tank item");
        }
        return new ComponentBackedChemicalTankGasTank(attachedTo, tankIndex, item.getTier());
    }

    private ComponentBackedChemicalTankGasTank(ItemStack attachedTo, int tankIndex, ChemicalTankTier tier) {
        super(attachedTo, tankIndex, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue,
              tier::getOutput, tier::getStorage, tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null);
        isCreative = tier == ChemicalTankTier.CREATIVE;
    }

    @Override
    public GasStack insert(GasStack stack, Action action, AutomationType automationType) {
        return super.insert(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public GasStack extract(AttachedGases attachedGases, GasStack stored, long amount, Action action, AutomationType automationType) {
        return super.extract(attachedGases, stored, amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(AttachedGases, GasStack, long, Action)}, as both {@link #growStack(long, Action)} and
     * {@link #shrinkStack(long, Action)} are wrapped through this method.
     */
    @Override
    public long setStackSize(AttachedGases attachedGases, GasStack stored, long amount, Action action) {
        return super.setStackSize(attachedGases, stored, amount, action.combine(!isCreative));
    }
}