package mekanism.common.capabilities.chemical;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.util.WorldUtils;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class StackedWasteBarrel extends VariableCapacityChemicalTank {

    private static final ChemicalAttributeValidator ATTRIBUTE_VALIDATOR = new ChemicalAttributeValidator() {
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
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int inserted = super.insert(resource, amount, transaction, automationType);
        //Ensure we have the same type of gas stored as we failed to insert, in which case we want to try to insert to the one above
        if (inserted < amount && getResource().equals(resource)) {
            //If we have any leftover check if we can send it to the tank that is above
            TileEntityRadioactiveWasteBarrel tileAbove = WorldUtils.getTileEntity(TileEntityRadioactiveWasteBarrel.class, this.tile.getLevel(), this.tile.getBlockPos().above());
            if (tileAbove != null) {
                //Note: We do external so that it is not limited by the internal rate limits
                inserted += tileAbove.getChemicalTank().insert(resource, amount - inserted, transaction, AutomationType.EXTERNAL);
            }
        }
        return inserted;
    }
}