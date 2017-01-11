/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.settings;

import java.io.*;
import java.util.ArrayList;
import java.util.SortedSet;

import com.mtbs3d.minecrift.provider.MCOpenVR;
import com.mtbs3d.minecrift.settings.profile.ProfileReader;
import com.mtbs3d.minecrift.control.VRControllerButtonMapping;
import com.mtbs3d.minecrift.control.ViveButtons;
import com.mtbs3d.minecrift.settings.profile.ProfileManager;
import com.mtbs3d.minecrift.settings.profile.ProfileWriter;
import com.mtbs3d.minecrift.utils.KeyboardSimulator;

import de.fruitfly.ovr.IOculusRift;
import de.fruitfly.ovr.enums.EyeType;
import jopenvr.VR_IVRSystem_FnTable.GetTrackedDeviceIndexForControllerRole_callback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.lwjgl.util.Color;

public class VRSettings
{
    public static final int VERSION = 2;
    public static final Logger logger = LogManager.getLogger();
	public static VRSettings inst;
	public JSONObject defaults = new JSONObject();
    public static final int UNKNOWN_VERSION = 0;
    public final String DEGREE  = "\u00b0";

    public static final int INERTIA_NONE = 0;
    public static final int INERTIA_NORMAL = 1;
    public static final int INERTIA_LARGE = 2;
    public static final int INERTIA_MASSIVE = 3;

    public static final float INERTIA_NONE_ADD_FACTOR = 1f / 0.01f;
    public static final float INERTIA_NORMAL_ADD_FACTOR = 1f;
    public static final float INERTIA_LARGE_ADD_FACTOR = 1f / 4f;
    public static final float INERTIA_MASSIVE_ADD_FACTOR = 1f / 16f;
    public static final int RENDER_FIRST_PERSON_FULL = 0;
    public static final int RENDER_FIRST_PERSON_HAND = 1;
    public static final int RENDER_FIRST_PERSON_NONE = 2;
    public static final int RENDER_CROSSHAIR_MODE_ALWAYS = 0;
    public static final int RENDER_CROSSHAIR_MODE_HUD = 1;
    public static final int RENDER_CROSSHAIR_MODE_NEVER = 2;
    public static final int RENDER_BLOCK_OUTLINE_MODE_ALWAYS = 0;
    public static final int RENDER_BLOCK_OUTLINE_MODE_HUD = 1;
    public static final int RENDER_BLOCK_OUTLINE_MODE_NEVER = 2;
  
    public static final int MIRROR_OFF = 0;
    public static final int MIRROR_ON_ONE_THIRD_FRAME_RATE = 1;
    public static final int MIRROR_ON_FULL_FRAME_RATE = 2;
    public static final int MIRROR_ON_ONE_THIRD_FRAME_RATE_SINGLE_VIEW = 3;
    public static final int MIRROR_ON_FULL_FRAME_RATE_SINGLE_VIEW = 4;
    public static final int MIRROR_MIXED_REALITY = 5;
    public static final int MIRROR_FIRST_PERSON = 6;
    
    public static final int HUD_LOCK_HEAD= 1;
    public static final int HUD_LOCK_HAND= 2;
    public static final int HUD_LOCK_WRIST= 3;
    public static final int HUD_LOCK_BODY= 4;

    public static final int FREEMOVE_CONTROLLER= 1;
    public static final int FREEMOVE_HMD= 2;
    public static final int FREEMOVE_RUNINPLACE= 3;
    
    public static final int MAINMENU_JRBUDDA = 0;
    public static final int MAINMENU_TECHJAR = 1;
    
    public static final int NO_SHADER = -1;

    public int version = UNKNOWN_VERSION;

    public float movementSpeedMultiplier = 1.0f;   // VIVE - use full speed by default
    public float hudOpacity = 0.95f;
    public boolean menuBackground = false;
    public int renderFullFirstPersonModelMode = RENDER_FIRST_PERSON_HAND;   // VIVE - hand only by default
    public int shaderIndex = NO_SHADER;
    public float  renderScaleFactor = 1.0f;
    public int displayMirrorMode = MIRROR_ON_FULL_FRAME_RATE_SINGLE_VIEW;
    public boolean walkUpBlocks = true;     // VIVE default to enable climbing
    public float   menuCrosshairScale = 1f;
    public boolean useCrosshairOcclusion = false;
    public boolean simulateFalling = false;  // VIVE if HMD is over empty space, fall
    public boolean weaponCollision = true;  // VIVE weapon hand collides with blocks/enemies
    public float hudScale = 1.5f;
    public float hudDistance = 1.25f;
    public float hudPitchOffset = -2f;
    public float hudYawOffset = 0.0f;
    public boolean floatInventory = true; //false not working yet, have to account for rotation and tilt in MCOpenVR>processGui()
    public boolean useFsaa = false;   // default to off
    public boolean useFOVReduction = false;   // default to off
    public String stereoProviderPluginID = "openvr";
    public String badStereoProviderPluginID = "";
    public float crosshairScale = 1.0f;
    public int renderInGameCrosshairMode = RENDER_CROSSHAIR_MODE_ALWAYS;
    public int renderBlockOutlineMode = RENDER_BLOCK_OUTLINE_MODE_ALWAYS;
    public boolean hudOcclusion = false;
    public int inertiaFactor = INERTIA_NORMAL;
    public boolean storeDebugAim = false;
    public int smoothRunTickCount = 20;
    public boolean smoothTick = false;
    public boolean hideGui = false;     // VIVE show gui
    //Jrbudda's Options
    public boolean vrFreeMove = false;
    public boolean vrAllowLocoModeSwotch = true;
    public boolean vrLimitedSurvivalTeleport = true;
    public boolean vrAllowCrawling = false;
    public boolean vrReverseHands = false;
    public boolean vrReverseShootingEye = false;
    public VRControllerButtonMapping[] buttonMappings;
    public boolean vrUseStencil = true;
    public boolean vrShowBlueCircleBuddy = true;
    public float vrWorldScale = 1.0f;
    public float vrWorldRotation = 0f;
    public float vrWorldRotationCached = 0f;
    public float vrWorldRotationIncrement = 45f;
    public String[] vrQuickCommands;
    public float vrFixedCamposX = 0;
    public float vrFixedCamposY = 0;
    public float vrFixedCamposZ = 0;
    public float vrFixedCamrotYaw = 0;
    public float vrFixedCamrotPitch = 0;
    public float vrFixedCamrotRoll = 0;
    public int vrHudLockMode = HUD_LOCK_HAND;
    public Color mixedRealityKeyColor = new Color();
    public float mixedRealityAspectRatio = 16F / 9F;
    public boolean mixedRealityRenderHands = false;
    public boolean insideBlockSolidColor = false;
    public boolean vrTouchHotbar = true;
    public boolean seated = false;
    private Minecraft mc;
    public float jumpThreshold=0.05f;
    public float sneakThreshold=0.4f;
    public boolean realisticJumpEnabled=true;
    public boolean realisticSneakEnabled=true;
    public boolean realisticClimbEnabled=true;
    public boolean realisticSwimEnabled=true;
    public boolean realisticRowEnabled=true;
    public float walkMultiplier=1;
    public int vrFreeMoveMode = this.FREEMOVE_CONTROLLER;
    public int menuRoom = 1;

    public double headToHmdLength=0.10f;

    public float xSensitivity=1f;
    public float ySensitivity=1f; 
    public float keyholeX=15;

    public float autoCalibration=-1;
    public float manualCalibration=-1;
    public float playerEyeHeight = 1.62f;
    public boolean vehicleRotation = false;
	public boolean loadMumbleLib;
    
    public VRSettings( Minecraft minecraft, File dataDir )
    {
        // Assumes GameSettings (and hence optifine's settings) have been read first

    	mc = minecraft;
    	inst = this;

        // Store our class defaults to a member variable for later use
    	storeDefaults();

        // Legacy config files. Note that in general these files will be by-passed
        // by the Profile handling in ProfileManager. loadOptions and saveOptions ill
        // be redirected to the profile manager using ProfileReader and ProfileWriter
        // respectively.

        // Load settings from the file
        this.loadOptions();
    }

    public void loadOptions()
    {
        loadOptions(null);
    }

    public void loadDefaults()
    {
        loadOptions(this.defaults);
    }
    
    public void loadOptions(JSONObject theProfiles)
    {
        // Load Minecrift options
        try
        {
            ProfileReader optionsVRReader = new ProfileReader(ProfileManager.PROFILE_SET_VR, theProfiles);

            String var2 = "";
           
            while ((var2 = optionsVRReader.readLine()) != null)
            {
                try
                {
                    String[] optionTokens = var2.split(":");

                    if (optionTokens[0].equals("version"))
                    {
                        this.version = Integer.parseInt(optionTokens[1]);
                    }

//                    if (optionTokens[0].equals("firstLoad"))
//                    {
//                        this.firstLoad = optionTokens[1].equals("true");
//                    }

                    if (optionTokens[0].equals("stereoProviderPluginID"))
                    {
                        this.stereoProviderPluginID = optionTokens[1];
                    }

                    if (optionTokens[0].equals("badStereoProviderPluginID"))
                    {
                        if (optionTokens.length > 1) {  // Trap if no entry
                            this.badStereoProviderPluginID = optionTokens[1];
                        }
                    }

                    if (optionTokens[0].equals("hudOpacity"))
                    {
                        this.hudOpacity = this.parseFloat(optionTokens[1]);
                        if(hudOpacity< 0.15f)
                        	hudOpacity = 1.0f;
                    }
                    if (optionTokens[0].equals("menuBackground"))
                    {
                        this.menuBackground = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("renderFullFirstPersonModelMode"))
                    {
                        this.renderFullFirstPersonModelMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("shaderIndex"))
                    {
                        this.shaderIndex = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("walkUpBlocks"))
                    {
                        this.walkUpBlocks = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("displayMirrorMode"))
                    {
                        this.displayMirrorMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("mixedRealityKeyColor"))
                    {
                        String[] split = optionTokens[1].split(",");
                        this.mixedRealityKeyColor = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                    }

                    if (optionTokens[0].equals("mixedRealityRenderHands"))
                    {
                        this.mixedRealityRenderHands = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("insideBlockSolidColor"))
                    {
                        this.insideBlockSolidColor = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("mainMenuRoom"))
                    {
                        this.menuRoom = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("hudScale"))
                    {
                        this.hudScale = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderScaleFactor"))
                    {
                        this.renderScaleFactor = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("vrHudLockMode"))
                    {
                        this.vrHudLockMode =  Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("hudDistance"))
                    {
                        this.hudDistance = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("hudPitchOffset"))
                    {
                        this.hudPitchOffset = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("hudYawOffset"))
                    {
                        this.hudYawOffset = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("useFsaa"))
                    {
                        this.useFsaa = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("movementSpeedMultiplier"))
                    {
                        this.movementSpeedMultiplier = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderInGameCrosshairMode"))
                    {
                        this.renderInGameCrosshairMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderBlockOutlineMode"))
                    {
                        this.renderBlockOutlineMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("crosshairScale"))
                    {
                        this.crosshairScale = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("menuCrosshairScale"))
                    {
                        this.menuCrosshairScale = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderInGameCrosshairMode"))
                    {
                        this.renderInGameCrosshairMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderBlockOutlineMode"))
                    {
                        this.renderBlockOutlineMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("hudOcclusion"))
                    {
                        this.hudOcclusion = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useCrosshairOcclusion"))
                    {
                        this.useCrosshairOcclusion = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("inertiaFactor"))
                    {
                        this.inertiaFactor = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("smoothRunTickCount"))
                    {
                        this.smoothRunTickCount = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("smoothTick"))
                    {
                        this.smoothTick = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hideGui"))
                    {
                        this.hideGui = optionTokens[1].equals("true");
                    }

                    // VIVE START - new options
                    if (optionTokens[0].equals("simulateFalling"))
                    {
                        this.simulateFalling = optionTokens[1].equals("true");
                    }
                    if (optionTokens[0].equals("weaponCollision"))
                    {
                        this.weaponCollision = optionTokens[1].equals("true");
                    }
                    // VIVE END - new options
                    //JRBUDDA
                    if (optionTokens[0].equals("allowCrawling"))
                    {
                        this.vrAllowCrawling = optionTokens[1].equals("true");
                    }
                    if (optionTokens[0].equals("allowModeSwitch"))
                    {
                        this.vrAllowLocoModeSwotch = optionTokens[1].equals("true");
                    }
                    if (optionTokens[0].equals("freeMoveDefault"))
                    {
                        this.vrFreeMove = optionTokens[1].equals("true");
                    }
                    if (optionTokens[0].equals("limitedTeleport"))
                    {
                        this.vrLimitedSurvivalTeleport = optionTokens[1].equals("true");
                    }
                    if (optionTokens[0].equals("reverseHands"))
                    {
                        this.vrReverseHands = optionTokens[1].equals("true");
                    }
                    if (optionTokens[0].equals("stencilOn"))
                    {
                        this.vrUseStencil = optionTokens[1].equals("true");
                    }
                    if (optionTokens[0].equals("bcbOn"))
                    {
                        this.vrShowBlueCircleBuddy = optionTokens[1].equals("true");
                    }
                    if (optionTokens[0].equals("worldScale"))
                    {
                        this.vrWorldScale = this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("worldRotation"))
                    {
                        this.vrWorldRotation = this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("vrWorldRotationIncrement"))
                    {
                        this.vrWorldRotationIncrement =  this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("vrFixedCamposX"))
                    {
                        this.vrFixedCamposX =  this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("vrFixedCamposY"))
                    {
                        this.vrFixedCamposY =  this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("vrFixedCamposZ"))
                    {
                        this.vrFixedCamposZ =  this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("vrFixedCamrotPitch"))
                    {
                        this.vrFixedCamrotPitch =this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("vrFixedCamrotYaw"))
                    {
                        this.vrFixedCamrotYaw =this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("vrFixedCamrotRoll"))
                    {
                        this.vrFixedCamrotRoll =this.parseFloat(optionTokens[1]);
                    }
                    if (optionTokens[0].equals("vrTouchHotbar"))
                    {
                    	  this.vrTouchHotbar = optionTokens[1].equals("true");
                    }
                    if (optionTokens[0].equals("seated"))
                    {
                    	  this.seated = optionTokens[1].equals("true");
                    }

                    if(optionTokens[0].equals("jumpThreshold")){
                        this.jumpThreshold=this.parseFloat(optionTokens[1]);
                    }

                    if(optionTokens[0].equals("sneakThreshold")){
                        this.sneakThreshold=this.parseFloat(optionTokens[1]);
                    }

                    if(optionTokens[0].equals("realisticSneakEnabled")){
                        this.realisticSneakEnabled=optionTokens[1].equals("true");
                    }

                    if(optionTokens[0].equals("realisticJumpEnabled")){
                        this.realisticJumpEnabled=optionTokens[1].equals("true");
                    }
                    if(optionTokens[0].equals("realisticClimbEnabled")){
                        this.realisticClimbEnabled=optionTokens[1].equals("true");
                    }
                    if(optionTokens[0].equals("realisticSwimEnabled")){
                        this.realisticSwimEnabled=optionTokens[1].equals("true");
                    }
                    if(optionTokens[0].equals("realisticRowEnabled")){
                        this.realisticRowEnabled=optionTokens[1].equals("true");
                    }

                    if(optionTokens[0].equals("headToHmdLength")){
                        this.headToHmdLength=parseFloat(optionTokens[1]);
                    }

                    if(optionTokens[0].equals("walkMultiplier")){
                        this.walkMultiplier=parseFloat(optionTokens[1]);
                    }
                    
                    if (optionTokens[0].equals("vrFreeMoveMode"))
                    {
                        this.vrFreeMoveMode =  Integer.parseInt(optionTokens[1]);
                    }

                    if(optionTokens[0].equals("xSensitivity")){
                        this.xSensitivity=parseFloat(optionTokens[1]);
                    }

                    if(optionTokens[0].equals("ySensitivity")){
                        this.ySensitivity=parseFloat(optionTokens[1]);
                    }

                    if(optionTokens[0].equals("keyholeX")){
                        this.keyholeX=parseFloat(optionTokens[1]);
                    }

                    if(optionTokens[0].equals("autoCalibration")){
                        this.autoCalibration=parseFloat(optionTokens[1]);
                    }

                    if(optionTokens[0].equals("manualCalibration")){
                        this.manualCalibration=parseFloat(optionTokens[1]);
                    }
                    
                    if(optionTokens[0].equals("vehicleRotation")){
                        this.vehicleRotation=optionTokens[1].equals("true");
                    }
                    
                    if(optionTokens[0].equals("fovReduction")){
                        this.useFOVReduction=optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].startsWith("BUTTON_")  || optionTokens[0].startsWith("OCULUS_"))
                    {
                       VRControllerButtonMapping vb = new VRControllerButtonMapping(
                    		   Enum.valueOf(ViveButtons.class, optionTokens[0]),"");
                                               
                       String[] pts = optionTokens[1].split("_");
                      
                       if (pts.length == 1 || !optionTokens[1].startsWith("keyboard")) {
                           vb.FunctionDesc = optionTokens[1];
                           vb.FunctionExt = 0;
                       } else {
                           vb.FunctionDesc = pts[0];
                           vb.FunctionExt = (char) pts[1].getBytes()[0];
                       }
                                         
                       this.buttonMappings[vb.Button.ordinal()] = vb;
                    }       
                    if(optionTokens[0].startsWith("QUICKCOMMAND_")){
                    	 String[] pts = optionTokens[0].split("_");
                    	 int i = Integer.parseInt(pts[1]);
                    	 if (optionTokens.length == 1) 
                        	 vrQuickCommands[i] = "";
                    	 else
                        	 vrQuickCommands[i] = optionTokens[1];

                    }
                    
                    //END JRBUDDA
         
                }
                catch (Exception var7)
                {
                    logger.warn("Skipping bad VR option: " + var2);
                    var7.printStackTrace();
                }
            }           
            optionsVRReader.close();
        }
        catch (Exception var8)
        {
            logger.warn("Failed to load VR options!");
            var8.printStackTrace();
        }
    }

	public void processBindings() {
		//process button mappings     
		int offset = 0;
		for (int i = 0; i < buttonMappings.length;i++){
			VRControllerButtonMapping vb = buttonMappings[i];

			if(vb==null) { //shouldnt
		        vb = new VRControllerButtonMapping(ViveButtons.values()[i + offset],"none");
		        buttonMappings[i] = vb;
			}
			
			if(vb.FunctionDesc.equals("none")){
				vb.key = null;
				vb.FunctionExt = 0;
			} else 	if(vb.FunctionDesc.startsWith("keyboard")){
				vb.key = null;
	    		if(vb.FunctionDesc.contains("-")) vb.FunctionExt = 0;
			} else {
		        KeyBinding[] var3 = mc.gameSettings.keyBindings;
		        for (final KeyBinding keyBinding : var3) {	
		        	if (keyBinding.getKeyDescription().equals(vb.FunctionDesc)){
		        		vb.key = keyBinding;    
		        		vb.FunctionExt = 0;
		        		break;
		        	}
				}					
			}
			
			if(vb.key == null && !vb.FunctionDesc.startsWith("keyboard"))
				System.out.println("Unknown key binding: " + vb.FunctionDesc);
		}
	}

    public void resetSettings()
    {
        // Get the Minecrift defaults
        loadDefaults();
    }
    
    public String getKeyBinding( VRSettings.VrOptions par1EnumOptions )
    {
        String var2 = par1EnumOptions.getEnumString();

        String var3 = var2 + ": ";
        String var4 = var3;
        String var5;

        switch( par1EnumOptions) {
            case OTHER_HUD_SETTINGS:
                return var2;
            case OTHER_RENDER_SETTINGS:
                return var2;
            case LOCOMOTION_SETTINGS:
                return var2;
	        case MOVEMENT_MULTIPLIER:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.movementSpeedMultiplier) });
	        case HUD_OPACITY:
	        	if( this.hudOpacity > 0.99)
	        		return var4 + "Opaque";
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudOpacity) });
            case RENDER_MENU_BACKGROUND:
                return this.menuBackground ? var4 + "ON" : var4 + "OFF";
	        case HUD_HIDE:
	            return this.hideGui ? var4 + "YES" : var4 + "NO";
	        case RENDER_FULL_FIRST_PERSON_MODEL_MODE:
                if (this.renderFullFirstPersonModelMode == RENDER_FIRST_PERSON_FULL)
                    return var4 + "Full";
                else if (this.renderFullFirstPersonModelMode == RENDER_FIRST_PERSON_HAND)
                    return var4 + "Hand";
                else if (this.renderFullFirstPersonModelMode == RENDER_FIRST_PERSON_NONE)
                    return var4 + "None";
            case MIRROR_DISPLAY:
                switch(this.displayMirrorMode) {
                    case MIRROR_OFF:
                    default:
                        return var4 + "OFF";
                    case MIRROR_ON_ONE_THIRD_FRAME_RATE:
                        return var4 + "DUAL (1/3)";
                    case MIRROR_ON_FULL_FRAME_RATE:
                        return var4 + "DUAL (Full)";
                    case MIRROR_ON_ONE_THIRD_FRAME_RATE_SINGLE_VIEW:
                        return var4 + "SINGLE (1/3)";
                    case MIRROR_ON_FULL_FRAME_RATE_SINGLE_VIEW:
                        return var4 + "SINGLE (Full)";
                    case MIRROR_MIXED_REALITY:
                        return var4 + "MIXED REALITY";
                    case MIRROR_FIRST_PERSON:
                        return var4 + "UNDISTORTED";
                }
            case MIXED_REALITY_KEY_COLOR:
                if (this.mixedRealityKeyColor.equals(new Color(0, 0, 0))) {
                	return var4 + "BLACK";
                } else if (this.mixedRealityKeyColor.equals(new Color(255, 0, 0))) {
                	return var4 + "RED";
                } else if (this.mixedRealityKeyColor.equals(new Color(255, 255, 0))) {
                	return var4 + "YELLOW";
                } else if (this.mixedRealityKeyColor.equals(new Color(0, 255, 0))) {
                	return var4 + "GREEN";
                } else if (this.mixedRealityKeyColor.equals(new Color(0, 255, 255))) {
                	return var4 + "CYAN";
                } else if (this.mixedRealityKeyColor.equals(new Color(0, 0, 255))) {
                	return var4 + "BLUE";
                } else if (this.mixedRealityKeyColor.equals(new Color(255, 0, 255))) {
                	return var4 + "MAGENTA";
                }
                return var4 + this.mixedRealityKeyColor.getRed() + " " + this.mixedRealityKeyColor.getGreen() + " " + this.mixedRealityKeyColor.getBlue();
             case MIXED_REALITY_RENDER_HANDS:
                return this.mixedRealityRenderHands ? var4 + "YES" : var4 + "NO";
            case INSIDE_BLOCK_SOLID_COLOR:
            	return this.insideBlockSolidColor ? var4 + "SOLID COLOR" : var4 + "TEXTURE";
 	        case MAIN_MENU_ROOM:
	            if (this.menuRoom == MAINMENU_JRBUDDA) {
	            	return var4 + "OLD";
	            } else if (this.menuRoom == MAINMENU_TECHJAR) {
	            	return var4 + "NEW";
	            }
	            return "???";
            case WALK_UP_BLOCKS:
                return this.walkUpBlocks ? var4 + "YES" : var4 + "NO";
 	        case HUD_SCALE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudScale) });
            case HUD_LOCK_TO:
                switch (this.vrHudLockMode) {
                // VIVE - lock to hand instead of body
                case HUD_LOCK_HAND:
                	return var4 + " hand";
                case HUD_LOCK_HEAD:
                	return var4 + " head";
                case HUD_LOCK_WRIST:
                	return var4 + " wrist";
                case HUD_LOCK_BODY:
                    return var4 + " body";
                }
	        case HUD_DISTANCE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudDistance) });
	        case HUD_PITCH:
	            return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.hudPitchOffset) });
            case HUD_YAW:
            	return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.hudYawOffset) });
            case RENDER_SCALEFACTOR:
            	return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.renderScaleFactor) });
            case FSAA:
            	return this.useFsaa ? var4 + "ON" : var4 + "OFF";
            case CROSSHAIR_SCALE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.crosshairScale) });
            case MENU_CROSSHAIR_SCALE:
                return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.menuCrosshairScale) });
	        case RENDER_CROSSHAIR_MODE:
                if (this.renderInGameCrosshairMode == RENDER_CROSSHAIR_MODE_HUD)
                    return var4 + "With HUD";
                else if (this.renderInGameCrosshairMode == RENDER_CROSSHAIR_MODE_ALWAYS)
                    return var4 + "Always";
                else if (this.renderInGameCrosshairMode == RENDER_CROSSHAIR_MODE_NEVER)
                    return var4 + "Never";
	        case RENDER_BLOCK_OUTLINE_MODE:
                if (this.renderBlockOutlineMode == RENDER_BLOCK_OUTLINE_MODE_HUD)
                    return var4 + "With HUD";
                else if (this.renderBlockOutlineMode == RENDER_BLOCK_OUTLINE_MODE_ALWAYS)
                    return var4 + "Always";
                else if (this.renderBlockOutlineMode == RENDER_BLOCK_OUTLINE_MODE_NEVER)
                    return var4 + "Never";
	        case HUD_OCCLUSION:
	        	return this.hudOcclusion ? var4 + "ON" : var4 + "OFF";
	        case CROSSHAIR_OCCLUSION:
	        	return this.useCrosshairOcclusion ? var4 + "ON" : var4 + "OFF";
	        case MONO_FOV:
	        	return var4 + String.format("%.0f\u00B0", new Object[] { Float.valueOf(this.mc.gameSettings.fovSetting) });
	        case INERTIA_FACTOR:
	        	if (this.inertiaFactor == INERTIA_NONE)
	        		return var4 + "Automan";
	        	else if (this.inertiaFactor == INERTIA_NORMAL)
	        		return var4 + "Normal";
                else if (this.inertiaFactor == INERTIA_LARGE)
                    return var4 + "A lot";
                else if (this.inertiaFactor == INERTIA_MASSIVE)
                    return var4 + "Even more";
                // VIVE START - new options
            case SIMULATE_FALLING:
                return this.simulateFalling ? var4 + "ON" : var4 + "OFF";
            case WEAPON_COLLISION:
                return this.weaponCollision ? var4 + "ON" : var4 + "OFF";
                // VIVE END - new options
                //JRBUDDA
            case ALLOW_MODE_SWITCH:
                return this.vrAllowLocoModeSwotch ? var4 + "ON" : var4 + "OFF";     
            case MOVE_MODE:
            	if(this.vrFreeMove == false){
            		return var4 + "Teleport";
            	} else return var4+ "Free Move";
            case ALLOW_CRAWLING:
                return this.vrAllowCrawling ? var4 + "ON" : var4 + "OFF"; 
            case LIMIT_TELEPORT:
                return this.vrLimitedSurvivalTeleport ? var4 + "ON" : var4 + "OFF";
            case REVERSE_HANDS:
            	return this.vrReverseHands ? var4 + "ON" : var4 + "OFF";
            case STENCIL_ON:
            	return this.vrUseStencil ? var4 + "ON" : var4 + "OFF";
            case BCB_ON:
            	return this.vrShowBlueCircleBuddy ? var4 + "ON" : var4 + "OFF";
            case WORLD_SCALE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.vrWorldScale)})+ "x" ;
            case WORLD_ROTATION:
	            return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.vrWorldRotation) });
            case WORLD_ROTATION_INCREMENT:
	            return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.vrWorldRotationIncrement) });
            case TOUCH_HOTBAR:
            	return this.vrTouchHotbar ? var4 + "ON" : var4 + "OFF";
            case PLAY_MODE_SEATED:
            	return this.seated ? var4 + "SEATED" : var4 + "STANDING";
                //END JRBUDDA
            case REALISTIC_JUMP:
                return this.realisticJumpEnabled ? var4 + "ON" : var4 + "OFF";
            case REALISTIC_SNEAK:
                return this.realisticSneakEnabled ? var4 + "ON" : var4 + "OFF";
            case REALISTIC_CLIMB:
                return this.realisticClimbEnabled ? var4 + "ON" : var4 + "OFF";
            case REALISTIC_SWIM:
                return this.realisticSwimEnabled ? var4 + "ON" : var4 + "OFF";
            case REALISTIC_ROW:
                return this.realisticRowEnabled ? var4 + "ON" : var4 + "OFF";
            case VEHICLE_ROTATION:
                return this.vehicleRotation ? var4 + "ON" : var4 + "OFF";
            case CALIBRATE_HEIGHT:
                return var2;
            case WALK_MULTIPLIER:
                return var4+ String.format("%.1f",walkMultiplier);
            case X_SENSITIVITY:
                return var4+ String.format("%.2f",xSensitivity);
            case Y_SENSITIVITY:
                return var4+ String.format("%.2f",ySensitivity);
            case KEYHOLE:
                return var4+ String.format("%.0f",keyholeX);
            case RESET_ORIGIN:
                return var2;
            case FREEMOVE_MODE:
                switch (this.vrFreeMoveMode) {
                // VIVE - lock to hand instead of body
                case FREEMOVE_CONTROLLER:
                	return var4 + " Controller";
                case FREEMOVE_HMD:
                	return var4 + " HMD";
                case FREEMOVE_RUNINPLACE:
                	return var4 + " RunInPlace";
                }
            case FOV_REDUCTION:
                return this.useFOVReduction ? var4 + "ON" : var4 + "OFF";
 	        default:
	        	return "";
        }
    }

    public float getOptionFloatValue(VRSettings.VrOptions par1EnumOptions)
    {
    	switch( par1EnumOptions ) {
			case MOVEMENT_MULTIPLIER :
				return this.movementSpeedMultiplier ;
			case HUD_SCALE :
				return this.hudScale ;
			case HUD_OPACITY :
				return this.hudOpacity ;
			case HUD_DISTANCE :
				return this.hudDistance ;
			case HUD_PITCH :
				return this.hudPitchOffset ;
            case HUD_YAW :
                return this.hudYawOffset ;
			case CROSSHAIR_SCALE :
				return this.crosshairScale ;
            case MENU_CROSSHAIR_SCALE :
                return this.menuCrosshairScale ;
            case WALK_MULTIPLIER:
                return this.walkMultiplier;
            case X_SENSITIVITY:
                return this.xSensitivity;
            case Y_SENSITIVITY:
                return this.ySensitivity;
            case KEYHOLE:
                return this.keyholeX;
            // VIVE START - new options
            case WORLD_SCALE:          	
            	if(vrWorldScale ==  0.1f) return 0;
            	if(vrWorldScale ==  0.25f) return 1;
            	if(vrWorldScale >=  0.5f && vrWorldScale <=  2.0f) return (vrWorldScale / 0.1f) - 3f;
            	if(vrWorldScale == 3) return 18;
            	if(vrWorldScale == 4) return 19;
            	if(vrWorldScale == 6) return 20;
            	if(vrWorldScale == 8) return 21;
            	if(vrWorldScale == 10) return 22;
            	if(vrWorldScale == 12) return 23;
            	if(vrWorldScale == 16) return 24;
            	if(vrWorldScale == 20) return 25;
            	if(vrWorldScale == 30) return 26;
            	if(vrWorldScale == 50) return 27;
            	if(vrWorldScale == 75) return 28;
            	if(vrWorldScale == 100) return 29;
            	return 7;
            case WORLD_ROTATION:
                return vrWorldRotation;
            case WORLD_ROTATION_INCREMENT:
            	if(vrWorldRotationIncrement == 10f) return 0;
            	if(vrWorldRotationIncrement == 36f) return 1;            	
            	if(vrWorldRotationIncrement == 45f) return 2;
            	if(vrWorldRotationIncrement == 90f) return 3;
            	if(vrWorldRotationIncrement == 180f) return 4;
            	return 0;
            case MONO_FOV:
            	return this.mc.gameSettings.fovSetting;
            case RENDER_SCALEFACTOR:
            	return this.renderScaleFactor;
            // VIVE END - new options
            default:
                return 0.0f;
    	}
    }
    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     */
    public void setOptionValue(VRSettings.VrOptions par1EnumOptions, int par2)
    {
    	switch( par1EnumOptions )
    	{
            case RENDER_MENU_BACKGROUND:
                this.menuBackground = !this.menuBackground;
                break;
	        case HUD_HIDE:
	            this.hideGui = !this.hideGui;
	            break;
	        case RENDER_FULL_FIRST_PERSON_MODEL_MODE:
                this.renderFullFirstPersonModelMode++;
                if (this.renderFullFirstPersonModelMode > RENDER_FIRST_PERSON_NONE)
                    this.renderFullFirstPersonModelMode = RENDER_FIRST_PERSON_FULL;
	            break;
             case MIRROR_DISPLAY:
                this.displayMirrorMode++;
                if (this.displayMirrorMode > MIRROR_FIRST_PERSON)
                    this.displayMirrorMode = MIRROR_OFF;
                break;
            case MIXED_REALITY_KEY_COLOR:
            	if (this.mixedRealityKeyColor.equals(new Color(0, 0, 0))) {
            		this.mixedRealityKeyColor = new Color(255, 0, 0);
	            } else if (this.mixedRealityKeyColor.equals(new Color(255, 0, 0))) {
	            	this.mixedRealityKeyColor = new Color(255, 255, 0);
	            } else if (this.mixedRealityKeyColor.equals(new Color(255, 255, 0))) {
	            	this.mixedRealityKeyColor = new Color(0, 255, 0);
	            } else if (this.mixedRealityKeyColor.equals(new Color(0, 255, 0))) {
	            	this.mixedRealityKeyColor = new Color(0, 255, 255);
	            } else if (this.mixedRealityKeyColor.equals(new Color(0, 255, 255))) {
	            	this.mixedRealityKeyColor = new Color(0, 0, 255);
	            } else if (this.mixedRealityKeyColor.equals(new Color(0, 0, 255))) {
	            	this.mixedRealityKeyColor = new Color(255, 0, 255);
	            } else if (this.mixedRealityKeyColor.equals(new Color(255, 0, 255))) {
	            	this.mixedRealityKeyColor = new Color(0, 0, 0);
	            } else {
	            	this.mixedRealityKeyColor = new Color(0, 0, 0);
	            }
                break;
            case MIXED_REALITY_RENDER_HANDS:
            	this.mixedRealityRenderHands = !this.mixedRealityRenderHands;
            	break;
            case INSIDE_BLOCK_SOLID_COLOR:
            	this.insideBlockSolidColor = !this.insideBlockSolidColor;
            	break;
            case MAIN_MENU_ROOM:
                this.menuRoom++;
                if (this.menuRoom >= 2) this.menuRoom = 0;
                break;
            case WALK_UP_BLOCKS:
                this.walkUpBlocks = !this.walkUpBlocks;
                break;
             case HUD_LOCK_TO:
                switch (this.vrHudLockMode) {
                // VIVE - lock to hand instead of body
                case HUD_LOCK_HAND:
                	this.vrHudLockMode = HUD_LOCK_HEAD;
                	break;
                case HUD_LOCK_HEAD:
                   	this.vrHudLockMode = HUD_LOCK_WRIST;
                	break;
                case HUD_LOCK_WRIST:
                   	this.vrHudLockMode = HUD_LOCK_BODY;
                	break;
                case HUD_LOCK_BODY:
                    this.vrHudLockMode = HUD_LOCK_HAND;
                }
                break;
	        case FSAA:
	            this.useFsaa = !this.useFsaa;
	            break;
  	        case RENDER_CROSSHAIR_MODE:
	            this.renderInGameCrosshairMode++;
                if (this.renderInGameCrosshairMode > RENDER_CROSSHAIR_MODE_NEVER)
                    this.renderInGameCrosshairMode = RENDER_CROSSHAIR_MODE_ALWAYS;
	            break;
	        case RENDER_BLOCK_OUTLINE_MODE:
                this.renderBlockOutlineMode++;
                if (this.renderBlockOutlineMode > RENDER_BLOCK_OUTLINE_MODE_NEVER)
                    this.renderBlockOutlineMode = RENDER_BLOCK_OUTLINE_MODE_ALWAYS;
	            break;
	            
	        case HUD_OCCLUSION:
	            this.hudOcclusion = !this.hudOcclusion;
	            break;
            case CROSSHAIR_OCCLUSION:
                this.useCrosshairOcclusion = !this.useCrosshairOcclusion;
                break;
             case INERTIA_FACTOR:
                this.inertiaFactor +=1;
                if (this.inertiaFactor > INERTIA_MASSIVE)
                    this.inertiaFactor = INERTIA_NONE;
                break;
             // VIVE START - new options
            case SIMULATE_FALLING:
                this.simulateFalling = !this.simulateFalling;
                break;
            case WEAPON_COLLISION:
                this.weaponCollision = !this.weaponCollision;
                break;
            // VIVE END - new options
                //JRBUDDA
            case ALLOW_MODE_SWITCH:
                this.vrAllowLocoModeSwotch = !this.vrAllowLocoModeSwotch;
                break;
            case MOVE_MODE:
                this.vrFreeMove = !this.vrFreeMove;
                Minecraft.getMinecraft().vrPlayer.setFreeMoveMode(vrFreeMove);
                break;
            case ALLOW_CRAWLING:
                this.vrAllowCrawling = !this.vrAllowCrawling;
                break;
            case LIMIT_TELEPORT:
                this.vrLimitedSurvivalTeleport = !this.vrLimitedSurvivalTeleport;
                break;
            case REVERSE_HANDS:
                this.vrReverseHands = !this.vrReverseHands;
                break;
            case STENCIL_ON:
                this.vrUseStencil = !this.vrUseStencil;
                break;
            case BCB_ON:
                this.vrShowBlueCircleBuddy = !this.vrShowBlueCircleBuddy;
                break;
            case TOUCH_HOTBAR:
                this.vrTouchHotbar = !this.vrTouchHotbar;
                break;
            case PLAY_MODE_SEATED:
                this.seated = !this.seated;
                break;
                //JRBUDDA
            case REALISTIC_JUMP:
                realisticJumpEnabled = !realisticJumpEnabled;
                break;
            case REALISTIC_SWIM:
                realisticSwimEnabled = !realisticSwimEnabled;
                break;
            case REALISTIC_CLIMB:
                realisticClimbEnabled = !realisticClimbEnabled;
                break;
            case REALISTIC_ROW:
                realisticRowEnabled = !realisticRowEnabled;
                break;
            case REALISTIC_SNEAK:
                realisticSneakEnabled = !realisticSneakEnabled;
                break;
            case VEHICLE_ROTATION:
                vehicleRotation = !vehicleRotation;
                break;
            case CALIBRATE_HEIGHT:
                if(seated) {
                    MCOpenVR.resetPosition();
                }
                playerEyeHeight = (float) Minecraft.getMinecraft().roomScale.getHMDPos_Room().yCoord;
                break;
            case FREEMOVE_MODE:
                switch (this.vrFreeMoveMode) {
                case FREEMOVE_CONTROLLER:
                	this.vrFreeMoveMode = FREEMOVE_HMD;
                	break;
                case FREEMOVE_HMD:
                   	this.vrFreeMoveMode = FREEMOVE_RUNINPLACE;
                	break;
                case FREEMOVE_RUNINPLACE:
                   	this.vrFreeMoveMode = FREEMOVE_CONTROLLER;
                	break;
                }
                break;
            case FOV_REDUCTION:
            	useFOVReduction = !useFOVReduction;
            	break;
            default:
                    break;
    	}

        this.saveOptions();
    }

    public void setOptionFloatValue(VRSettings.VrOptions par1EnumOptions, float par2)
    {
    	switch( par1EnumOptions ) {
	        case MOVEMENT_MULTIPLIER:
	            this.movementSpeedMultiplier = par2;
	            break;
	        case HUD_SCALE:
	            this.hudScale = par2;
	        	break;
	        case HUD_OPACITY:
	            this.hudOpacity = par2;
	        	break;
	        case HUD_DISTANCE:
	            this.hudDistance = par2;
	        	break;
	        case HUD_PITCH:
	            this.hudPitchOffset = par2;
	        	break;
            case HUD_YAW:
                this.hudYawOffset = par2;
                break;
	        case CROSSHAIR_SCALE:
	            this.crosshairScale = par2;
	        	break;
            case MENU_CROSSHAIR_SCALE:
                this.menuCrosshairScale = par2;
                break;
            case WALK_MULTIPLIER:
                this.walkMultiplier=par2;
                break;
            // VIVE START - new options
            case WORLD_SCALE:
            	if(par2 ==  0) vrWorldScale = 0.1f;
            	else if(par2 ==  1) vrWorldScale = 0.25f;
            	else if(par2 >=  2 && par2 <=  17) vrWorldScale = (float) (par2 * 0.1 + 0.3);
            	else if(par2 == 18) vrWorldScale = 3f;
            	else if(par2 == 19) vrWorldScale = 4f;
            	else if(par2 == 20) vrWorldScale = 6f;
            	else if(par2 == 21) vrWorldScale = 8f;
            	else if(par2 == 22) vrWorldScale = 10f;
            	else if(par2 == 23) vrWorldScale = 12f;
            	else if(par2 == 24) vrWorldScale = 16f;
            	else if(par2 == 25) vrWorldScale = 20f;
               	else if(par2 == 26) vrWorldScale = 30f;
               	else if(par2 == 27) vrWorldScale = 50f;
               	else if(par2 == 28) vrWorldScale = 75f;
               	else if(par2 == 29) vrWorldScale = 100f;           	         	
            	else vrWorldScale = 1;           	
                break;
            case WORLD_ROTATION:
                this.vrWorldRotation = par2;
                break;
            case WORLD_ROTATION_INCREMENT:
            	if(par2 == 0f) this.vrWorldRotationIncrement =  10f;
            	if(par2 == 1f) this.vrWorldRotationIncrement =  36f;            	
            	if(par2 == 2f) this.vrWorldRotationIncrement =  45f;
            	if(par2 == 3f) this.vrWorldRotationIncrement =  90f;
            	if(par2 == 4f) this.vrWorldRotationIncrement =  180f;
                break;
            case X_SENSITIVITY:
                this.xSensitivity=par2;
                break;
            case Y_SENSITIVITY:
                this.ySensitivity=par2;
                break;
            case KEYHOLE:
            	this.keyholeX=par2;
            	break;
            case MONO_FOV:
            	this.mc.gameSettings.fovSetting = par2;
            	break;
            case RENDER_SCALEFACTOR:
            	this.renderScaleFactor = par2;
            	break;
            	// VIVE END - new options
            default:
            	break;
    	}
	
        this.saveOptions();
    }



    public void saveOptions()
    {
        saveOptions(null); // Use null for current profile
    }

    private void storeDefaults()
    {
        saveOptions(this.defaults);
    }

    private void saveOptions(JSONObject theProfiles)
    {
        // Save Minecrift settings
        try
        {
            ProfileWriter var5 = new ProfileWriter(ProfileManager.PROFILE_SET_VR, theProfiles);

            var5.println("version:" + version);
            var5.println("newlyCreated:" + false );
            //var5.println("firstLoad:" + this.firstLoad );  
            var5.println("playerEyeHeight:" + this.playerEyeHeight);
            var5.println("stereoProviderPluginID:"+ this.stereoProviderPluginID);
            var5.println("badStereoProviderPluginID:"+ this.badStereoProviderPluginID);
            var5.println("hudOpacity:" + this.hudOpacity);
            var5.println("menuBackground:" + this.menuBackground);
            var5.println("renderFullFirstPersonModelMode:" + this.renderFullFirstPersonModelMode);
            var5.println("shaderIndex:" + this.shaderIndex);
            var5.println("displayMirrorMode:" + this.displayMirrorMode);
            var5.println("mixedRealityKeyColor:" + this.mixedRealityKeyColor.getRed() + "," + this.mixedRealityKeyColor.getGreen() + "," + this.mixedRealityKeyColor.getBlue());
            var5.println("mixedRealityRenderHands:" + this.mixedRealityRenderHands);
            var5.println("insideBlockSolidColor:" + this.insideBlockSolidColor);
            var5.println("mainMenuRoom:" + this.menuRoom);
            var5.println("walkUpBlocks:" + this.walkUpBlocks);
            var5.println("hudScale:" + this.hudScale);
            var5.println("renderScaleFactor:" + this.renderScaleFactor);
            var5.println("vrHudLockMode:" + this.vrHudLockMode);
            var5.println("hudDistance:" + this.hudDistance);
            var5.println("hudPitchOffset:" + this.hudPitchOffset);
            var5.println("hudYawOffset:" + this.hudYawOffset);
            var5.println("useFsaa:" + this.useFsaa);
            var5.println("movementSpeedMultiplier:" + this.movementSpeedMultiplier);
            var5.println("renderInGameCrosshairMode:" + this.renderInGameCrosshairMode);
            var5.println("renderBlockOutlineMode:" + this.renderBlockOutlineMode);
            var5.println("hudOcclusion:" + this.hudOcclusion);
            var5.println("useCrosshairOcclusion:" + this.useCrosshairOcclusion);
            var5.println("crosshairScale:" + this.crosshairScale);
            var5.println("menuCrosshairScale:" + this.menuCrosshairScale);
            var5.println("inertiaFactor:" + this.inertiaFactor);
            var5.println("smoothRunTickCount:" + this.smoothRunTickCount);
            var5.println("smoothTick:" + this.smoothTick);
            var5.println("hideGui:" + this.hideGui);
            //VIVE
            var5.println("simulateFalling:" + this.simulateFalling);
            var5.println("weaponCollision:" + this.weaponCollision);
            //END VIVE
            
            //JRBUDDA
            var5.println("allowCrawling:" + this.vrAllowCrawling);
            var5.println("allowModeSwitch:" + this.vrAllowLocoModeSwotch);   
            var5.println("freeMoveDefault:" + this.vrFreeMove);
            var5.println("limitedTeleport:" + this.vrLimitedSurvivalTeleport);
            var5.println("reverseHands:" + this.vrReverseHands);
            var5.println("stencilOn:" + this.vrUseStencil);
            var5.println("bcbOn:" + this.vrShowBlueCircleBuddy);
            var5.println("worldScale:" + this.vrWorldScale);
            var5.println("worldRotation:" + this.vrWorldRotation);
            var5.println("worldRotationIncrement:" + this.vrWorldRotationIncrement);
            var5.println("vrFixedCamposX:" + this.vrFixedCamposX);
            var5.println("vrFixedCamposY:" + this.vrFixedCamposY);
            var5.println("vrFixedCamposZ:" + this.vrFixedCamposZ);
            var5.println("vrFixedCamrotPitch:" + this.vrFixedCamrotPitch);
            var5.println("vrFixedCamrotYaw:" + this.vrFixedCamrotYaw);
            var5.println("vrFixedCamrotRoll:" + this.vrFixedCamrotRoll);
            var5.println("vrTouchHotbar:" + this.vrTouchHotbar);
            var5.println("seated:" + this.seated);
            var5.println("jumpThreshold:" + this.jumpThreshold);
            var5.println("sneakThreshold:" + this.sneakThreshold);
            var5.println("realisticJumpEnabled:" + this.realisticJumpEnabled);
            var5.println("realisticSwimEnabled:" + this.realisticSwimEnabled);
            var5.println("realisticClimbEnabled:" + this.realisticClimbEnabled);
            var5.println("realisticRowEnabled:" + this.realisticRowEnabled);
            var5.println("realisticSneakEnabled:" + this.realisticSneakEnabled);
            var5.println("headToHmdLength:" + this.headToHmdLength);
            var5.println("walkMultiplier:" + this.walkMultiplier);
            var5.println("vrFreeMoveMode:" + this.vrFreeMoveMode);
            var5.println("xSensitivity:" + this.xSensitivity);
            var5.println("ySensitivity:" + this.ySensitivity);
            var5.println("keyholeX:" + this.keyholeX);
            var5.println("autoCalibration:" + this.autoCalibration);
            var5.println("manualCalibration:" + this.manualCalibration);
            var5.println("vehicleRotation:" + this.vehicleRotation);
            var5.println("fovReduction:" + this.useFOVReduction);
            
            if (vrQuickCommands == null) vrQuickCommands = getQuickCommandsDefaults(); //defaults
            
            for (int i = 0; i < 11 ; i++){
            	var5.println("QUICKCOMMAND_" + i + ":" + vrQuickCommands[i]);
            }
   
           
            if (buttonMappings == null) resetBindings(); //defaults
              
            for (int i = 0; i<buttonMappings.length;i++){
            	VRControllerButtonMapping vb = buttonMappings[i];
            	var5.println(vb.toString());
			}
            
            //END JRBUDDA
            var5.close();
        }
        catch (Exception var3)
        {
            logger.warn("Failed to save VR options: " + var3.getMessage());
            var3.printStackTrace();
        }
    }

    public void resetBindings(){
    	buttonMappings = getBindingsDefaults();
    	processBindings();
    }
    
  
    public void setMinecraftPlayerEyeHeight(float eyeHeight)
    {
        this.playerEyeHeight = eyeHeight;
    }

    public float getMinecraftPlayerEyeHeight(){
        return playerEyeHeight;
    }



    /**
     * Parses a string into a float.
     */
    private float parseFloat(String par1Str)
    {
        return par1Str.equals("true") ? 1.0F : (par1Str.equals("false") ? 0.0F : Float.parseFloat(par1Str));
    }

    public float getHeadTrackSensitivity()
    {
        //if (this.useQuaternions)
            return 1.0f;

        //return this.headTrackSensitivity;  // TODO: If head track sensitivity is working again... if
    }

    public static double getInertiaAddFactor(int inertiaFactor)
    {
        float addFac = INERTIA_NORMAL_ADD_FACTOR;
        switch (inertiaFactor)
        {
            case INERTIA_NONE:
                addFac = INERTIA_NONE_ADD_FACTOR;
                break;
            case INERTIA_LARGE:
                addFac = INERTIA_LARGE_ADD_FACTOR;
                break;
            case INERTIA_MASSIVE:
                addFac = INERTIA_MASSIVE_ADD_FACTOR;
                break;
        }
        return addFac;
    }


    public static enum VrOptions
    {
        // Minecrift below here

        // TODO: Port to Mark's excellent VROption implementation

        //General
        HUD_SCALE("HUD Size", true, false),
        HUD_DISTANCE("HUD Distance", true, false),
        HUD_PITCH("HUD Vertical Offset", true, false),
        HUD_YAW("HUD Horiz. Offset", true, false),
        HUD_LOCK_TO("HUD Orientation Lock", false, true),
        HUD_OPACITY("HUD Opacity", true, false),
        RENDER_MENU_BACKGROUND("Menu Background", false, true),
        MAIN_MENU_ROOM("Main Menu Room", false, false),
        HUD_HIDE("Hide HUD (F1)", false, true),
        HUD_OCCLUSION("HUD Occlusion", false, true),
        CROSSHAIR_OCCLUSION("Crosshair Occlusion", false, true),
        CHAT_FADE_AWAY("Chat Persistence", false, true),
        DUMMY("Dummy", false, true),
        DUMMY_SMALL("Dummy", false, true),
        VR_RENDERER("Stereo Renderer", false, true),
        VR_HEAD_ORIENTATION("Head Orientation", false, true),
        VR_HEAD_POSITION("Head Position", false, true),
        VR_CONTROLLER("Controller", false, true),
        CROSSHAIR_SCALE("Crosshair Size", true, false),
        MENU_CROSSHAIR_SCALE("Menu Crosshair Size", true, false),
        RENDER_CROSSHAIR_MODE("Show Crosshair", false, true),
        CROSSHAIR_ROLL("Roll Crosshair", false, true),
        CROSSHAIR_SCALES_WITH_DISTANCE("Crosshair Scaling", false, true),
        RENDER_BLOCK_OUTLINE_MODE("Show Block Outline", false, true),
        LOAD_MUMBLE_LIB("Load Mumble Lib", false, true),

        // Player
         RENDER_OWN_HEADWEAR("Render Own Headwear", false, true),
        RENDER_FULL_FIRST_PERSON_MODEL_MODE("First Person Model", false, true),
        RENDER_PLAYER_OFFSET("View Body Offset", true, false),


        //HMD/render
        FSAA("FSAA", false, true),
        MIRROR_DISPLAY("Mirror Display", false, true),
        MIXED_REALITY_KEY_COLOR("MR Key Color", false, false),
        MIXED_REALITY_RENDER_HANDS("MR Show Hands", false, true),
        INSIDE_BLOCK_SOLID_COLOR("Inside Block", false, true),
        WALK_UP_BLOCKS("Walk up blocks", false, true),
        //Movement/aiming controls
        DECOUPLE_LOOK_MOVE("Decouple Look/Move", false, true),
        MOVEMENT_MULTIPLIER("Move. Speed Multiplier", true, false),
        STRAFE_MULTIPLIER("Strafe Speed Multiplier", true, false),
        PITCH_AFFECTS_CAMERA("Pitch Affects Camera", false, true),
        JOYSTICK_DEADZONE("Joystick Deadzone",true,false),
        KEYHOLE_HEAD_RELATIVE("Keyhole Moves With Head",false,true),
        MOUSE_AIM_TYPE("Aim Type",false,true),
        CROSSHAIR_HEAD_RELATIVE("Cursor Relative To",false,true),
        MOVEAIM_HYDRA_USE_CONTROLLER_ONE("Controller", false, true),
        JOYSTICK_AIM_TYPE("Aim Type", false, false),
        AIM_PITCH_OFFSET("Vertical Cursor Offset",true,false),
        INERTIA_FACTOR("Player Inertia",false,true),

        // VIVE START - new options
        SIMULATE_FALLING("Simulate falling", false, true),
        WEAPON_COLLISION("Weapon collision", false, true),
        // VIVE END - new options

        //JRBUDDA VIVE
        ALLOW_CRAWLING("Allow crawling",false, true),
        ALLOW_MODE_SWITCH("Allow Mode Switch",false, true),
        MOVE_MODE("Move Mode",false, true),
        LIMIT_TELEPORT("Limit TP in Survival",false, true),
        REVERSE_HANDS("Reverse Hands",false, true),
        STENCIL_ON("Use Eye Stencil", false, true), 
        BCB_ON("Show Body Position", false, true),    
        WORLD_SCALE("World Scale", true, false),
        WORLD_ROTATION("World Rotation", true, false),
        WORLD_ROTATION_INCREMENT("Rotation Increment", true, false),
        TOUCH_HOTBAR("Touch Hotbar Enabled", false, true),
        PLAY_MODE_SEATED("Play Mode", false, true),
        RENDER_SCALEFACTOR("Render Scale Factor", true, false),
        MONO_FOV("Mono FOV", true, false),
        //END JRBUDDA
        REALISTIC_JUMP("Roomscale Jumping",false,true),
        REALISTIC_SNEAK("Roomscale Sneaking",false,true),
        REALISTIC_CLIMB("Roomscale Climbing",false,true),
        REALISTIC_SWIM("Roomscale Swimming",false,true),
        REALISTIC_ROW("Roomscale Rowing",false,true),
        CALIBRATE_HEIGHT("Calibrate Height",false,true),
        WALK_MULTIPLIER("Walking Multipier",true,false),
        FREEMOVE_MODE("Move Type", false, true),
        VEHICLE_ROTATION("Vechile Rotation",false,true),
        //SEATED
        RESET_ORIGIN("Reset Origin",false,true),
        X_SENSITIVITY("Rotation Speed",true,false),
        Y_SENSITIVITY("Y Sensitivity",true,false),
        KEYHOLE("Keyhole",true,false),
        FOV_REDUCTION("FOV Comfort Reduction",false,true),
        
        // OTher buttons
        OTHER_HUD_SETTINGS("Overlay/Crosshair/Chat...", false, true),
        OTHER_RENDER_SETTINGS("IPD / FOV...", false, true),
        LOCOMOTION_SETTINGS("Locomotion Settings...", false, true); 

//        ANISOTROPIC_FILTERING("options.anisotropicFiltering", true, false, 1.0F, 16.0F, 0.0F)
//                {
//                    private static final String __OBFID = "CL_00000654";
//                    protected float snapToStep(float p_148264_1_)
//                    {
//                        return (float) MathHelper.roundUpToPowerOfTwo((int) p_148264_1_);
//                    }
//                },

        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private float valueMin;
        private float valueMax;

        private static final String __OBFID = "CL_00000653";

        public static VRSettings.VrOptions getEnumOptions(int par0)
        {
            VRSettings.VrOptions[] aoptions = values();
            int j = aoptions.length;

            for (int k = 0; k < j; ++k)
            {
                VRSettings.VrOptions options = aoptions[k];

                if (options.returnEnumOrdinal() == par0)
                {
                    return options;
                }
            }

            return null;
        }

        private VrOptions(String par3Str, boolean isfloat, boolean isbool)
        {
            this(par3Str, isfloat, isbool, 0.0F, 1.0F, 0.0F);
        }

        private VrOptions(String p_i45004_3_, boolean p_i45004_4_, boolean p_i45004_5_, float p_i45004_6_, float p_i45004_7_, float p_i45004_8_)
        {
            this.enumString = p_i45004_3_;
            this.enumFloat = p_i45004_4_;
            this.enumBoolean = p_i45004_5_;
            this.valueMin = p_i45004_6_;
            this.valueMax = p_i45004_7_;
            this.valueStep = p_i45004_8_;
        }
        
        public boolean getEnumFloat()
        {
            return this.enumFloat;
        }

        public boolean getEnumBoolean()
        {
            return this.enumBoolean;
        }

        public int returnEnumOrdinal()
        {
            return this.ordinal();
        }

        public String getEnumString()
        {
            return this.enumString;
        }

        public float getValueMax()
        {
            return this.valueMax;
        }

        public void setValueMax(float p_148263_1_)
        {
            this.valueMax = p_148263_1_;
        }

        protected float snapToStep(float p_148264_1_)
        {
            if (this.valueStep > 0.0F)
            {
                p_148264_1_ = this.valueStep * (float)Math.round(p_148264_1_ / this.valueStep);
            }

            return p_148264_1_;
        }

        VrOptions(String p_i45005_3_, boolean p_i45005_4_, boolean p_i45005_5_, float p_i45005_6_, float p_i45005_7_, float p_i45005_8_, Object p_i45005_9_)
        {
            this(p_i45005_3_, p_i45005_4_, p_i45005_5_, p_i45005_6_, p_i45005_7_, p_i45005_8_);
        }
    }

    public static synchronized void initSettings( Minecraft mc, File dataDir )
    {
        ProfileManager.init(dataDir);
        mc.gameSettings = new GameSettings( mc, dataDir );
       // mc.gameSettings.saveOptions();
        mc.vrSettings = new VRSettings( mc, dataDir );
        mc.vrSettings.saveOptions();
    }

    public static synchronized void loadAll( Minecraft mc )
    {
        mc.gameSettings.loadOptions();
        mc.vrSettings.loadOptions();
    }

    public static synchronized void saveAll( Minecraft mc )
    {
        mc.gameSettings.saveOptions();
        mc.vrSettings.saveOptions();
    }

    public static synchronized void resetAll( Minecraft mc )
    {
        mc.gameSettings.resetSettings();
        mc.vrSettings.resetSettings();
    }

    public static synchronized String getCurrentProfile()
    {
        return ProfileManager.getCurrentProfileName();
    }

    public static synchronized boolean profileExists(String profile)
    {
        return ProfileManager.profileExists(profile);
    }

    public static synchronized SortedSet<String> getProfileList()
    {
        return ProfileManager.getProfileList();
    }

    public static synchronized boolean setCurrentProfile(String profile)
    {
        StringBuilder error = new StringBuilder();
        return setCurrentProfile(profile, error);
    }

    public static synchronized boolean setCurrentProfile(String profile, StringBuilder error)
    {
        boolean result = true;
        Minecraft mc = Minecraft.getMinecraft();

        // Save settings in current profile
        VRSettings.saveAll(mc);

        // Set the new profile
        result = ProfileManager.setCurrentProfile(profile, error);

        if (result) {
            // Load new profile
            VRSettings.loadAll(mc);
        }

        return result;
    }

    public static synchronized boolean createProfile(String profile, boolean useDefaults, StringBuilder error)
    {
        boolean result = true;
        Minecraft mc = Minecraft.getMinecraft();
        String originalProfile = VRSettings.getCurrentProfile();

        // Save settings in original profile
        VRSettings.saveAll(mc);

        // Create the new profile
        if (!ProfileManager.createProfile(profile, error))
            return false;

        // Set the new profile
        ProfileManager.setCurrentProfile(profile, error);

        // Save existing settings as new profile...

        if (useDefaults) {
            // ...unless set to use defaults
            VRSettings.resetAll(mc);
        }

        // Save new profile settings to file
        VRSettings.saveAll(mc);

        // Select the original profile
        ProfileManager.setCurrentProfile(originalProfile, error);
        VRSettings.loadAll(mc);

        return result;
    }

    public static synchronized boolean deleteProfile(String profile)
    {
        StringBuilder error = new StringBuilder();
        return deleteProfile(profile, error);
    }

    public static synchronized boolean deleteProfile(String profile, StringBuilder error)
    {
        Minecraft mc = Minecraft.getMinecraft();

        // Save settings in current profile
        VRSettings.saveAll(mc);

        // Nuke the profile data
        if (!ProfileManager.deleteProfile(profile, error))
            return false;

        // Load settings in case the selected profile has changed
        VRSettings.loadAll(mc);

        return true;
    }

    public static synchronized boolean duplicateProfile(String originalProfile, String newProfile, StringBuilder error)
    {
        Minecraft mc = Minecraft.getMinecraft();

        // Save settings in current profile
        VRSettings.saveAll(mc);

        // Duplicate the profile data
        if (!ProfileManager.duplicateProfile(originalProfile, newProfile, error))
            return false;

        return true;
    }

    public static synchronized boolean renameProfile(String originalProfile, String newProfile, StringBuilder error)
    {
        Minecraft mc = Minecraft.getMinecraft();

        // Save settings in current profile
        VRSettings.saveAll(mc);

        // Rename the profile
        if (!ProfileManager.renameProfile(originalProfile, newProfile, error))
            return false;

        return true;
    }
    
    public String[] getQuickCommandsDefaults(){
    	
    	String[] out = new String[12];
    	out[0] = "/gamemode 0";
    	out[1] = "/gamemode 1";
    	out[2] = "/help";
    	out[3] = "/home";
    	out[4] = "/sethome";
    	out[5] = "/spawn";
    	out[6] = "hi!";
    	out[7] = "bye!";
    	out[8] = "folow me!";
    	out[9] = "take this!";
    	out[10] = "thank you!";
    	out[11] = "praise the sun!";

    	return out;
    	
    }

    private VRControllerButtonMapping[] getBindingsDefaults(){

    	VRControllerButtonMapping[] out = new VRControllerButtonMapping[ViveButtons.values().length];
    		//vive
    		out[ViveButtons.BUTTON_RIGHT_TRIGGER.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_RIGHT_TRIGGER, "key.attack");
    		out[ViveButtons.BUTTON_RIGHT_TRIGGER_FULLCLICK.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_RIGHT_TRIGGER_FULLCLICK, "none");
    		out[ViveButtons.BUTTON_RIGHT_GRIP.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_RIGHT_GRIP, "key.pickItem");
    		out[ViveButtons.BUTTON_RIGHT_APPMENU.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_RIGHT_APPMENU, "key.drop");
    		out[ViveButtons.BUTTON_RIGHT_TOUCHPAD_BL.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_RIGHT_TOUCHPAD_BL, "key.use");
    		out[ViveButtons.BUTTON_RIGHT_TOUCHPAD_BR.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_RIGHT_TOUCHPAD_BR, "key.use");
    		out[ViveButtons.BUTTON_RIGHT_TOUCHPAD_UL.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_RIGHT_TOUCHPAD_UL, "key.use");
    		out[ViveButtons.BUTTON_RIGHT_TOUCHPAD_UR.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_RIGHT_TOUCHPAD_UR, "key.use");
    		out[ViveButtons.BUTTON_LEFT_TRIGGER.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_LEFT_TRIGGER, "key.forward");
    		out[ViveButtons.BUTTON_LEFT_TRIGGER_FULLCLICK.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_LEFT_TRIGGER_FULLCLICK, "key.sprint");
    		out[ViveButtons.BUTTON_LEFT_GRIP.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_LEFT_GRIP, "key.sneak");
    		out[ViveButtons.BUTTON_LEFT_APPMENU.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_LEFT_APPMENU, "none");
    		out[ViveButtons.BUTTON_LEFT_TOUCHPAD_BL.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_LEFT_TOUCHPAD_BL, "key.jump");
    		out[ViveButtons.BUTTON_LEFT_TOUCHPAD_BR.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_LEFT_TOUCHPAD_BR, "key.jump");
    		out[ViveButtons.BUTTON_LEFT_TOUCHPAD_UL.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_LEFT_TOUCHPAD_UL, "key.inventory");
    		out[ViveButtons.BUTTON_LEFT_TOUCHPAD_UR.ordinal()] = new VRControllerButtonMapping(ViveButtons.BUTTON_LEFT_TOUCHPAD_UR, "key.inventory");
    		//touch
    		out[ViveButtons.OCULUS_RIGHT_INDEX_TRIGGER_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_INDEX_TRIGGER_PRESS, "key.attack");
    		out[ViveButtons.OCULUS_RIGHT_INDEX_TRIGGER_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_INDEX_TRIGGER_TOUCH, "none");
    		out[ViveButtons.OCULUS_RIGHT_A_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_A_PRESS, "key.use");
    		out[ViveButtons.OCULUS_RIGHT_B_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_B_PRESS, "key.drop");
    		out[ViveButtons.OCULUS_RIGHT_A_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_A_TOUCH, "none");
    		out[ViveButtons.OCULUS_RIGHT_B_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_B_TOUCH, "none");
    		out[ViveButtons.OCULUS_RIGHT_HAND_TRIGGER_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_HAND_TRIGGER_PRESS, "key.pickItem");
    		out[ViveButtons.OCULUS_RIGHT_HAND_TRIGGER_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_HAND_TRIGGER_TOUCH, "none");
    		out[ViveButtons.OCULUS_RIGHT_STICK_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_STICK_PRESS, "key.inventory");
    		out[ViveButtons.OCULUS_RIGHT_STICK_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_STICK_TOUCH, "none");
    		//out[ViveButtons.OCULUS_RIGHT_THUMBPAD_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_RIGHT_THUMBPAD_TOUCH, "none");
    		out[ViveButtons.OCULUS_LEFT_INDEX_TRIGGER_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_INDEX_TRIGGER_PRESS, "key.forward");
    		out[ViveButtons.OCULUS_LEFT_INDEX_TRIGGER_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_INDEX_TRIGGER_TOUCH, "none");
    		out[ViveButtons.OCULUS_LEFT_X_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_X_PRESS, "key.chat");
    		out[ViveButtons.OCULUS_LEFT_Y_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_Y_PRESS, "none");
    		out[ViveButtons.OCULUS_LEFT_X_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_X_TOUCH, "none");
    		out[ViveButtons.OCULUS_LEFT_Y_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_Y_TOUCH, "none");
    		out[ViveButtons.OCULUS_LEFT_HAND_TRIGGER_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_HAND_TRIGGER_PRESS, "key.playerlist");
    		out[ViveButtons.OCULUS_LEFT_HAND_TRIGGER_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_HAND_TRIGGER_TOUCH, "none");
    		out[ViveButtons.OCULUS_LEFT_STICK_PRESS.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_STICK_PRESS, "key.sprint");
    		out[ViveButtons.OCULUS_LEFT_STICK_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_STICK_TOUCH, "none");
    		out[ViveButtons.OCULUS_LEFT_STICK_LEFT.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_STICK_LEFT, "Rotate Left");
    		out[ViveButtons.OCULUS_LEFT_STICK_RIGHT.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_STICK_RIGHT, "Rotate Right");
    		out[ViveButtons.OCULUS_LEFT_STICK_UP.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_STICK_UP, "key.jump");
    		out[ViveButtons.OCULUS_LEFT_STICK_DOWN.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_STICK_DOWN, "key.sneak");
  	 		
    		
    		//out[ViveButtons.OCULUS_LEFT_THUMBPAD_TOUCH.ordinal()] = new VRControllerButtonMapping(ViveButtons.OCULUS_LEFT_THUMBPAD_TOUCH, "none");
    	return out;

    }
    
}

