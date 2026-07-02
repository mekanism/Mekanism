package mekanism.common.integration.framedblocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.xfacthd.framedblocks.api.camo.TriggerRegistrar;
import io.github.xfacthd.framedblocks.api.camo.resource.ResourceCamoContainerFactory;
import io.github.xfacthd.framedblocks.api.util.CamoMessageVerbosity;
import io.github.xfacthd.framedblocks.api.util.Utils;
import java.util.function.Predicate;
import mekanism.api.MekanismAPITags;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalAttributeValidator;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

final class ChemicalCamoContainerFactory extends ResourceCamoContainerFactory<ChemicalResource, ChemicalCamoContent, ChemicalCamoContainer> {

    private static final TagKey<Item> CRAFTING_BLOCKED_CONTAINERS = Utils.itemTag("crafting_blocked_chemical_containers");
    private static final MapCodec<ChemicalCamoContainer> MAP_CODEC = ChemicalResource.OPTIONAL_CODEC.xmap(
          ChemicalCamoContainer::new,
          ChemicalCamoContainer::getChemicalType
    ).fieldOf(SerializationConstants.CHEMICAL);
    private static final Codec<ChemicalCamoContainer> CODEC = MAP_CODEC.codec();
    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalCamoContainer> STREAM_CODEC = ChemicalResource.STREAM_CODEC.map(
          ChemicalCamoContainer::new,
          ChemicalCamoContainer::getChemicalType
    );
    private static final Component MSG_HAS_SPECIAL_HANDLING = MekanismLang.FRAMEDBLOCKS_CAMO_HAS_SPECIAL_HANDLING.translate();

    ChemicalCamoContainerFactory() {
        super(Capabilities.CHEMICAL.item(), FluidType.BUCKET_VOLUME, CRAFTING_BLOCKED_CONTAINERS);
    }

    @Override
    protected void writeToNetwork(ValueOutput output, ChemicalCamoContainer camo) {
        output.store(SerializationConstants.CHEMICAL, CODEC, camo);
    }

    @Override
    protected ChemicalCamoContainer readFromNetwork(ValueInput input) {
        return input.read(SerializationConstants.CHEMICAL, CODEC).orElseGet(() -> new ChemicalCamoContainer(ChemicalResource.EMPTY));
    }

    @Override
    protected ChemicalCamoContainer createContainer(ChemicalResource resource) {
        return new ChemicalCamoContainer(resource);
    }

    @Override
    protected boolean isValidResource(ChemicalResource resource, @Nullable Player player) {
        if (resource.isEmpty()) {
            return false;
        } else if (ChemicalAttributeValidator.DEFAULT.process(resource)) {
            if (resource.is(MekanismAPITags.Chemicals.FRAMEDBLOCKS_BLACKLISTED)) {
                displayValidationMessage(player, MSG_BLACKLISTED, CamoMessageVerbosity.DEFAULT);
                return false;
            }
            return true;
        }
        displayValidationMessage(player, MSG_HAS_SPECIAL_HANDLING, CamoMessageVerbosity.DEFAULT);
        return false;
    }

    @Override
    public MapCodec<ChemicalCamoContainer> codec() {
        return MAP_CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ChemicalCamoContainer> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void registerTriggerItems(TriggerRegistrar registrar) {
        Predicate<ItemStack> predicate = stack -> Capabilities.CHEMICAL.getCapability(ItemAccessUtils.sideEffectFreeAccess(stack)) != null;
        registrar.registerApplicationPredicate(predicate);
        registrar.registerRemovalPredicate(predicate);
    }
}
