package mekanism.common.integration.framedblocks;

import com.mojang.serialization.MapCodec;
import io.github.xfacthd.framedblocks.api.camo.CamoContainerFactory;
import io.github.xfacthd.framedblocks.api.camo.TriggerRegistrar;
import io.github.xfacthd.framedblocks.api.util.CamoMessageVerbosity;
import io.github.xfacthd.framedblocks.api.util.ConfigView;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPITags;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: Make use of https://github.com/XFactHD/FramedBlocks/commit/98a92c86d28c7f3c6011ce7444c07d54cd0e2115 once there is a released version that contains that
final class ChemicalCamoContainerFactory extends CamoContainerFactory<ChemicalCamoContainer> {

    private static final MapCodec<ChemicalCamoContainer> CODEC = ChemicalResource.CODEC.xmap(
          ChemicalCamoContainer::new,
          ChemicalCamoContainer::getChemicalType
    ).fieldOf(SerializationConstants.CHEMICAL);
    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalCamoContainer> STREAM_CODEC = ChemicalResource.STREAM_CODEC.map(
          ChemicalCamoContainer::new,
          ChemicalCamoContainer::getChemicalType
    );
    private static final Component MSG_HAS_SPECIAL_HANDLING = MekanismLang.FRAMEDBLOCKS_CAMO_HAS_SPECIAL_HANDLING.translate();

    @Override
    protected void writeToNetwork(ValueOutput output, ChemicalCamoContainer camo) {
        output.putInt(SerializationConstants.CHEMICAL, MekanismAPI.CHEMICAL_REGISTRY.getId(camo.getChemicalType().value()));
        //TODO - 26.1: Do we want to be adding an equivalent of flow_dir?
    }

    @Override
    protected ChemicalCamoContainer readFromNetwork(ValueInput input) {
        Chemical chemical = MekanismAPI.CHEMICAL_REGISTRY.byId(input.getIntOr(SerializationConstants.CHEMICAL, -1));
        return new ChemicalCamoContainer(ChemicalResource.of(chemical));
    }

    @Override
    @Nullable
    public ChemicalCamoContainer applyCamo(Level level, BlockPos pos, Player player, ItemAccess itemAccess) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        if (handler == null || handler.size() <= 0) {
            return null;
        }

        int size = handler.size();
        Set<ChemicalResource> seenTypes = new HashSet<>(size);
        for (int tank = 0; tank < size; tank++) {
            ChemicalResource chemicalType = handler.getResource(tank);
            if (!seenTypes.add(chemicalType) || !isValidChemical(chemicalType, player)) {
                //If we already tried this type, or if it is not a valid chemical, skip
                continue;
            }

            if (!player.isCreative() && ConfigView.Server.INSTANCE.shouldConsumeCamoItem()) {
                try (Transaction transaction = Transaction.openRoot()) {
                    if (handler.extract(chemicalType, FramedBlocksIntegration.Constants.CHEMICAL_AMOUNT, transaction) != FramedBlocksIntegration.Constants.CHEMICAL_AMOUNT) {
                        continue;
                    }
                    if (!level.isClientSide()) {
                        transaction.commit();
                    }
                }
            }
            return new ChemicalCamoContainer(chemicalType);
        }
        return null;
    }

    @Override
    public boolean removeCamo(Level level, BlockPos pos, Player player, ItemAccess itemAccess, ChemicalCamoContainer camo) {
        if (itemAccess.getResource().isEmpty()) {
            return false;
        }
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        if (handler == null || handler.size() <= 0) {
            return false;
        }
        ChemicalResource chemicalType = camo.getChemicalType();
        if (!isValidForHandler(handler, chemicalType)) {
            return false;
        }
        if (!player.isCreative() && ConfigView.Server.INSTANCE.shouldConsumeCamoItem()) {
            try (Transaction transaction = Transaction.openRoot()) {
                if (handler.insert(chemicalType, FramedBlocksIntegration.Constants.CHEMICAL_AMOUNT, transaction) != FramedBlocksIntegration.Constants.CHEMICAL_AMOUNT) {
                    return false;
                }
                if (!level.isClientSide()) {
                    transaction.commit();
                }
            }
        }
        return true;
    }

    private static boolean isValidForHandler(ResourceHandler<ChemicalResource> handler, ChemicalResource chemicalType) {
        if (chemicalType.isEmpty()) {
            return false;
        }
        for (int tank = 0, size = handler.size(); tank < size; tank++) {
            if (!handler.isValid(tank, chemicalType)) {
                continue;
            }
            ChemicalResource inTank = handler.getResource(tank);
            if (inTank.isEmpty() || inTank.equals(chemicalType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canTriviallyConvertToItemStack() {
        return false;
    }

    @Override
    public ItemStack dropCamo(ChemicalCamoContainer camo) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean validateCamo(ChemicalCamoContainer camo) {
        return isValidChemical(camo.getChemicalType(), null);
    }

    private static boolean isValidChemical(ChemicalResource chemical, @Nullable Player player) {
        if (chemical.isEmpty()) {
            return false;
        } else if (chemical.value().hasAttributesWithValidation()) {
            displayValidationMessage(player, MSG_HAS_SPECIAL_HANDLING, CamoMessageVerbosity.DEFAULT);
            return false;
        } else if (chemical.is(MekanismAPITags.Chemicals.FRAMEDBLOCKS_BLACKLISTED)) {
            displayValidationMessage(player, MSG_BLACKLISTED, CamoMessageVerbosity.DEFAULT);
            return false;
        }
        return true;
    }

    @Override
    public MapCodec<ChemicalCamoContainer> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ChemicalCamoContainer> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void registerTriggerItems(TriggerRegistrar registrar) {
        Predicate<ItemStack> predicate = stack -> Capabilities.CHEMICAL.getCapability(ItemAccess.forStack(stack)) != null;
        registrar.registerApplicationPredicate(predicate);
        registrar.registerRemovalPredicate(predicate);
    }
}
