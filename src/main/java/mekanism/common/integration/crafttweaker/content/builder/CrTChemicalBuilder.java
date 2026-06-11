package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.util.ChemicalUtils;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister(loaders = CrTConstants.CONTENT_LOADER)
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_CHEMICAL)
public class CrTChemicalBuilder {

    private final ChemicalBuilder builder;
    @Nullable
    private Integer colorRepresentation;

    private CrTChemicalBuilder(ChemicalBuilder builder) {
        this.builder = builder;
    }

    /// Sets the tint to apply to this chemical when rendering.
    ///
    /// @param tint Color in AARRGGBB format
    @ZenCodeType.Method
    public CrTChemicalBuilder tint(int tint) {
        builder.tint(tint);
        return this;
    }

    /// Sets the color representation to apply to this chemical when used for things like durability bars. Mostly for use in combination with custom textures that are not
    /// tinted.
    ///
    /// @param color Color in AARRGGBB format
    @ZenCodeType.Method
    public CrTChemicalBuilder colorRepresentation(int color) {
        colorRepresentation = color;
        return this;
    }

    /// Create a chemical from this builder with the given name.
    ///
    /// @param name Registry name for the chemical.
    @ZenCodeType.Method
    public void build(String name) {
        build(Mekanism.hooks.craftTweaker.rl(name));
    }

    /// Create a chemical from this builder with the given name.
    ///
    /// @param registryName Registry name for the chemical.
    protected void build(Identifier registryName) {
        Chemical chemical = ChemicalUtils.chemical(builder, colorRepresentation);
        CrTContentUtils.queueChemicalForRegistration(registryName, chemical);
    }

    /// Creates a builder for registering a custom [Chemical].
    ///
    /// @param textureLocation If present the [Identifier] representing the texture this [Chemical] will use, otherwise defaults to our default Gas texture.
    ///
    /// @return A builder for creating a custom [Chemical].
    ///
    /// @apiNote If a custom texture is used it is recommended to override to use [#colorRepresentation(int)] if this builder method is not being used in combination with
    /// [#tint(int)] due to the texture not needing tinting.
    @ZenCodeType.Method
    public static CrTChemicalBuilder builder(@ZenCodeType.Optional Identifier textureLocation) {
        return new CrTChemicalBuilder(textureLocation == null ? ChemicalBuilder.builder() : ChemicalBuilder.builder(textureLocation));
    }

    /// Creates a builder for registering a custom [Chemical] with the default infuse type texture.
    ///
    /// @return A builder for creating a custom [Chemical].
    @ZenCodeType.Method
    public static CrTChemicalBuilder infuseType() {
        return new CrTChemicalBuilder(ChemicalBuilder.infuseType());
    }

    /// Creates a builder for registering a custom [Chemical] with the default pigment texture.
    ///
    /// @return A builder for creating a custom [Chemical].
    @ZenCodeType.Method
    public static CrTChemicalBuilder pigment() {
        return new CrTChemicalBuilder(ChemicalBuilder.pigment());
    }

    /// Creates a builder for registering a custom [Chemical], using our default clean Slurry texture.
    ///
    /// @return A builder for creating a custom [Chemical].
    @ZenCodeType.Method
    public static CrTChemicalBuilder clean() {
        return new CrTChemicalBuilder(ChemicalBuilder.cleanSlurry());
    }

    /// Creates a builder for registering a custom [Chemical], using our default dirty Slurry texture.
    ///
    /// @return A builder for creating a custom [Chemical].
    @ZenCodeType.Method
    public static CrTChemicalBuilder dirty() {
        return new CrTChemicalBuilder(ChemicalBuilder.dirtySlurry());
    }
}