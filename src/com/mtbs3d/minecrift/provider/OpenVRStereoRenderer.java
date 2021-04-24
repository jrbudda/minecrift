package com.mtbs3d.minecrift.provider;

import com.mtbs3d.minecrift.api.IStereoProvider;
import com.mtbs3d.minecrift.render.RenderConfigException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import de.fruitfly.ovr.enums.EyeType;
import de.fruitfly.ovr.structs.*;
import jopenvr.HiddenAreaMesh_t;
import jopenvr.HmdMatrix44_t;
import jopenvr.JOpenVRLibrary;
import jopenvr.OpenVRUtil;
import jopenvr.Texture_t;
import jopenvr.VRTextureBounds_t;
import jopenvr.JOpenVRLibrary.EVRCompositorError;
import net.minecraft.client.Minecraft.renderPass;
import net.minecraft.client.gui.GuiScreen;

import java.nio.IntBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
/**
 * Created by jrbudda
 */
public class OpenVRStereoRenderer implements IStereoProvider
{
	// TextureIDs of framebuffers for each eye
	private int LeftEyeTextureId, RightEyeTextureId;

	private HiddenAreaMesh_t[] hiddenMeshes = new HiddenAreaMesh_t[2];
	private float[][] hiddenMesheVertecies = new float[2][];

	@Override
	public RenderTextureInfo getRenderTextureSizes(float renderScaleFactor)
	{
		IntByReference rtx = new IntByReference();
		IntByReference rty = new IntByReference();
		MCOpenVR.vrsystem.GetRecommendedRenderTargetSize.apply(rtx, rty);

		RenderTextureInfo info = new RenderTextureInfo();
		info.HmdNativeResolution.w = rtx.getValue();
		info.HmdNativeResolution.h = rty.getValue();
		info.LeftFovTextureResolution.w = (int) (rtx.getValue());
		info.LeftFovTextureResolution.h = (int) (rty.getValue());
		info.RightFovTextureResolution.w = (int) (rtx.getValue());
		info.RightFovTextureResolution.h = (int) (rty.getValue());
		if ( info.LeftFovTextureResolution.w % 2 != 0) info.LeftFovTextureResolution.w++;
		if ( info.LeftFovTextureResolution.h % 2 != 0) info.LeftFovTextureResolution.w++;
		if ( info.RightFovTextureResolution.w % 2 != 0) info.LeftFovTextureResolution.w++;
		if ( info.RightFovTextureResolution.h % 2 != 0) info.LeftFovTextureResolution.w++;

		info.CombinedTextureResolution.w = info.LeftFovTextureResolution.w + info.RightFovTextureResolution.w;
		info.CombinedTextureResolution.h = info.LeftFovTextureResolution.h;


		for (int i = 0; i < 2; i++) {
			hiddenMeshes[i] = MCOpenVR.vrsystem.GetHiddenAreaMesh.apply(i,0);
			hiddenMeshes[i].read();
			int tc = hiddenMeshes[i].unTriangleCount;
			if(tc >0){
				hiddenMesheVertecies[i] = new float[hiddenMeshes[i].unTriangleCount * 3 * 2];
				Pointer arrptr = new Memory(hiddenMeshes[i].unTriangleCount * 3 * 2);
				hiddenMeshes[i].pVertexData.getPointer().read(0, hiddenMesheVertecies[i], 0, hiddenMesheVertecies[i].length);
	
				for (int ix = 0;ix < hiddenMesheVertecies[i].length;ix+=2) {
					hiddenMesheVertecies[i][ix] = hiddenMesheVertecies[i][ix] * info.LeftFovTextureResolution.w;
					hiddenMesheVertecies[i][ix + 1] = hiddenMesheVertecies[i][ix +1] * info.LeftFovTextureResolution.h;
				}
				System.out.println("Stencil mesh loaded for eye " + i);
			} else {
				System.out.println("No stencil mesh found for eye " + i);
			}
		}

		return info;
	}

	@Override
	public Matrix4f getProjectionMatrix(FovPort fov,
			int eyeType,
			float nearClip,
			float farClip)
	{
		if ( eyeType == 0 )
		{
			HmdMatrix44_t mat = MCOpenVR.vrsystem.GetProjectionMatrix.apply(JOpenVRLibrary.EVREye.EVREye_Eye_Left, nearClip, farClip);
			MCOpenVR.hmdProjectionLeftEye = new Matrix4f();
			return OpenVRUtil.convertSteamVRMatrix4ToMatrix4f(mat, MCOpenVR.hmdProjectionLeftEye);
		}else{
			HmdMatrix44_t mat = MCOpenVR.vrsystem.GetProjectionMatrix.apply(JOpenVRLibrary.EVREye.EVREye_Eye_Right, nearClip, farClip);
			MCOpenVR.hmdProjectionRightEye = new Matrix4f();
			return OpenVRUtil.convertSteamVRMatrix4ToMatrix4f(mat, MCOpenVR.hmdProjectionRightEye);
		}
	}


	@Override
	public EyeType eyeRenderOrder(int index)
	{
		return ( index == 1 ) ? EyeType.ovrEye_Right : EyeType.ovrEye_Left;
	}

	@Override
	public boolean usesDistortion()
	{
		return true;
	}

	@Override
	public boolean isStereo()
	{
		return true;
	}

	@Override
	public double getFrameTiming() {
		return getCurrentTimeSecs();
	}

	@Override
	public void deleteRenderTextures() {
		if (LeftEyeTextureId > 0)	GL11.glDeleteTextures(LeftEyeTextureId);
		if (RightEyeTextureId > 0)	GL11.glDeleteTextures(LeftEyeTextureId);
	}

	@Override
	public String getLastError() { return ""; }

	@Override
	public boolean setCurrentRenderTextureInfo(int index, int textureIdx, int depthId, int depthWidth, int depthHeight)
	{
		return true;
	}
	
	@Override
	public double getCurrentTimeSecs()
	{
		return System.nanoTime() / 1000000000d;
	}


	@Override
	public boolean providesMirrorTexture() { return false; }

	@Override
	public int createMirrorTexture(int width, int height) { return -1; }

	@Override
	public void deleteMirrorTexture() { }

	@Override
	public boolean providesRenderTextures() { return true; }

	@Override
	public RenderTextureSet createRenderTexture(int lwidth, int lheight)
	{	
		// generate left eye texture
		LeftEyeTextureId = GL11.glGenTextures();
		int boundTextureId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, LeftEyeTextureId);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, boundTextureId);

		MCOpenVR.texType0.handle= Pointer.createConstant(LeftEyeTextureId);
		MCOpenVR.texType0.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		MCOpenVR.texType0.eType = JOpenVRLibrary.ETextureType.ETextureType_TextureType_OpenGL;
		MCOpenVR.texType0.write();
		
		// generate right eye texture
		RightEyeTextureId = GL11.glGenTextures();
		boundTextureId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, RightEyeTextureId);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, boundTextureId);

		MCOpenVR.texType1.handle=Pointer.createConstant(RightEyeTextureId);
		MCOpenVR.texType1.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		MCOpenVR.texType1.eType = JOpenVRLibrary.ETextureType.ETextureType_TextureType_OpenGL;
		MCOpenVR.texType1.write();

		RenderTextureSet textureSet = new RenderTextureSet();
		textureSet.leftEyeTextureIds.add(LeftEyeTextureId);
		textureSet.rightEyeTextureIds.add(RightEyeTextureId);
		return textureSet;
	}

	@Override
	public void configureRenderer(GLConfig cfg) {

	}

	public void onGuiScreenChanged(GuiScreen previousScreen, GuiScreen newScreen) {
		MCOpenVR.onGuiScreenChanged(previousScreen, newScreen);
	}

	@Override
	public boolean endFrame(renderPass eye)
	{
		return true;
	}

	
	public void endFrame() throws RenderConfigException {
		if(MCOpenVR.vrCompositor.Submit == null) return;
		
		int lret = MCOpenVR.vrCompositor.Submit.apply(
				JOpenVRLibrary.EVREye.EVREye_Eye_Left,
				MCOpenVR.texType0, null,
				JOpenVRLibrary.EVRSubmitFlags.EVRSubmitFlags_Submit_Default);

		int rret = MCOpenVR.vrCompositor.Submit.apply(
				JOpenVRLibrary.EVREye.EVREye_Eye_Right,
				MCOpenVR.texType1, null,
				JOpenVRLibrary.EVRSubmitFlags.EVRSubmitFlags_Submit_Default);

		if(lret + rret > 0){
			throw new RenderConfigException("Compositor Error","Texture submission error: Left/Right " + getCompostiorError(lret) + "/" + getCompostiorError(rret));		
		}

		MCOpenVR.vrCompositor.PostPresentHandoff.apply();
	}

	
	public static String getCompostiorError(int code){
		switch (code){
		case EVRCompositorError.EVRCompositorError_VRCompositorError_DoNotHaveFocus:
			return "DoesNotHaveFocus";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_IncompatibleVersion:
			return "IncompatibleVersion";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_IndexOutOfRange:
			return "IndexOutOfRange";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_InvalidTexture:
			return "InvalidTexture";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_IsNotSceneApplication:
			return "IsNotSceneApplication";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_RequestFailed:
			return "RequestFailed";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_SharedTexturesNotSupported:
			return "SharedTexturesNotSupported";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_TextureIsOnWrongDevice:
			return "TextureIsOnWrongDevice";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_TextureUsesUnsupportedFormat:
			return "TextureUsesUnsupportedFormat:";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_None:
			return "None:";
		case EVRCompositorError.EVRCompositorError_VRCompositorError_AlreadySubmitted:
			return "AlreadySubmitted:";
		}
		return "Unknown";
	}
	
	@Override
	public boolean providesStencilMask() {
		return true;
	}

	@Override
	public float[] getStencilMask(renderPass eye) {
		if(hiddenMesheVertecies == null || eye == renderPass.Center || eye == renderPass.Third) return null;
		return eye == renderPass.Left? hiddenMesheVertecies[0] : hiddenMesheVertecies[1];
	}

	@Override
	public String getName() {
		return "OpenVR";
	}

	@Override
	public boolean isInitialized() {
		return MCOpenVR.initSuccess;
	}

	@Override
	public String getinitError() {
		return MCOpenVR.initStatus;
	}

}
