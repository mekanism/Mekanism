package mekanism.client.model;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import mekanism.client.ModelUtil;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.ComposedModelState;
import net.neoforged.neoforge.client.model.obj.ObjGeometry;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class BaseModelCache {

    private final Map<Identifier, MekanismModelData> modelMap = new Object2ObjectOpenHashMap<>();

    private final String modid;

    protected BaseModelCache(String modid) {
        this.modid = modid;
    }

    private Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(modid, path);
    }

    public void onBake(ModelEvent.BakingCompleted evt) {
        for (MekanismModelData m : modelMap.values()) {
            m.reload(evt);
        }
    }

    public void setup(ModelEvent.RegisterStandalone event) {
        for (MekanismModelData mekanismModelData : modelMap.values()) {
            mekanismModelData.setup(event);
        }
    }

    protected OBJModelData registerOBJ(String path) {
        return registerOBJ(rl(path));
    }

    protected OBJModelData registerOBJ(Identifier rl) {
        return register(rl, OBJModelData::new);
    }

    protected BlockStateModelPartHelper registerJSON(String path) {
        return registerJSON(rl(path));
    }

    protected BlockStateModelPartHelper registerJSON(Identifier rl) {
        return register(rl, BlockStateModelPartHelper::new);
    }

    protected <DATA extends MekanismModelData> DATA register(Identifier rl, Function<Identifier, DATA> creator) {
        DATA data = creator.apply(rl);
        modelMap.put(rl, data);
        return data;
    }

    public static class MekanismModelData implements ModelDebugName {

        protected final Identifier rl;

        protected MekanismModelData(Identifier rl) {
            this.rl = rl;
        }

        @Override
        public String debugName() {
            return rl.toDebugFileName();
        }

        protected void reload(ModelEvent.BakingCompleted evt) {
        }

        protected void setup(ModelEvent.RegisterStandalone event) {
        }
    }

    //TODO - 26.2: Move this into MekaSuitArmor?
    public static class OBJModelData extends MekanismModelData {

        private final StandaloneModelKey<ObjModelSettings> key;
        private final ModelState modelState;
        @Nullable
        private ObjModelSettings settings;

        protected OBJModelData(Identifier rl) {
            super(rl);
            key = new StandaloneModelKey<>(this);
            Matrix4f matrix = new Matrix4f();
            matrix.rotate(Axis.ZP.rotation(Mth.PI));
            this.modelState = new ComposedModelState(BlockModelRotation.IDENTITY, new Transformation(matrix));
        }

        @Override
        protected void reload(ModelEvent.BakingCompleted evt) {
            super.reload(evt);
            settings = evt.getBakingResult().standaloneModels().get(key);
        }

        @Override
        protected void setup(ModelEvent.RegisterStandalone event) {
            event.register(key, new SimpleUnbakedStandaloneModel<>(rl, (model, baker, _) -> new ObjModelSettings(model, baker)));
        }

        //TODO - 26.2: Can we precalculate the various part combinations that we sometimes have and just bake them?
        public List<BlockStateModelPart> getParts(Set<String> partNames) {
            if (settings == null || partNames.isEmpty()) {
                return Collections.emptyList();
            }
            //TODO - 26.2: Cache the result of this method? May not fully matter because it is used from a cache within MekaSuitArmor, but might still be worth it
            ResolvedModel resolvedModel = settings.resolvedModel();
            Map<String, Boolean> visibility = new HashMap<>();
            for (String part : getPartNames()) {
                visibility.put(part, partNames.contains(part));
            }
            try {
                //TODO - 26.2: More useful debug name that takes into account what parts we are getting the results for?
                QuadCollection quadCollection = resolvedModel.getTopGeometry().bake(resolvedModel.getTopTextureSlots(), settings.baker, modelState, resolvedModel, ModelUtil.partVisibility(resolvedModel, visibility));
                //we don't intend to use the particle, so no point resolving it
                BlockStateModelPart bakedModel = new SimpleModelWrapper(quadCollection, resolvedModel.getTopAmbientOcclusion(), settings.baker.missingBlockModelPart().particleMaterial());
                return Collections.singletonList(bakedModel);
            } catch (Exception e) {
                Mekanism.logger.error("Unable to bake {} model due to exception", resolvedModel.debugName(), e);
            }
            return Collections.emptyList();
        }

        public Set<String> getPartNames() {
            return settings == null ? Collections.emptySet() : settings.geometry().getRootComponentNames();
        }

        private record ObjModelSettings(ResolvedModel resolvedModel, ModelBaker baker) {

            private ObjGeometry geometry() {
                return (ObjGeometry) resolvedModel.getTopGeometry();
            }
        }
    }

    public static class BlockStateModelPartHelper extends MekanismModelData {

        //this is a list due to SubmitNodeCollector wanting a list
        private List<BlockStateModelPart> bakedModel = Collections.emptyList();
        private final StandaloneModelKey<BlockStateModelPart> key;

        private BlockStateModelPartHelper(Identifier rl) {
            super(rl);
            key = new StandaloneModelKey<>(this);
        }

        @Override
        protected void reload(ModelEvent.BakingCompleted evt) {
            super.reload(evt);
            BlockStateModelPart modelPart = evt.getBakingResult().standaloneModels().get(key);
            bakedModel = modelPart == null ? Collections.emptyList() : List.of(modelPart);
        }

        @Override
        protected void setup(ModelEvent.RegisterStandalone event) {
            event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(rl));
        }

        public List<BlockStateModelPart> getBakedModel() {
            return bakedModel;
        }
    }
}
