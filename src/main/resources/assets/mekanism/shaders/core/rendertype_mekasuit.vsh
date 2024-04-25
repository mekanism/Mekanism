#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

#define MEKANISM_NO_COLOR vec4(1, 1, 1, 1)

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
out vec4 vertexColor;
out vec4 shadedVertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;
out vec4 normal;

void main() {
    //Like rendertype_entity_cutout except we calculate vertex colors for passed in and for non recoloring
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    //Calculate theactual tint to apply based on the passed in alpha value
    vec4 tint = vec4(mix(MEKANISM_NO_COLOR.rgb, Color.rgb, Color.a), MEKANISM_NO_COLOR.a);

    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, MEKANISM_NO_COLOR);
    shadedVertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, tint);
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}