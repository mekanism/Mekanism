package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PigmentRecipeData extends ChemicalRecipeData<Pigment, PigmentStack, IPigmentTank, IPigmentHandler> {

    public PigmentRecipeData(ListNBT tanks) {
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
            @Nonnull
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
        return Capabilities.PIGMENT_HANDLER_CAPABILITY;
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