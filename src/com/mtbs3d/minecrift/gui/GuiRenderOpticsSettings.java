/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.api.ErrorHelper;
import com.mtbs3d.minecrift.gui.framework.*;
import com.mtbs3d.minecrift.provider.MCOculus;
import com.mtbs3d.minecrift.provider.MCOpenVR;
import com.mtbs3d.minecrift.settings.VRSettings;
import com.mtbs3d.minecrift.settings.VRSettings.VrOptions;

import de.fruitfly.ovr.structs.HmdParameters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

import java.util.List;

import org.lwjgl.util.Color;

public class GuiRenderOpticsSettings  extends BaseGuiSettings implements GuiEventEx
{
    protected boolean reinit = false;

    static VRSettings.VrOptions[] monoDisplayOptions = new VRSettings.VrOptions[] {
            //VRSettings.VrOptions.USE_ORTHO_GUI,
            VRSettings.VrOptions.MONO_FOV,
            VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.FSAA,
    };

    static VRSettings.VrOptions[] openVRDisplayOptions = new VRSettings.VrOptions[] {
            //VRSettings.VrOptions.HMD_NAME_PLACEHOLDER,
            //VRSettings.VrOptions.DUMMY,
            VRSettings.VrOptions.RENDER_SCALEFACTOR,
            VRSettings.VrOptions.MIRROR_DISPLAY,     
            VRSettings.VrOptions.FSAA,
            VRSettings.VrOptions.MIXED_REALITY_KEY_COLOR,
            VRSettings.VrOptions.STENCIL_ON,
            VRSettings.VrOptions.MIXED_REALITY_RENDER_HANDS,
            VRSettings.VrOptions.INSIDE_BLOCK_SOLID_COLOR,
            VRSettings.VrOptions.MONO_FOV,
            /*VRSettings.VrOptions.WORLD_SCALE,
            VRSettings.VrOptions.TIMEWARP,
            VRSettings.VrOptions.TIMEWARP_JIT_DELAY,
            VRSettings.VrOptions.VIGNETTE,
            VRSettings.VrOptions.HIGH_QUALITY_DISTORTION,
            VRSettings.VrOptions.OTHER_RENDER_SETTINGS,*/
    };

    GameSettings settings;
    VRSettings vrSettings;
    Minecraft mc;
    GuiSelectOption selectOption;

    public GuiRenderOpticsSettings(GuiScreen par1GuiScreen, VRSettings par2vrSettings, GameSettings gameSettings)
    {
    	super( par1GuiScreen, par2vrSettings);
        screenTitle = "Stereo Renderer Settings";
        settings = gameSettings;
        this.vrSettings = par2vrSettings;
        this.mc = Minecraft.getMinecraft();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        String productName = "";

        // this.screenTitle = var1.translateKey("options.videoTitle");
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DEFAULTS, this.width / 2 - 155 ,  this.height -25 ,150,20, "Reset To Defaults"));
        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DONE, this.width / 2 - 155  + 160, this.height -25,150,20, "Done"));
        VRSettings.VrOptions[] var10 = null;
        
        if( Minecraft.getMinecraft().stereoProvider.isStereo() )
        {
            productName = "OpenVR";
            var10 = openVRDisplayOptions;
        }
        else
            var10 = monoDisplayOptions;

        int var11 = var10.length;

        for (int var12 = 2; var12 < var11 + 2; ++var12)
        {
            VRSettings.VrOptions var8 = var10[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10;

            if (var8 == VRSettings.VrOptions.DUMMY)
                continue;

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.001f;

                if (var8 == VRSettings.VrOptions.RENDER_SCALEFACTOR)
                {
                    minValue = 0.5f;
                    maxValue = 4.0f;
                    increment = 0.1f;
                }
                else if (var8 == VRSettings.VrOptions.MONO_FOV)
                {
                    minValue = 1f;
                    maxValue = 179f;
                    increment = 1f;
                }
                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guivrSettings.getOptionFloatValue(var8));
                slider.setEventHandler(this);
                slider.enabled = getEnabledState(var8);
                this.buttonList.add(slider);
            }
            else
            {
                if (false)
                {
                    GuiSmallButtonEx button = new GuiSmallButtonEx(9999, width, height, var8, productName);
                    button.enabled = false;
                    this.buttonList.add(button);
                }
                else
                {
                    String keyBinding = this.guivrSettings.getKeyBinding(var8);
                    GuiSmallButtonEx button = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, keyBinding);
                    button.enabled = getEnabledState(var8);
                    this.buttonList.add(button);
                }
            }
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        VRSettings.VrOptions num = VRSettings.VrOptions.getEnumOptions(par1GuiButton.id);
        Minecraft minecraft = Minecraft.getMinecraft();

        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id == ID_GENERIC_DONE)
            {
                minecraft.vrSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == ID_GENERIC_DEFAULTS)
            {

                minecraft.vrSettings.renderScaleFactor = 1.0f;
                minecraft.vrSettings.displayMirrorMode = VRSettings.MIRROR_ON_ONE_THIRD_FRAME_RATE;
                minecraft.vrSettings.mixedRealityKeyColor = new Color();
                minecraft.vrSettings.mixedRealityRenderHands = false;
                minecraft.vrSettings.insideBlockSolidColor = false;
                minecraft.gameSettings.fovSetting = 70f;
                minecraft.vrSettings.useFsaa = false;
                minecraft.vrSettings.vrUseStencil = true;
                minecraft.reinitFramebuffers = true;
			    this.guivrSettings.saveOptions();
            }
            else if (par1GuiButton.id == ID_GENERIC_MODE_CHANGE) // Mode Change
            {
                Minecraft.getMinecraft().vrSettings.saveOptions();
               // selectOption = new GuiSelectOption(this, this.guivrSettings, "Select StereoProvider", "Select the render provider:", pluginModeChangeButton.getPluginNames());
                this.mc.displayGuiScreen(selectOption);
            }
            else if (par1GuiButton.id == VRSettings.VrOptions.OTHER_RENDER_SETTINGS.returnEnumOrdinal())
            {
                Minecraft.getMinecraft().vrSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiOtherRenderOpticsSettings(this, this.guivrSettings));
            }
            else if (par1GuiButton instanceof GuiSmallButtonEx)
            {
                this.guivrSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnVrEnumOptions(), 1);
                par1GuiButton.displayString = this.guivrSettings.getKeyBinding(VRSettings.VrOptions.getEnumOptions(par1GuiButton.id));
            }

            if (num == VRSettings.VrOptions.MIRROR_DISPLAY ||
                num == VRSettings.VrOptions.FSAA)
	        {
                minecraft.reinitFramebuffers = true;
	        }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        if (reinit)
        {
            initGui();
            reinit = false;
        }
        super.drawScreen(par1,par2,par3);
    }

    @Override
    public boolean event(int id, VRSettings.VrOptions enumm)
    {
        boolean ret = false;

        if (     enumm == VRSettings.VrOptions.RENDER_SCALEFACTOR)
        {
            Minecraft.getMinecraft().reinitFramebuffers = true;
            ret = true;
        }

        return ret;
    }

    @Override
    public boolean event(int id, String s)
    {
        boolean success = true;
        String title = null;
        String error = null;

        if (id == GuiSelectOption.ID_OPTION_SELECTED)
        {
//            String origId = pluginModeChangeButton.getSelectedID();
//
//            try {
//                pluginModeChangeButton.setPluginByName(s);
//                vrSettings.stereoProviderPluginID = pluginModeChangeButton.getSelectedID();
//                mc.stereoProvider = PluginManager.configureStereoProvider(vrSettings.stereoProviderPluginID, true);
//                vrSettings.badStereoProviderPluginID = "";
//                vrSettings.saveOptions();
//                mc.reinitFramebuffers = true;
//                this.reinit = true;
//            }
//            catch (Throwable e) {
//                e.printStackTrace();
//                error = e.getClass().getName() + ": " + e.getMessage();
//                title = "Failed to initialise stereo provider: " + pluginModeChangeButton.getSelectedName();
//                mc.errorHelper = new ErrorHelper(title, error, "Reverted to previous renderer!", mc.ERROR_DISPLAY_TIME_SECS);
//                success = false;
//            }
//
//            if (!success) {
//                pluginModeChangeButton.setPluginByID(origId);
//                vrSettings.stereoProviderPluginID = pluginModeChangeButton.getSelectedID();
//                try {
//                    mc.stereoProvider = PluginManager.configureStereoProvider(vrSettings.stereoProviderPluginID);
//                }
//                catch (Exception ex) {}
//            }
        }

        return success;
    }

    @Override
    protected String[] getTooltipLines(String displayString, int buttonId)
    {
        VRSettings.VrOptions e = VRSettings.VrOptions.getEnumOptions(buttonId);
    	if( e != null )
    	switch(e)
    	{
	        case FSAA:
	            return new String[] {
	                    "Uses a fancier method of resampling the",
	                    "game before sending it to the HMD. Works best",
	                    "at high render scales. "};

            case RENDER_SCALEFACTOR:
                return new String[] {
                        "The internal rendering scale of the game, relative",
                        "to the native HMD display. Higher values improve visual",
                        "quality, espeically with FSAA on, at the cost of performance"
                };
            case MIRROR_DISPLAY:
                return new String[] {
                        "Mirrors image on HMD to separate desktop window.",
                        "Can be set to OFF, single or dual view at 1/3 or",
                        "full framerate."
                };
            case MIXED_REALITY_KEY_COLOR:
                return new String[] {
                        "The color drawn to the \"transparent\" areas of the",
                        "mixed reality view. Other colors in-game will be",
                        "prevented from matching this so it doesn't cause",
                        "weirdness."
                };
            case MIXED_REALITY_RENDER_HANDS:
                return new String[] {
                        "Render hands on the mixed reality view. Only",
                        "toggles rendering of the actual hand models, items",
                        "will still be rendered."
                };
            case INSIDE_BLOCK_SOLID_COLOR:
                return new String[] {
                        "Whether to render the block texture or a solid",
                        "color when eye is inside of a block."
                };
            case MONO_FOV:
                return new String[] {
                        "The FOV used for the mixed reality and",
                        "undistorted mirror modes."
                };

            case OTHER_RENDER_SETTINGS:
                return new String[] {
                        "Configure IPD and FOV border settings."
                };
            case STENCIL_ON:
                return new String[] {
                        "Mask out areas of the screen outside the FOV.",
                        "Improves performance."
                };
    	default:
    		return null;
    	}
    	else
    	switch(buttonId)
    	{
	    	case ID_GENERIC_DEFAULTS:
	    		return new String[] {
	    			"Resets all values on this screen to their defaults"
	    		};
    		default:
    			return null;
    	}
    }

    private boolean getEnabledState(VRSettings.VrOptions var8)
    {
        String s = var8.getEnumString();

        return true;
    }
}

