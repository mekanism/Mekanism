package mekanism.client.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.armor.MekaSuitArmor.ModuleOBJModelData;
import mekanism.common.Mekanism;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.client.model.obj.ObjGeometry;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

public class MekanismModelCache extends BaseModelCache {

    public static final MekanismModelCache INSTANCE = new MekanismModelCache();
    private final Set<Runnable> callbacks = new HashSet<>();

    public final Identifier MEKATOOL_LEFT_ID = Mekanism.rl("item/meka_tool_left");
    public final Identifier MEKATOOL_RIGHT_ID = Mekanism.rl("item/meka_tool_default");
    private Set<String> overrideLeftMekaToolParts = Collections.emptySet();
    private Set<String> overrideRightMekaToolParts = Collections.emptySet();

    public final OBJModelData MEKASUIT = registerOBJ("entity/mekasuit");
    private final Set<ModuleOBJModelData> mekaSuitModules = new HashSet<>();
    @UnmodifiableView
    public final Set<ModuleOBJModelData> MEKASUIT_MODULES = Collections.unmodifiableSet(mekaSuitModules);

    public final BlockStateModelPartHelper LIQUIFIER_BLADE = registerJSON("block/liquifier_blade");
    public final BlockStateModelPartHelper VIBRATOR_SHAFT = registerJSON("block/vibrator_shaft");
    public final BlockStateModelPartHelper PIGMENT_MIXER_SHAFT = registerJSON("block/pigment_mixer_shaft");
    public final BlockStateModelPartHelper TRANSPORTER_BOX = registerJSON("block/transporter_box");
    public final BlockStateModelPartHelper[] QIO_DRIVES = new BlockStateModelPartHelper[EnumUtils.DRIVE_STATUSES.length];

    private MekanismModelCache() {
        super(Mekanism.MODID);
        for (DriveStatus status : EnumUtils.DRIVE_STATUSES) {
            Identifier model = status.getModel();
            if (model != null) {
                QIO_DRIVES[status.ordinal()] = registerJSON(model);
            }
        }
    }

    @Override
    public void onBake(BakingCompleted evt) {
        super.onBake(evt);
        callbacks.forEach(Runnable::run);
        Map<Identifier, ResolvedModel> resolvedModels = evt.getModelBakery().resolvedModels;
        //Look up the override parts from the already loaded mekatool model
        overrideLeftMekaToolParts = calculateOverrideMekaToolParts(resolvedModels.get(MEKATOOL_LEFT_ID));
        overrideRightMekaToolParts = calculateOverrideMekaToolParts(resolvedModels.get(MEKATOOL_RIGHT_ID));
    }

    public void reloadCallback(Runnable callback) {
        callbacks.add(callback);
    }

    /// Call via [mekanism.api.gear.IClientModuleHelper#addMekaSuitModuleModels(Identifier)].
    public ModuleOBJModelData registerMekaSuitModuleModel(Identifier rl) {
        ModuleOBJModelData data = register(rl, ModuleOBJModelData::new);
        mekaSuitModules.add(data);
        return data;
    }

    private Set<String> calculateOverrideMekaToolParts(@Nullable ResolvedModel model) {
        if (model != null && model.getTopGeometry() instanceof ObjGeometry geometry) {
            Set<String> overridden = new HashSet<>();
            for (String partName : geometry.getRootComponentNames()) {
                if (partName.contains(MekaSuitArmor.OVERRIDDEN_TAG)) {
                    //Note: We just ignore the pieces here as the override will be rendered as part of the item's model
                    overridden.add(MekaSuitArmor.processOverrideName(partName, "mekatool"));
                }
            }
            return Collections.unmodifiableSet(overridden);
        }
        return Collections.emptySet();
    }

    public Set<String> getOverrideMekaToolParts(boolean left) {
        return left ? overrideLeftMekaToolParts : overrideRightMekaToolParts;
    }
}
