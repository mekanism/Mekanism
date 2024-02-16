package mekanism.client.model.baked;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.common.base.holiday.ClientHolidayInfo;
import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DigitalMinerBakedModel extends ExtensionBakedModel<Void> {

    public DigitalMinerBakedModel(BakedModel original) {
        super(original);
    }

    @Nullable
    @Override
    protected QuadsKey<Void> createKey(QuadsKey<Void> key, ModelData data) {
        QuadTransformation holidayTransform = ClientHolidayInfo.getMinerTransform();
        if (holidayTransform != null) {
            return key.transform(holidayTransform);
        }
        return null;
    }

    @Override
    protected DigitalMinerBakedModel wrapModel(BakedModel model) {
        return new DigitalMinerBakedModel(model);
    }
}
