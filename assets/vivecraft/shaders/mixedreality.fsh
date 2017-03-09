#version 330
uniform vec2 resolution;
uniform vec2 position;
uniform sampler2D colorTex;
uniform sampler2D depthTex;
uniform vec3 hmdViewPosition;
uniform vec3 hmdPlaneNormal;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform int pass;
uniform vec3 keyColor;
uniform int alphaMode;
out vec4 out_Color;
vec3 getFragmentPosition(vec2 coord) {
	vec4 posScreen = vec4(coord * 2.0 - 1.0, texture(depthTex, coord).x * 2.0 - 1.0, 1);
	vec4 posView = inverse(projectionMatrix * viewMatrix) * posScreen;
	return posView.xyz / posView.w;
}
void main(void) {
	vec2 pos = (gl_FragCoord.xy - position) / resolution;
	vec3 fragPos = getFragmentPosition(pos);
	float fragHmdDot = dot(fragPos - hmdViewPosition, hmdPlaneNormal);
	if (((pass == 0 || pass == 2) && fragHmdDot >= 0) || pass == 1) {
		vec4 color = texture(colorTex, pos);
		if (pass == 2) {
			color = vec4(1, 1, 1, 1);
		} else if (alphaMode == 0) {
			vec3 diff = color.rgb - keyColor; // The following code prevents actual colors from matching the key color and looking weird
			if (keyColor.r < 0.004 && keyColor.g < 0.004 && keyColor.b < 0.004 && color.r < 0.004 && color.g < 0.004 && color.b < 0.004) {
				color = vec4(0.004, 0.004, 0.004, 1);
			} else if (diff.r < 0.004 && diff.g < 0.004 && diff.b < 0.004) {
				color = vec4(color.r - 0.004, color.g - 0.004, color.b - 0.004, color.a);
			}
		}
		out_Color = color;
		//out_Color = vec4(vec3( (distance(fragPos.xz,hmdViewPosition.xz)) / 3), 1); // Draw depth buffer
	} else {
		discard; // Throw out the fragment to save some GPU processing
		//out_Color = vec4(1, 0, 1, 1);
	}
}