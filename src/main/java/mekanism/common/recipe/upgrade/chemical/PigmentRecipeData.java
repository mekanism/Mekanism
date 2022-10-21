package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class PigmentRecipeData extends ChemicalRecipeData<Pigment, PigmentStack, IPigmentTank, IPigmentHandler> {

    public PigmentRecipeData(ListTag tanks) {
        super(tanks);
    }

    private PigmentRecipeData(List<IPigmentTank> tanks) {
        super(tanks);
    }

    @Override
    protected PigmentRecipeData create(List<IPigmentTank> tanks) {
        return new PigmentRecipeData(tanks);
    }

    @Override
    protected SubstanceType getSubstanceType() {
        return SubstanceType.PIGMENT;
    }

    @Override
    protected ChemicalTankBuilder<Pigment, PigmentStack, IPigmentTank> getTankBuilder() {
        return ChemicalTankBuilder.PIGMENT;
    }

    @Override
    protected IPigmentHandler getOutputHandler(List<IPigmentTank> tanks) {
        return new IMekanismPigmentHandler() {
            @NotNull
            @Override
            public List<IPigmentTank> getChemicalTanks(@Nullable Direction side) {
                return tanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
    }

    @Override
    protected Capability<IPigmentHandler> getCapability() {
        return Capabilities.PIGMENT_HANDLER;
    }

    @Override
    protected Predicate<Pigment> cloneValidator(IPigmentHandler handler, int tank) {
        return type -> handler.isValid(tank, new PigmentStack(type, 1));
    }

    @Override
    protected IPigmentHandler getHandlerFromTile(TileEntityMekanism tile) {
        return tile.getPigmentManager().getInternal();
    }
}