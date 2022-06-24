package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionHandler.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class InfusionRecipeData extends ChemicalRecipeData<InfuseType, InfusionStack, IInfusionTank, IInfusionHandler> {

    public InfusionRecipeData(ListTag tanks) {
        super(tanks);
    }

    private InfusionRecipeData(List<IInfusionTank> tanks) {
        super(tanks);
    }

    @Override
    protected InfusionRecipeData create(List<IInfusionTank> tanks) {
        return new InfusionRecipeData(tanks);
    }

    @Override
    protected SubstanceType getSubstanceType() {
        return SubstanceType.INFUSION;
    }

    @Override
    protected ChemicalTankBuilder<InfuseType, InfusionStack, IInfusionTank> getTankBuilder() {
        return ChemicalTankBuilder.INFUSION;
    }

    @Override
    protected IInfusionHandler getOutputHandler(List<IInfusionTank> tanks) {
        return new IMekanismInfusionHandler() {
            @NotNull
            @Override
            public List<IInfusionTank> getChemicalTanks(@Nullable Direction side) {
                return tanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
    }

    @Override
    protected Capability<IInfusionHandler> getCapability() {
        return Capabilities.INFUSION_HANDLER;
    }

    @Override
    protected Predicate<InfuseType> cloneValidator(IInfusionHandler handler, int tank) {
        return type -> handler.isValid(tank, new InfusionStack(type, 1));
    }

    @Override
    protected IInfusionHandler getHandlerFromTile(TileEntityMekanism tile) {
        return tile.getInfusionManager().getInternal();
    }
}