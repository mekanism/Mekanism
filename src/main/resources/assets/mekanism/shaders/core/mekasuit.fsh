#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

#ifdef DISSOLVE
uniform sampler2D DissolveMaskSampler;
#endif

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
#ifdef PER_FACE_LIGHTING
in vec4 vertexPerFaceColorBack;
in vec4 vertexPerFaceColorFront;
in vec4 shadedVertexPerFaceColorBack;
in vec4 shadedVertexPerFaceColorFront;
#else
in vec4 vertexColor;
in vec4 shadedVertexColor;
#endif

#ifndef EMISSIVE
in vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
in vec4 overlayColor;
#endif

in vec2 texCoord0;

out vec4 fragColor;

bool shouldTint(float red, float green, float blue);

void main() {
    vec4 color = texture(Sampler0, texCoord0);
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif

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
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
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