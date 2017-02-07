package com.mtbs3d.minecrift.render;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.ARBShaderObjects;

import net.minecraft.client.Minecraft;

public class VRShaders {
	public static int _Lanczos_shaderProgramId = -1;
	public static int _LanczosShader_texelWidthOffsetUniform = -1;
	public static int _LanczosShader_texelHeightOffsetUniform = -1;
	public static int _LanczosShader_inputImageTextureUniform = -1;
	
	public static int _DepthMask_shaderProgramId = -1;
	public static int _DepthMask_resolutionUniform = -1;
	public static int _DepthMask_positionUniform = -1;
	public static int _DepthMask_scaleUniform = -1;
	public static int _DepthMask_colorTexUniform = -1;
	public static int _DepthMask_depthTexUniform = -1;
	public static int _DepthMask_hmdViewPosition = -1;
	public static int _DepthMask_hmdPlaneNormal = -1;
	public static int _DepthMask_projectionMatrix = -1;
	public static int _DepthMask_viewMatrix = -1;
	public static int _DepthMask_passUniform = -1;
	public static int _DepthMask_keyColorUniform = -1;
	public static int _DepthMask_alphaModeUniform = -1;

	public static int _FOVReduction_Enabled = -1;
	public static int _FOVReduction_RadiusUniform = -1;
	public static int _FOVReduction_BorderUniform = -1;
	public static int _FOVReduction_TextureUniform= -1;
	public static int _FOVReduction_shaderProgramId = -1;
	
	public static int _Overlay_HealthAlpha = -1;
	public static int _Overlay_waterAmplitude= -1;
	public static int _Overlay_portalAmplitutde= -1;
	public static int _Overlay_pumpkinAmplitutde= -1;
	public static int _Overlay_time= -1;
	public static int _Overlay_BlackAlpha = -1;
	public static int _Overlay_eye= -1;
	
	
	private VRShaders() {
	}

	public static String load(String name){
		InputStream is = VRShaders.class.getResourceAsStream("/assets/vivecraft/shaders/" + name);
		String out = "";
		try {
			if(is==null){
				//uhh debugging?
				Path dir = Paths.get(System.getProperty("user.dir")); // ..\mcpxxx\jars\
				Path p5 = dir.getParent().resolve("src/assets/shaders/" + name);
				if (!p5.toFile().exists()) {
					p5 = dir.getParent().getParent().resolve("assets/vivecraft/shaders/" + name);
				}
				is = new FileInputStream(p5.toFile());
			}
			InputStreamReader in = new InputStreamReader(is);
			out =IOUtils.toString(in);
			if(out == null){
				System.out.println("Cannot load shader: " + name);
				out = "";
			}
			in.close();
		} catch (Exception e) {
		}
		return out;
	}

	public static final String PASSTHRU_VERTEX_SHADER = load("passthru.vsh");
	public static final String DEPTH_MASK_FRAGMENT_SHADER = load("mixedreality.fsh");
	public static final String LANCZOS_SAMPLER_VERTEX_SHADER= load("lanczos.vsh");
	public static final String LANCZOS_SAMPLER_FRAGMENT_SHADER= load("lanczos.fsh");
	public static final String FOV_REDUCTION_FRAGMENT_SHADER= load("fovreduction.fsh");
	
	public static void setupDepthMask() throws Exception{
		_DepthMask_shaderProgramId = ShaderHelper.initShaders(VRShaders.PASSTHRU_VERTEX_SHADER, VRShaders.DEPTH_MASK_FRAGMENT_SHADER, true);
		
		if (_DepthMask_shaderProgramId == 0) {
			throw new Exception("Failed to validate depth mask shader!");
		}
		
		// Setup uniform IDs
		_DepthMask_resolutionUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "resolution");
		_DepthMask_positionUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "position");
		_DepthMask_colorTexUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "colorTex");
		_DepthMask_depthTexUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "depthTex");
		_DepthMask_hmdViewPosition = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "hmdViewPosition");
		_DepthMask_hmdPlaneNormal = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "hmdPlaneNormal");
		_DepthMask_projectionMatrix = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "projectionMatrix");
		_DepthMask_viewMatrix = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "viewMatrix");
		_DepthMask_passUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "pass");
		_DepthMask_keyColorUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "keyColor");
		_DepthMask_alphaModeUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "alphaMode");
		
	}
	
	public static void setupFSAA() throws Exception{
		_Lanczos_shaderProgramId = ShaderHelper.initShaders(VRShaders.LANCZOS_SAMPLER_VERTEX_SHADER, VRShaders.LANCZOS_SAMPLER_FRAGMENT_SHADER, true);
		if (_Lanczos_shaderProgramId == 0) {
			throw new Exception("Failed to validate FSAA shader!");
		}

		// Setup uniform IDs
		_LanczosShader_texelWidthOffsetUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelWidthOffset");
		_LanczosShader_texelHeightOffsetUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelHeightOffset");
		_LanczosShader_inputImageTextureUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "inputImageTexture");

	}
	
	public static void setupFOVReduction() throws Exception{
		
		_FOVReduction_shaderProgramId = ShaderHelper.initShaders(VRShaders.PASSTHRU_VERTEX_SHADER, VRShaders.FOV_REDUCTION_FRAGMENT_SHADER, true);
		if (_FOVReduction_shaderProgramId == 0) {
			throw new Exception("Failed to validate FOV shader!");
		}
		// Setup uniform IDs
		_FOVReduction_RadiusUniform = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "circle_radius");
		_FOVReduction_BorderUniform = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "border");
		_FOVReduction_TextureUniform = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "tex0");

		_Overlay_HealthAlpha = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "redalpha");
		_Overlay_waterAmplitude= ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "water");
		_Overlay_portalAmplitutde= ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "portal");
		_Overlay_pumpkinAmplitutde= ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "pumpkin");
		_Overlay_eye= ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "eye");
		_Overlay_time= ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "portaltime");
		_Overlay_BlackAlpha = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "blackalpha");
		
	}
	
}

