package buildcraft.api.transport.pipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class PipeDefinition {
    public final ResourceLocation identifier;
    public final IPipeCreator logicConstructor;
    public final IPipeLoader logicLoader;
    public final PipeFlowType flowType;
    public final String[] textures;
    public final int itemTextureTop, itemTextureCenter, itemTextureBottom;

    public PipeDefinition(PipeDefinitionBuilder builder) {
        this.identifier = builder.identifier;
        this.textures = new String[builder.textureSuffixes.length];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = builder.texturePrefix + builder.textureSuffixes[i];
        }
        this.logicConstructor = builder.logicConstructor;
        this.logicLoader = builder.logicLoader;
        this.flowType = builder.flowType;
        this.itemTextureTop = builder.itemTextureTop;
        this.itemTextureCenter = builder.itemTextureCenter;
        this.itemTextureBottom = builder.itemTextureBottom;
    }

    @FunctionalInterface
    public interface IPipeCreator {
        PipeBehaviour createBehaviour(IPipe t);
    }

    @FunctionalInterface
    public interface IPipeLoader {
        PipeBehaviour loadBehaviour(IPipe t, NBTTagCompound u);
    }

    public static class PipeDefinitionBuilder {
        public ResourceLocation identifier;
        public String texturePrefix;
        public String[] textureSuffixes = { "" };
        public IPipeCreator logicConstructor;
        public IPipeLoader logicLoader;
        public PipeFlowType flowType;
        public int itemTextureTop = 0;
        public int itemTextureCenter = 0;
        public int itemTextureBottom = 0;

        public PipeDefinitionBuilder() {}

        public PipeDefinitionBuilder(ResourceLocation identifier, IPipeCreator logicConstructor, IPipeLoader logicLoader, PipeFlowType flowType) {
            this.identifier = identifier;
            this.logicConstructor = logicConstructor;
            this.logicLoader = logicLoader;
            this.flowType = flowType;
        }

        public PipeDefinitionBuilder idTexPrefix(String both) {
            return id(both).texPrefix(both);
        }

        public PipeDefinitionBuilder idTex(String both) {
            return id(both).tex(both);
        }

        private static String getActiveModId() {
            ModContainer mod = Loader.instance().activeModContainer();
            if (mod == null) {
                throw new IllegalStateException("Cannot interact with PipeDefinition outside of an activly scoped mod!");
            }
            return mod.getModId();
        }

        public PipeDefinitionBuilder id(String post) {
            identifier = new ResourceLocation(getActiveModId(), post);
            return this;
        }

        public PipeDefinitionBuilder tex(String prefix, String... suffixes) {
            return texPrefix(prefix).texSuffixes(suffixes);
        }

        /** Sets the texture prefix to be: <code>[current_mod_id]:pipes/[prefix]</code> where [current_mod_id] is the
         * modid of the currently loaded mod, and [prefix] is the string parameter given.
         * 
         * @return this */
        public PipeDefinitionBuilder texPrefix(String prefix) {
            return texPrefixDirect(getActiveModId() + ":pipes/" + prefix);
        }

        /** Sets the {@link #texturePrefix} to the input string, without any additions or changes (unlike
         * {@link #texPrefix(String)})
         * 
         * @return this */
        public PipeDefinitionBuilder texPrefixDirect(String prefix) {
            texturePrefix = prefix;
            return this;
        }

        /** Sets {@link #textureSuffixes} to the given array, or to <code>{""}</code> if the argument list is empty or
         * null.
         * 
         * @return this. */
        public PipeDefinitionBuilder texSuffixes(String... suffixes) {
            if (suffixes == null || suffixes.length == 0) {
                textureSuffixes = new String[] { "" };
            } else {
                textureSuffixes = suffixes;
            }
            return this;
        }

        public PipeDefinitionBuilder itemTex(int all) {
            itemTextureTop = all;
            itemTextureCenter = all;
            itemTextureBottom = all;
            return this;
        }

        public PipeDefinitionBuilder itemTex(int top, int center, int bottom) {
            itemTextureTop = top;
            itemTextureCenter = center;
            itemTextureBottom = bottom;
            return this;
        }

        public PipeDefinitionBuilder logic(IPipeCreator creator, IPipeLoader loader) {
            logicConstructor = creator;
            logicLoader = loader;
            return this;
        }

        public PipeDefinitionBuilder flowItem() {
            return flow(PipeApi.flowItems);
        }

        public PipeDefinitionBuilder flowFluid() {
            return flow(PipeApi.flowFluids);
        }

        public PipeDefinitionBuilder flowPower() {
            return flow(PipeApi.flowPower);
        }

        public PipeDefinitionBuilder flow(PipeFlowType flow) {
            flowType = flow;
            return this;
        }

        public PipeDefinition define() {
            PipeDefinition def = new PipeDefinition(this);
            PipeApi.pipeRegistry.registerPipe(def);
            return def;
        }
    }
}
