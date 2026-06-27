#version 330

#if defined(PER_FACE_LIGHTING) || !defined(NO_CARDINAL_LIGHTING)
#moj_import <minecraft:light.glsl>
#endif
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

#define MEKANISM_NO_COLOR vec4(1, 1, 1, 1)

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

#ifndef NO_OVERLAY
uniform sampler2D Sampler1;
#endif
#ifndef EMISSIVE
uniform sampler2D Sampler2;
#endif

out float sphericalVertexDistance;
out float cylindricalVertexDistance;

#ifdef PER_FACE_LIGHTING
out vec4 vertexPerFaceColorBack;
out vec4 vertexPerFaceColorFront;
out vec4 shadedVertexPerFaceColorBack;
out vec4 shadedVertexPerFaceColorFront;
#else
out vec4 vertexColor;
out vec4 shadedVertexColor;
#endif

#ifndef EMISSIVE
out vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
out vec4 overlayColor;
#endif

out vec2 texCoord0;

void main() {
    //Like core/entity.vsh except we calculate vertex colors for passed in and for non recoloring
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    //Calculate the actual tint to apply based on the passed in alpha value
    vec4 tint = vec4(mix(MEKANISM_NO_COLOR.rgb, Color.rgb, Color.a), MEKANISM_NO_COLOR.a);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);

#ifdef PER_FACE_LIGHTING
    vec2 light = minecraft_compute_light(Light0_Direction, Light1_Direction, Normal);
    vertexPerFaceColorBack = minecraft_mix_light_separate(-light, MEKANISM_NO_COLOR);
    vertexPerFaceColorFront = minecraft_mix_light_separate(light, MEKANISM_NO_COLOR);

    shadedVertexPerFaceColorBack = minecraft_mix_light_separate(-light, tint);
    shadedVertexPerFaceColorFront = minecraft_mix_light_separate(light, tint);
#elif defined(NO_CARDINAL_LIGHTING)
    vertexColor = MEKANISM_NO_COLOR;
    shadedVertexColor = tint;
#else
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, MEKANISM_NO_COLOR);
    shadedVertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, tint);
#endif

#ifndef EMISSIVE
    lightMapColor = sample_lightmap(Sampler2, UV2);
#endif

#ifndef NO_OVERLAY
    overlayColor = texelFetch(Sampler1, UV1, 0);
#endif

    texCoord0 = UV0;

#ifdef APPLY_TEXTURE_MATRIX
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
#endif
}