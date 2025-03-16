package mekanism.common.capabilities.chemical;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.util.WorldUtils;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class StackedWasteBarrel extends VariableCapacityChemicalTank {

    @SuppressWarnings("removal")
    private static final ChemicalAttributeValidator ATTRIBUTE_VALIDATOR = new mekanism.api.chemical.attribute.ChemicalAttributeValidator.ChemicalAttributeValidatorLegacyAdapter() {
        @Override
        public boolean validate(IChemicalAttribute attr) {
            return attr instanceof ChemicalRadioactivity;
        }

        @Override
        public boolean process(Chemical chemical) {
            return chemical.isRadioactive();
        }
    };

    public static StackedWasteBarrel create(TileEntityRadioactiveWasteBarrel tile, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tile, "Radioactive Waste Barrel tile entity cannot be null");
        return new StackedWasteBarrel(tile, listener);
    }

    private final TileEntityRadioactiveWasteBarrel tile;

    protected StackedWasteBarrel(TileEntityRadioactiveWasteBarrel tile, @Nullable IContentsListener listener) {
        super(MekanismConfig.general.radioactiveWasteBarrelMaxChemical, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(),
              ConstantPredicates.alwaysTrue(), ATTRIBUTE_VALIDATOR, listener);
        this.tile = tile;
    }

    @Override
    public ChemicalStack insert(ChemicalStack stack, Action action, AutomationType automationType) {
        ChemicalStack remainder = super.insert(stack, action, automationType);
        //Ensure we have the same type of gas stored as we failed to insert, in which case we want to try to insert to the one above
        if (!remainder.isEmpty() && ChemicalStack.isSameChemical(stored, remainder)) {
            //If we have any leftover check if we can send it to the tank that is above
            TileEntityRadioactiveWasteBarrel tileAbove = WorldUtils.getTileEntity(TileEntityRadioactiveWasteBarrel.class, tile.getLevel(), tile.getBlockPos().above());
            if (tileAbove != null) {
                //Note: We do external so that it is not limited by the internal rate limits
                remainder = tileAbove.getChemicalTank().insert(remainder, action, AutomationType.EXTERNAL);
            }
        }
        return remainder;
    }

    @Override
    public long growStack(long amount, Action action) {
        long grownAmount = super.growStack(amount, action);
        if (amount > 0 && grownAmount < amount) {
            //If we grew our stack less than we tried to, and we were actually growing and not shrinking it
            // try inserting into above tiles
            if (!tile.getActive()) {
                TileEntityRadioactiveWasteBarrel tileAbove = WorldUtils.getTileEntity(TileEntityRadioactiveWasteBarrel.class, tile.getLevel(), tile.getBlockPos().above());
                if (tileAbove != null) {
                    long leftOverToInsert = amount - grownAmount;
                    //Note: We do external so that it is not limited by the internal rate limits
                    ChemicalStack remainder = tileAbove.getChemicalTank().insert(stored.copyWithAmount(leftOverToInsert), action, AutomationType.EXTERNAL);
                    grownAmount += leftOverToInsert - remainder.getAmount();
                }
            }
        }
        return grownAmount;
    }
}