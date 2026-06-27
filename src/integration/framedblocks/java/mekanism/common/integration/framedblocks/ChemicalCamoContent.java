package mekanism.common.integration.framedblocks;

import io.github.xfacthd.framedblocks.api.camo.CamoContent;
import io.github.xfacthd.framedblocks.api.camo.CamoContentClientHandler;
import io.github.xfacthd.framedblocks.api.camo.resource.ResourceCamoContent;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.integration.framedblocks.ChemicalCamoClientHandler;
import mekanism.common.registration.impl.FluidDeferredRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.Nullable;

public final class ChemicalCamoContent extends ResourceCamoContent<ChemicalResource, ChemicalCamoContent> {

    private final MapColor mapColor;

    ChemicalCamoContent(ChemicalResource chemicalType) {
        super(chemicalType);
        this.mapColor = FluidDeferredRegister.getClosestColor(resource.value().colorRepresentation());
    }

    @Override
    public boolean propagatesSkylightDown() {
        return true;
    }

    @Override
    public float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
        return 0;
    }

    @Override
    public boolean isFlammable(BlockGetter level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public int getFlammability(BlockGetter level, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockGetter level, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public float getShadeBrightness(BlockGetter level, BlockPos pos, float frameShade) {
        return 1F;
    }

    @Override
    public boolean isIgnitedByLava(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return false;
    }

    @Override
    public int getLightEmission() {
        // TODO: light level is currently not forwarded from ChemicalConstants to the registered Chemical
        return 0;
    }

    @Override
    public boolean isEmissive() {
        return false;
    }

    @Override
    public SoundType getSoundType() {
        return SoundType.WET_GRASS;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockAndLightGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public float getFriction(LevelReader level, BlockPos pos, @Nullable Entity entity, float frameFriction) {
        return frameFriction;
    }

    @Override
    public TriState canSustainPlant(BlockGetter level, BlockPos pos, Direction side, BlockState plant) {
        return TriState.DEFAULT;
    }

    @Override
    public boolean canEntityDestroy(BlockGetter level, BlockPos pos, Entity entity) {
        return true;
    }

    @Override
    public MapColor getMapColor(BlockGetter level, BlockPos pos) {
        return mapColor;
    }

    @Override
    public Integer getBeaconColorMultiplier(LevelReader levelReader, BlockPos pos, BlockPos beaconPos) {
        return getResource().value().colorRepresentation();
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canOcclude() {
        return false;
    }

    @Override
    public BlockState getAsBlockState() {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState getAppearanceState() {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isOccludedBy(BlockState adjState, BlockGetter level, BlockPos pos, BlockPos adjPos, Direction direction) {
        return adjState.isSolidRender();
    }

    @Override
    public boolean isOccludedBy(CamoContent<?> adjCamo, BlockGetter level, BlockPos pos, BlockPos adjPos, Direction direction) {
        return adjCamo.isSolid() || equals(adjCamo);
    }

    @Override
    public boolean occludes(BlockState adjState, BlockGetter level, BlockPos pos, BlockPos adjPos, Direction direction) {
        return false;
    }

    @Override
    public ParticleOptions makeRunningLandingParticles(BlockPos pos) {
        return new ChemicalParticleOptions(resource);
    }

    @Override
    public String getCamoId() {
        return resource.typeHolder().getRegisteredName();
    }

    @Override
    public MutableComponent getCamoName() {
        return TextComponentUtil.build(resource);
    }

    @Override
    public CamoContentClientHandler<ChemicalCamoContent> getClientHandler() {
        return ChemicalCamoClientHandler.INSTANCE;
    }

    @Override
    public int hashCode() {
        return resource.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != ChemicalCamoContent.class) return false;
        return resource.equals(((ChemicalCamoContent) obj).resource);
    }

    @Override
    public String toString() {
        return "ChemicalCamoContent{" + getCamoId() + "}";
    }
}
