#version 330
#extension GL_ARB_separate_shader_objects : require

#if defined(PER_FACE_LIGHTING) || !defined(NO_CARDINAL_LIGHTING)
#include <minecraft:light.glsl>
#endif
#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:sample_lightmap.glsl>

#define MEKANISM_NO_COLOR vec4(1, 1, 1, 1)

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;
layout(location = 5) in vec3 Normal;

#if !defined(NO_OVERLAY) && !defined(OIT_ALPHA_ONLY)
uniform sampler2D Sampler1;
#endif

#if !defined(EMISSIVE) && !defined(OIT_ALPHA_ONLY)
uniform sampler2D Sampler2;
#endif

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;

#ifdef PER_FACE_LIGHTING
layout(location = 2) out vec4 vertexPerFaceColorBack;
layout(location = 3) out vec4 vertexPerFaceColorFront;
//TODO - 26.3: Re-evaluate this
layout(location = 8) out vec4 shadedVertexPerFaceColorBack;
layout(location = 9) out vec4 shadedVertexPerFaceColorFront;
#else
layout(location = 2) out vec4 vertexColor;
layout(location = 3) out vec4 shadedVertexColor;
#endif

#ifndef EMISSIVE
layout(location = 4) out vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
layout(location = 5) out vec4 overlayColor;
#endif

layout(location = 6) out vec2 texCoord0;
#ifdef GLINT
layout(location = 7) out vec2 texCoordGlint;
#endif

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

#if !defined(EMISSIVE) && !defined(OIT_ALPHA_ONLY)
    lightMapColor = sample_lightmap(Sampler2, UV2);
#endif

#if !defined(NO_OVERLAY) && !defined(OIT_ALPHA_ONLY)
    overlayColor = texelFetch(Sampler1, UV1, 0);
#endif

    texCoord0 = UV0;

#ifdef APPLY_TEXTURE_MATRIX
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
#endif

#ifdef GLINT
    texCoordGlint = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
#endif
}