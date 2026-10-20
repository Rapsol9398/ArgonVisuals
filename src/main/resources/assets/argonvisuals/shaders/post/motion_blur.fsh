#version 330

layout(std140) uniform MotionBlurConfig {
    float Blend;
};

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 current = texture(InSampler, texCoord).rgb;
    vec4 previous = texture(PrevSampler, texCoord);
    float blend = previous.a > 0.5 ? Blend : 0.0;
    fragColor = vec4(mix(current, previous.rgb, blend), 1.0);
}
