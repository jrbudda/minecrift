package com.mtbs3d.minecrift.render;

public class VRShaders {
	private VRShaders() {
	}
	
	public static final String DEPTH_MASK_VERTEX_SHADER = 
			"#version 110\n" +
			"void main() {\n" +
			    "gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
			"}\n";

	public static final String DEPTH_MASK_FRAGMENT_SHADER = 
			"#version 330\n" +
			"uniform vec2 resolution;\n"+
			"uniform vec2 position;\n"+
			"uniform sampler2D colorTex;\n"+
			"uniform sampler2D depthTex;\n"+
			"uniform vec3 hmdViewPosition;\n"+
			"uniform vec3 hmdPlaneNormal;\n"+
			"uniform mat4 projectionMatrix;\n"+
			"uniform mat4 viewMatrix;\n"+
			"uniform int pass;\n"+
			"uniform vec3 keyColor;\n"+
			"out vec4 out_Color;\n"+
			"vec3 getFragmentPosition(vec2 coord) {\n"+
				"vec4 posScreen = vec4(coord * 2.0 - 1.0, texture(depthTex, coord).x * 2.0 - 1.0, 1);\n"+
				"vec4 posView = inverse(projectionMatrix * viewMatrix) * posScreen;\n"+
				"return posView.xyz / posView.w;\n"+
			"}\n"+
			"void main(void) {\n" + 
				"vec2 pos = (gl_FragCoord.xy - position) / resolution;\n" +
				"vec3 fragPos = getFragmentPosition(pos);\n"+
				"float fragHmdDot = dot(fragPos - hmdViewPosition, hmdPlaneNormal);\n"+
				"if ((pass == 0 && fragHmdDot >= 0) || pass == 1) {\n"+
			    	"vec4 color = texture(colorTex, pos);\n"+
			    	"vec3 diff = color.rgb - keyColor;\n"+ // The following code prevents actual colors from matching the key color and looking weird
			    	"if (keyColor.r < 0.004 && keyColor.g < 0.004 && keyColor.b < 0.004 && color.r < 0.004 && color.g < 0.004 && color.b < 0.004) {\n"+
		    			"color = vec4(0.004, 0.004, 0.004, 1);\n"+
			    	"} else if (diff.r < 0.004 && diff.g < 0.004 && diff.b < 0.004) {\n"+
		    			"color = vec4(color.r - 0.004, color.g - 0.004, color.b - 0.004, color.a);\n"+
			    	"}\n"+
					"out_Color = color;\n" +
					//"out_Color = vec4(vec3( (distance(fragPos.xz,hmdViewPosition.xz)) / 3), 1);\n"+ // Draw depth buffer
				"} else {\n"+
					"discard;\n"+ // Throw out the fragment to save some GPU processing
					//"out_Color = vec4(1, 0, 1, 1);\n"+
				"}\n"+
			"}\n";
	
	public static final String LANCZOS_SAMPLER_VERTEX_SHADER =
			"#version 120\n" +
					"\n" +
					" uniform float texelWidthOffset;\n" +
					" uniform float texelHeightOffset;\n" +
					"\n" +
					" varying vec2 centerTextureCoordinate;\n" +
					" varying vec2 oneStepLeftTextureCoordinate;\n" +
					" varying vec2 twoStepsLeftTextureCoordinate;\n" +
					" varying vec2 threeStepsLeftTextureCoordinate;\n" +
					" varying vec2 fourStepsLeftTextureCoordinate;\n" +
					" varying vec2 oneStepRightTextureCoordinate;\n" +
					" varying vec2 twoStepsRightTextureCoordinate;\n" +
					" varying vec2 threeStepsRightTextureCoordinate;\n" +
					" varying vec2 fourStepsRightTextureCoordinate;\n" +
					"\n" +
					" void main()\n" +
					" {\n" +
					"     gl_Position = ftransform();\n" +
					"\n" +
					"     vec2 firstOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
					"     vec2 secondOffset = vec2(2.0 * texelWidthOffset, 2.0 * texelHeightOffset);\n" +
					"     vec2 thirdOffset = vec2(3.0 * texelWidthOffset, 3.0 * texelHeightOffset);\n" +
					"     vec2 fourthOffset = vec2(4.0 * texelWidthOffset, 4.0 * texelHeightOffset);\n" +
					"\n" +
					"     vec2 textCoord = gl_MultiTexCoord0.xy;\n" +
					"     centerTextureCoordinate = textCoord;\n" +
					"     oneStepLeftTextureCoordinate = textCoord - firstOffset;\n" +
					"     twoStepsLeftTextureCoordinate = textCoord - secondOffset;\n" +
					"     threeStepsLeftTextureCoordinate = textCoord - thirdOffset;\n" +
					"     fourStepsLeftTextureCoordinate = textCoord - fourthOffset;\n" +
					"     oneStepRightTextureCoordinate = textCoord + firstOffset;\n" +
					"     twoStepsRightTextureCoordinate = textCoord + secondOffset;\n" +
					"     threeStepsRightTextureCoordinate = textCoord + thirdOffset;\n" +
					"     fourStepsRightTextureCoordinate = textCoord + fourthOffset;\n" +
					" }\n";

	public static final String LANCZOS_SAMPLER_FRAGMENT_SHADER =

			"#version 120\n" +
					"\n" +
					" uniform sampler2D inputImageTexture;\n" +
					"\n" +
					" varying vec2 centerTextureCoordinate;\n" +
					" varying vec2 oneStepLeftTextureCoordinate;\n" +
					" varying vec2 twoStepsLeftTextureCoordinate;\n" +
					" varying vec2 threeStepsLeftTextureCoordinate;\n" +
					" varying vec2 fourStepsLeftTextureCoordinate;\n" +
					" varying vec2 oneStepRightTextureCoordinate;\n" +
					" varying vec2 twoStepsRightTextureCoordinate;\n" +
					" varying vec2 threeStepsRightTextureCoordinate;\n" +
					" varying vec2 fourStepsRightTextureCoordinate;\n" +
					"\n" +
					" // sinc(x) * sinc(x/a) = (a * sin(pi * x) * sin(pi * x / a)) / (pi^2 * x^2)\n" +
					" // Assuming a Lanczos constant of 2.0, and scaling values to max out at x = +/- 1.5\n" +
					"\n" +
					" void main()\n" +
					" {\n" +
					"     vec4 fragmentColor = texture2D(inputImageTexture, centerTextureCoordinate) * 0.38026;\n" +
					"\n" +
					"     fragmentColor += texture2D(inputImageTexture, oneStepLeftTextureCoordinate) * 0.27667;\n" +
					"     fragmentColor += texture2D(inputImageTexture, oneStepRightTextureCoordinate) * 0.27667;\n" +
					"\n" +
					"     fragmentColor += texture2D(inputImageTexture, twoStepsLeftTextureCoordinate) * 0.08074;\n" +
					"     fragmentColor += texture2D(inputImageTexture, twoStepsRightTextureCoordinate) * 0.08074;\n" +
					"\n" +
					"     fragmentColor += texture2D(inputImageTexture, threeStepsLeftTextureCoordinate) * -0.02612;\n" +
					"     fragmentColor += texture2D(inputImageTexture, threeStepsRightTextureCoordinate) * -0.02612;\n" +
					"\n" +
					"     fragmentColor += texture2D(inputImageTexture, fourStepsLeftTextureCoordinate) * -0.02143;\n" +
					"     fragmentColor += texture2D(inputImageTexture, fourStepsRightTextureCoordinate) * -0.02143;\n" +
					"\n" +
					"     gl_FragColor = fragmentColor;\n" +
					" }\n";

	public static final String FOV_REDUCTION_VERTEX_SHADER = 
			"#version 110\n" +
			"void main() {\n" +
			    "gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
			    "gl_TexCoord[0] = gl_MultiTexCoord0;" +
			"}\n";
	
	public static final String FOV_REDUCTION_FRAGMENT_SHADER =
			"#version 120\n" +
					"\n" +
					" uniform sampler2D tex0;\n" +
					" uniform float circle_radius;"+
					" uniform float border;"+
					"\n" +				
					" void main(){\n" +
					"  vec4 circle_color = vec4(0.0, 0, 0, 1.0);"+
					"  vec2 circle_center = vec2(0.5, 0.5);"+
					"  vec2 uv = gl_TexCoord[0].xy; "+				  
					"  vec4 bkg_color = texture2D(tex0,gl_TexCoord[0].st); "+					  
					"  uv -= circle_center; "+					  
					"  float dist =  sqrt(dot(uv, uv)); "+
					"  float t = 1.0 + smoothstep(circle_radius, circle_radius+10, dist)"+ 
		            "  - smoothstep(circle_radius-border, circle_radius, dist);"+
					"  gl_FragColor = mix(circle_color, bkg_color,t);"+
					" }\n";
	
}




