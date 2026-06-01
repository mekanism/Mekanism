package mekanism.client.model.blockstate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import mekanism.common.base.holiday.Holiday;
import mekanism.common.base.holiday.HolidayManager;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public record HolidayBasedModelSelector(Variant defaultModel, Map<Holiday, Identifier> holidayVariants) implements CustomUnbakedBlockStateModel {

    public static final MapCodec<HolidayBasedModelSelector> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
          instance.group(
                Variant.MAP_CODEC.forGetter(HolidayBasedModelSelector::defaultModel),
                Codec.unboundedMap(Holiday.CODEC, Identifier.CODEC).fieldOf("holidays").forGetter(HolidayBasedModelSelector::holidayVariants)
          ).apply(instance, HolidayBasedModelSelector::new)
    );

    @Override
    public MapCodec<HolidayBasedModelSelector> codec() {
        return MAP_CODEC;
    }

    @Override
    public BlockStateModel bake(ModelBaker modelBakery) {
        Identifier activeModel = getActiveModel();
        BlockStateModelPart modelPart = SimpleModelWrapper.bake(modelBakery, activeModel, defaultModel.modelState().asModelState());
        return new SingleVariant(modelPart);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        //other models will be deserialised by the system, but not baked
        resolver.markDependency(getActiveModel());
    }

    private Identifier getActiveModel() {
        if (!HolidayManager.areHolidaysEnabled()) {
            return defaultModel.modelLocation();
        }
        for (Map.Entry<Holiday, Identifier> entry : holidayVariants.entrySet()) {
            if (entry.getKey().isToday()) {
                return entry.getValue();
            }
        }
        return defaultModel.modelLocation();
    }
}
