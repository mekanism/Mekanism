package mekanism.common.attachments.containers.chemical.infuse;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedChemicalTankInfusionTank extends ComponentBackedInfusionTank {

    private final boolean isCreative;

    public static ComponentBackedChemicalTankInfusionTank create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int tankIndex) {
        if (!(attachedTo.getItem() instanceof ItemBlockChemicalTank item)) {
            throw new IllegalStateException("Attached to should always be a chemical tank item");
        }
        return new ComponentBackedChemicalTankInfusionTank(attachedTo, tankIndex, item.getTier());
    }

    private ComponentBackedChemicalTankInfusionTank(ItemStack attachedTo, int tankIndex, ChemicalTankTier tier) {
        super(attachedTo, tankIndex, ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrue,
              tier::getOutput, tier::getStorage, tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null);
        isCreative = tier == ChemicalTankTier.CREATIVE;
    }

    @Override
    public InfusionStack insert(InfusionStack stack, Action action, AutomationType automationType) {
        return super.insert(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public InfusionStack extract(AttachedInfuseTypes attachedInfuseTypes, InfusionStack stored, long amount, Action action, AutomationType automationType) {
        return super.extract(attachedInfuseTypes, stored, amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(AttachedInfuseTypes, InfusionStack, long, Action)}, as both {@link #growStack(long, Action)} and
     * {@link #shrinkStack(long, Action)} are wrapped through this method.
     */
    @Override
    public long setStackSize(AttachedInfuseTypes attachedInfuseTypes, InfusionStack stored, long amount, Action action) {
        return super.setStackSize(attachedInfuseTypes, stored, amount, action.combine(!isCreative));
    }
}