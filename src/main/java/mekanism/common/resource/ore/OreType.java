package mekanism.common.resource.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.common.resource.IResource;
import mekanism.common.resource.MiscResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.world.height.HeightShape;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

public enum OreType implements StringRepresentable {
    TIN(PrimaryResource.TIN, 0.7F,
          new BaseOreConfig("small", 14, 0, 4, HeightShape.TRAPEZOID, OreAnchor.absolute(-20), OreAnchor.absolute(94)),
          new BaseOreConfig("large", 12, 0, 9, HeightShape.TRAPEZOID, OreAnchor.absolute(-32), OreAnchor.absolute(72))
    ),
    OSMIUM(PrimaryResource.OSMIUM, 0.7F,
          new BaseOreConfig("upper", 65, 0, 7, HeightShape.TRAPEZOID, OreAnchor.absolute(72), OreAnchor.belowTop(-24), 8),
          new BaseOreConfig("middle", 6, 0, 9, HeightShape.TRAPEZOID, OreAnchor.absolute(-32), OreAnchor.absolute(56)),
          new BaseOreConfig("small", 8, 0, 4, HeightShape.UNIFORM, OreAnchor.aboveBottom(0), OreAnchor.absolute(64))
    ),
    URANIUM(PrimaryResource.URANIUM, 1.25F,
          new BaseOreConfig("small", 4, 0, 4, HeightShape.TRAPEZOID, OreAnchor.aboveBottom(0), OreAnchor.absolute(8)),
          new BaseOreConfig("buried", 7, 0.75F, 9, HeightShape.TRAPEZOID, OreAnchor.aboveBottom(-24), OreAnchor.aboveBottom(56), 16)
    ),
    FLUORITE(MiscResource.FLUORITE, 1, 4, 0.7F,
          new BaseOreConfig("normal", 5, 0, 5, HeightShape.UNIFORM, OreAnchor.aboveBottom(0), OreAnchor.absolute(23)),
          new BaseOreConfig("buried", 3, 1, 13, HeightShape.TRAPEZOID, OreAnchor.aboveBottom(0), OreAnchor.absolute(4))
    ),
    LEAD(PrimaryResource.LEAD, 1F,
          new BaseOreConfig("normal", 8, 0.25F, 9, HeightShape.TRAPEZOID, OreAnchor.aboveBottom(-24), OreAnchor.absolute(64))
    );

    /// Cached value of [OreType#values()]
    @Unmodifiable
    public static final List<OreType> VALUES = List.of(values());
    public static Codec<OreType> CODEC = StringRepresentable.fromEnum(OreType::values);

    private final List<BaseOreConfig> baseConfigs;
    private final IResource resource;
    private final int minExp;
    private final int maxExp;
    private final float smeltingExp;

    OreType(IResource resource, float smeltingExp, BaseOreConfig... configs) {
        this(resource, 0, 0, smeltingExp, configs);
    }

    OreType(IResource resource, int minExp, int maxExp, float smeltingExp, BaseOreConfig... configs) {
        this.resource = resource;
        this.minExp = minExp;
        this.maxExp = maxExp;
        this.smeltingExp = smeltingExp;
        this.baseConfigs = List.of(configs);
    }

    public IResource getResource() {
        return resource;
    }

    public List<BaseOreConfig> getBaseConfigs() {
        return baseConfigs;
    }

    public int getMinExp() {
        return minExp;
    }

    public int getMaxExp() {
        return maxExp;
    }

    public float getSmeltingXp() {
        return smeltingExp;
    }

    @Nullable
    public static OreType get(IResource resource) {
        for (OreType ore : VALUES) {
            if (resource == ore.resource) {
                return ore;
            }
        }
        return null;
    }

    @Override
    public String getSerializedName() {
        return resource.getRegistrySuffix();
    }

    public boolean isMetal() {
        return this != FLUORITE;
    }

    public record OreVeinType(OreType type, int index) {

        public static final Codec<OreVeinType> CODEC = RecordCodecBuilder.create(builder -> builder.group(
              OreType.CODEC.fieldOf(SerializationConstants.TYPE).forGetter(config -> config.type),
              Codec.INT.fieldOf(SerializationConstants.INDEX).forGetter(config -> config.index)
        ).apply(builder, OreVeinType::new));

        public OreVeinType {
            if (index < 0 || index >= type.getBaseConfigs().size()) {
                throw new IndexOutOfBoundsException("Vein Type index out of range: " + index);
            }
        }

        public String name() {
            return "ore_" + type.getResource().getRegistrySuffix() + "_" + type.getBaseConfigs().get(index).name();
        }
    }
}