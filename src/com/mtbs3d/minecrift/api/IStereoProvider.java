/**
 * Copyright 2014 StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.api;


import com.mtbs3d.minecrift.render.RenderConfigException;

import de.fruitfly.ovr.enums.EyeType;
import de.fruitfly.ovr.structs.*;
import net.minecraft.client.Minecraft.renderPass;
import net.minecraft.client.gui.GuiScreen;

/**
 * Implement this to provide Minecrift with stereo rendering services
 *
 * @author StellaArtois
 *
 */
public interface IStereoProvider 
{

    public boolean providesMirrorTexture();

    public int createMirrorTexture(int width, int height);

    public void deleteMirrorTexture();

    public boolean providesRenderTextures();

    public boolean providesStencilMask();
    
    public float[] getStencilMask(renderPass currentPass);
    
    public RenderTextureSet createRenderTexture(int width, int height);

    public void deleteRenderTextures();

    public EyeType eyeRenderOrder(int index);

    public boolean usesDistortion();

    public boolean isStereo();

    public double getFrameTiming();

    public Matrix4f getProjectionMatrix(FovPort fov,
                                        int i,  // VIVE added eyeType
                                        float nearClip,
                                        float farClip);

    public double getCurrentTimeSecs();

	public boolean setCurrentRenderTextureInfo(int index, int textureIdx, int depthId, int depthWidth, int depthHeight);

    public String getLastError();

    public void configureRenderer(GLConfig cfg);

    // VIVE START - new stereo provider functions
    public void onGuiScreenChanged(GuiScreen previousScreen, GuiScreen newScreen);
    // VIVE END - new stereo provider functions

	boolean endFrame(renderPass currentPass);

	RenderTextureInfo getRenderTextureSizes(float renderScaleFactor);

	public void endFrame() throws RenderConfigException;

	public String getName();

	public boolean isInitialized();
	
	public String getinitError();
}