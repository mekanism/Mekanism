#version 330
#extension GL_ARB_separate_shader_objects : require

#ifdef GLINT
#include <minecraft:globals.glsl>
#endif
#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:oit.glsl>

uniform sampler2D Sampler0;

#ifdef DISSOLVE
uniform sampler2D DissolveMaskSampler;
#endif

#ifdef GLINT
uniform sampler2D GlintSampler;
#endif

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
#ifdef PER_FACE_LIGHTING
layout(location = 2) in vec4 vertexPerFaceColorBack;
layout(location = 3) in vec4 vertexPerFaceColorFront;
//TODO - 26.3: Re-evaluate this
layout(location = 8) in vec4 shadedVertexPerFaceColorBack;
layout(location = 9) in vec4 shadedVertexPerFaceColorFront;
#else
layout(location = 2) in vec4 vertexColor;
layout(location = 3) in vec4 shadedVertexColor;
#endif

#ifndef EMISSIVE
layout(location = 4) in vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
layout(location = 5) in vec4 overlayColor;
#endif

layout(location = 6) in vec2 texCoord0;
#ifdef GLINT
layout(location = 7) in vec2 texCoordGlint;
#endif

#ifndef OIT_ALPHA_ONLY
layout(location = 0) out vec4 fragColor;
#endif

bool shouldTint(float red, float green, float blue);

vec4 calculateFinalColor(vec4 color) {
    #ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    #endif

    #ifndef EMISSIVE
    color *= lightMapColor;
    #endif

    #ifdef GLINT
    vec4 glintColor = GlintAlpha * texture(GlintSampler, texCoordGlint);
    // Matches BlendFuntion.GLINT
    color.rgb += glintColor.rgb * glintColor.rgb;
    #endif

    #ifdef OIT_ACCUMULATE
    color = sampleColorForAccumulation(color);
    vec4 fogColor = vec4(FogColor.rgb * color.a, FogColor.a);
    #else
    vec4 fogColor = FogColor;
    #endif

    return apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, fogColor);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    #ifdef OIT_ADDITIVE
    color.a = min(0.99, color.a);
    #endif

    #ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
    #endif

    //Move face vertex color out of the sub parts, mek
    vec4 faceVertexColor;
    #ifdef PER_FACE_LIGHTING
        if (shouldTint(color.r, color.g, color.b)) {
            faceVertexColor = gl_FrontFacing ? shadedVertexPerFaceColorFront : shadedVertexPerFaceColorBack;
        } else {
            faceVertexColor = gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack;
        }
    #else
        if (shouldTint(color.r, color.g, color.b)) {
            faceVertexColor = shadedVertexColor;
        } else {
            faceVertexColor = vertexColor;
        }
    #endif

    #ifdef DISSOLVE
        if (faceVertexColor.a < texture(DissolveMaskSampler, texCoord0).a) {
            discard;
        }
        // The dissolve effect entirely replaces translucency
        faceVertexColor.a = 1.0;
    #endif

    color *= faceVertexColor * ColorModulator;

    #ifdef GLINT
        color.a = max(color.a, GlintAlpha);
    #endif

    #ifdef OIT_ALPHA_ONLY
        executeAlphaOnlyPhase(gl_FragCoord.z, color.a);
    #else
        fragColor = calculateFinalColor(color);
    #endif
}

bool shouldTint(float red, float green, float blue) {
    float min = min(min(red, green), blue);
    float max = max(max(red, green), blue);
    float delta = max - min;
    //Calculate Saturation and Value components of HSV
    float saturation = max == 0.0 ? 0.0 : delta / max;
    float value = max;
    return value >= 0.48 && saturation <= 0.15;
}