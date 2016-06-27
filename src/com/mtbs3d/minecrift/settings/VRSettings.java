/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.settings;

import java.io.*;
import java.util.ArrayList;
import java.util.SortedSet;

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
import net.minecraft.util.MathHelper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

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
    public static final int VR_COMFORT_TRANS_BLANKING_MODE_OFF = 0;
    public static final int VR_COMFORT_TRANS_BLANKING_MODE_FADE = 1;
    public static final int VR_COMFORT_TRANS_BLANKING_MODE_BLANK = 2;
    public static final int VR_COMFORT_OFF = 0;
    public static final int VR_COMFORT_YAW = 1;
    public static final int VR_COMFORT_PITCH = 2;
    public static final int VR_COMFORT_PITCHANDYAW = 3;
  
    public static final int MIRROR_OFF = 0;
    public static final int MIRROR_ON_ONE_THIRD_FRAME_RATE = 1;
    public static final int MIRROR_ON_FULL_FRAME_RATE = 2;
    public static final int MIRROR_ON_ONE_THIRD_FRAME_RATE_SINGLE_VIEW = 3;
    public static final int MIRROR_ON_FULL_FRAME_RATE_SINGLE_VIEW = 4;

    public static final int NO_SHADER = -1;

    public int version = UNKNOWN_VERSION;
    public boolean firstLoad = true;
    public boolean newlyCreated = true;
    public boolean useVRRenderer  = false; //default to false
    public boolean debugPose = false;
    public boolean debugPos = false;
	protected float playerEyeHeight = 1.74f;  // Use getPlayerEyeHeight()
    public float eyeReliefAdjust = 0f;
	public float neckBaseToEyeHeight = 0.01f;
    public float movementSpeedMultiplier = 1.0f;   // VIVE - use full speed by default
    public float strafeSpeedMultiplier = 0.33f;
    public boolean useDistortion = true;
    public boolean loadMumbleLib = true;
    protected float leftHalfIpd = 0.032f;    // Use getIPD(eye), hence protected
    protected float rightHalfIpd = 0.032f;
    protected float oculusProfileLeftHalfIpd = leftHalfIpd;
    protected float oculusProfileRightHalfIpd = rightHalfIpd;
    public float hudOpacity = 0.95f;
    public boolean menuBackground = false;
    public boolean renderHeadWear = false;
    public int renderFullFirstPersonModelMode = RENDER_FIRST_PERSON_HAND;   // VIVE - hand only by default
    public int shaderIndex = NO_SHADER;
    public float renderPlayerOffset = 0.2f;
    public boolean testTimewarp = false;
    public boolean useTimewarp = true;
    public boolean useTimewarpJitDelay = false;
    public boolean useVignette = true;
    public boolean useLowPersistence = true;
    public boolean useDynamicPrediction = true;
    public float   renderScaleFactor = 1.0f;
    public int displayMirrorMode = MIRROR_ON_FULL_FRAME_RATE_SINGLE_VIEW;
    public boolean usePositionalTimewarp = true;
    public boolean useDisplayOverdrive = true;
    public boolean useHighQualityDistortion = true;
    public boolean posTrackBlankOnCollision = true;
    public boolean walkUpBlocks = true;     // VIVE default to enable climbing
    public float   menuCrosshairScale = 1f;
    public boolean useCrosshairOcclusion = false;
    public boolean maxCrosshairDistanceAtBlockReach = false;
    public boolean useMaxFov = false;
    public boolean chatFadeAway = true;
    public boolean simulateFalling = false;  // VIVE if HMD is over empty space, fall
    public boolean weaponCollision = true;  // VIVE weapon hand collides with blocks/enemies

    // TODO: Clean-up all the redundant crap!
    public boolean useDistortionTextureLookupOptimisation = false;
    public boolean useFXAA = false;
    public float hudScale = 1.5f;
    public boolean allowMousePitchInput = false;
    public float hudDistance = 1.25f;
    public float hudPitchOffset = -2f;
    public float hudYawOffset = 0.0f;
    public boolean hudLockToHead = false;
    public boolean floatInventory = true; //false not working yet, have to account for rotation and tilt in MCOpenVR>processGui()
    public float fovChange = 0f;
    public float lensSeparationScaleFactor = 1.0f;
    private IOculusRift.AspectCorrectionType aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_AUTO;
    private int aspectRatioCorrection = aspectRatioCorrectionMode.getValue();
    protected float headTrackSensitivity = 1.0f;
    public boolean useFsaa = false;   // default to off
    public float fsaaScaleFactor = 1.4f;
    public boolean useOculusProfileIpd = true;
    public boolean useHalfIpds = false;
 	public String headPositionPluginID   = "openvr";
	public String headTrackerPluginID    = "openvr";
	public String hmdPluginID            = "openvr";
    public String stereoProviderPluginID = "openvr";
    public String badStereoProviderPluginID = "";
	public String controllerPluginID = "openvr";    // VIVE use openVR for controller
    public float crosshairScale = 1.0f;
    public int renderInGameCrosshairMode = RENDER_CROSSHAIR_MODE_ALWAYS;
    public int renderBlockOutlineMode = RENDER_BLOCK_OUTLINE_MODE_ALWAYS;
    public boolean showEntityOutline = false;
    public boolean crosshairRollsWithHead = false;
    public boolean crosshairScalesWithDistance = false;
    public boolean hudOcclusion = false;
    public boolean soundOrientWithHead = true;
	public float chatOffsetX = 0;
	public float chatOffsetY = 0.4f;
    public int inertiaFactor = INERTIA_NORMAL;
    public boolean allowPitchAffectsHeightWhileFlying = true;
    public boolean storeDebugAim = false;
    public int useVrComfort = VR_COMFORT_YAW;
    public boolean allowForwardPlusStrafe = true;
    public boolean vrComfortTransitionLinear = false;
    public float movementAccelerationScaleFactor = 1f;
    public float vrComfortTransitionTimeSecs = 0.150f;
    public float vrComfortTransitionAngleDegs = 30f;
    public int vrComfortTransitionBlankingMode = VR_COMFORT_TRANS_BLANKING_MODE_OFF;
    public boolean mouseKeyholeTight = false;
    public int smoothRunTickCount = 20;
    public boolean smoothTick = false;
    public static final String LEGACY_OPTIONS_VR_FILENAME = "optionsvr.txt";
    public boolean allowAvatarIK = false;
    public boolean hideGui = false;     // VIVE show gui
    public boolean useKeyBindingForComfortYaw = false;

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
    public float vrWorldRotationIncrement = 45f;
    
    private Minecraft mc;

    private File optionsVRFile;
    private File optionsVRBackupFile;
    
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

                    if (optionTokens[0].equals("newlyCreated"))
                    {
                        this.newlyCreated = optionTokens[1].equals("true");
                    }

//                    if (optionTokens[0].equals("firstLoad"))
//                    {
//                        this.firstLoad = optionTokens[1].equals("true");
//                    }

                    if (optionTokens[0].equals("useVRRenderer"))
                    {
                        this.useVRRenderer = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("debugPose"))
                    {
                        this.debugPose = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("playerEyeHeight"))
                    {
                        this.playerEyeHeight = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("neckBaseToEyeHeight"))
                    {
                        this.neckBaseToEyeHeight = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("eyeReliefAdjust"))
                    {
                        this.eyeReliefAdjust = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("leftHalfIpd"))
                    {
                        this.leftHalfIpd = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("rightHalfIpd"))
                    {
                        this.rightHalfIpd = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("headTrackerPluginID"))
                    {
                        this.headTrackerPluginID = optionTokens[1];
                    }

                    if (optionTokens[0].equals("headPositionPluginID"))
                    {
                        this.headPositionPluginID = optionTokens[1];
                    }

                    if (optionTokens[0].equals("hmdPluginID"))
                    {
                        this.hmdPluginID = optionTokens[1];
                    }

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

                    if (optionTokens[0].equals("controllerPluginID"))
                    {
                        this.controllerPluginID = optionTokens[1];
                    }

                    if (optionTokens[0].equals("hudOpacity"))
                    {
                        this.hudOpacity = this.parseFloat(optionTokens[1]);
                        if(hudOpacity< 0.15f)
                        	hudOpacity = 1.0f;
                    }

                    if (optionTokens[0].equals("useDistortion"))
                    {
                        this.useDistortion = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("loadMumbleLib"))
                    {
                        this.loadMumbleLib = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("renderHeadWear"))
                    {
                        this.renderHeadWear = optionTokens[1].equals("true");
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

                    if (optionTokens[0].equals("useTimewarp"))
                    {
                        this.useTimewarp = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useTimewarpJitDelay"))
                    {
                        this.useTimewarpJitDelay = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useVignette"))
                    {
                        this.useVignette = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("posTrackBlankOnCollision"))
                    {
                        this.posTrackBlankOnCollision = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("walkUpBlocks"))
                    {
                        this.walkUpBlocks = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("allowPitchAffectsHeightWhileFlying"))
                    {
                        this.allowPitchAffectsHeightWhileFlying = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useLowPersistence"))
                    {
                        this.useLowPersistence = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useDynamicPrediction"))
                    {
                        this.useDynamicPrediction = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useDisplayOverdrive"))
                    {
                        this.useDisplayOverdrive = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("displayMirrorMode"))
                    {
                        this.displayMirrorMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("useDistortionTextureLookupOptimisation"))
                    {
                        this.useDistortionTextureLookupOptimisation = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useFXAA"))
                    {
                        this.useFXAA = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudScale"))
                    {
                        this.hudScale = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderPlayerOffset"))
                    {
                        this.renderPlayerOffset = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderScaleFactor"))
                    {
                        this.renderScaleFactor = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("allowMousePitchInput"))
                    {
                        this.allowMousePitchInput = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudLockToHead"))
                    {
                        this.hudLockToHead = optionTokens[1].equals("true");
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

                    if (optionTokens[0].equals("useHighQualityDistortion"))
                    {
                        this.useHighQualityDistortion = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("fsaaScaleFactor"))
                    {
                        this.fsaaScaleFactor = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("fovChange"))
                    {
                        this.fovChange = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("lensSeparationScaleFactor"))
                    {
                        this.lensSeparationScaleFactor = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("aspectRatioCorrection"))
                    {
                        this.aspectRatioCorrection = Integer.parseInt(optionTokens[1]);
                        setAspectCorrectionMode(this.aspectRatioCorrection);
                    }

                    if (optionTokens[0].equals("headTrackSensitivity"))
                    {
                        this.headTrackSensitivity = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("movementSpeedMultiplier"))
                    {
                        this.movementSpeedMultiplier = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("strafeSpeedMultiplier"))
                    {
                        this.strafeSpeedMultiplier = this.parseFloat(optionTokens[1]);
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

                    if (optionTokens[0].equals("useOculusProfileIpd"))
                    {
                        this.useOculusProfileIpd = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useHalfIpds"))
                    {
                        this.useHalfIpds = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("renderInGameCrosshairMode"))
                    {
                        this.renderInGameCrosshairMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("renderBlockOutlineMode"))
                    {
                        this.renderBlockOutlineMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("showEntityOutline"))
                    {
                        this.showEntityOutline = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("crosshairRollsWithHead"))
                    {
                        this.crosshairRollsWithHead = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("crosshairScalesWithDistance"))
                    {
                        this.crosshairScalesWithDistance = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("hudOcclusion"))
                    {
                        this.hudOcclusion = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useCrosshairOcclusion"))
                    {
                        this.useCrosshairOcclusion = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("maxCrosshairDistanceAtBlockReach"))
                    {
                        this.maxCrosshairDistanceAtBlockReach = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useMaxFov"))
                    {
                        this.useMaxFov = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("chatFadeAway"))
                    {
                        this.chatFadeAway = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("soundOrientWithHead"))
                    {
                        this.soundOrientWithHead = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("mouseKeyholeTight"))
                    {
                        this.mouseKeyholeTight = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("chatOffsetX"))
                    {
                        this.chatOffsetX = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("chatOffsetY"))
                    {
                        this.chatOffsetY = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("inertiaFactor"))
                    {
                        this.inertiaFactor = Integer.parseInt(optionTokens[1]);
                    }

             
                    if (optionTokens[0].equals("oculusProfileLeftHalfIpd"))
                    {
                        this.oculusProfileLeftHalfIpd = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("oculusProfileRightHalfIpd"))
                    {
                        this.oculusProfileRightHalfIpd = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("useVrComfort"))
                    {
                        this.useVrComfort = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("allowForwardPlusStrafe"))
                    {
                        this.allowForwardPlusStrafe = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("useKeyBindingForComfortYaw"))
                    {
                        this.useKeyBindingForComfortYaw = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("vrComfortTransitionLinear"))
                    {
                        this.vrComfortTransitionLinear = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("movementAccelerationScaleFactor"))
                    {
                        this.movementAccelerationScaleFactor = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("vrComfortTransitionTimeSecs"))
                    {
                        this.vrComfortTransitionTimeSecs = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("vrComfortTransitionAngleDegs"))
                    {
                        this.vrComfortTransitionAngleDegs = this.parseFloat(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("vrComfortTransitionBlankingMode"))
                    {
                        this.vrComfortTransitionBlankingMode = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("smoothRunTickCount"))
                    {
                        this.smoothRunTickCount = Integer.parseInt(optionTokens[1]);
                    }

                    if (optionTokens[0].equals("smoothTick"))
                    {
                        this.smoothTick = optionTokens[1].equals("true");
                    }

                    if (optionTokens[0].equals("allowAvatarIK"))
                    {
                        this.allowAvatarIK = optionTokens[1].equals("true");
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
                    if (optionTokens[0].startsWith("BUTTON_"))
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
		for (int i = 0; i < 16;i++){
			VRControllerButtonMapping vb = buttonMappings[i];

			if(vb==null) { //shouldnt
		        vb = new VRControllerButtonMapping(ViveButtons.values()[i],"none");
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
	        case USE_VR:
	            return var4 + "ON"; // Always ON - this is Minecrift after all
            case EYE_RELIEF_ADJUST:
                return var4 + String.format("%.2fmm", new Object[] { Float.valueOf(this.eyeReliefAdjust) * 1000f });
	        case NECK_LENGTH:
	            return var4 + String.format("%.3fm", new Object[] { Float.valueOf(this.neckBaseToEyeHeight) });
	        case MOVEMENT_MULTIPLIER:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.movementSpeedMultiplier) });
            case STRAFE_MULTIPLIER:
                return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.strafeSpeedMultiplier) });
	        case USE_DISTORTION:
	            return this.useDistortion ? var4 + "ON" : var4 + "OFF";
            case LOAD_MUMBLE_LIB:
                return this.loadMumbleLib ? var4 + "YES" : var4 + "NO";
            case USE_PROFILE_IPD:
                return this.useOculusProfileIpd ? var4 + "Profile" : var4 + "Manual";
            case CONFIG_IPD_MODE:
                return this.useHalfIpds ? var4 + "Per Eye" : var4 + "Both";
	        case TOTAL_IPD:
	            return var4 + String.format("%.1fmm", new Object[] { Float.valueOf(getIPD() * 1000) });
            case LEFT_HALF_IPD:
                return var4 + String.format("%.1fmm", new Object[] { Float.valueOf(getHalfIPD(EyeType.ovrEye_Left) * 1000) });
            case RIGHT_HALF_IPD:
                return var4 + String.format("%.1fmm", new Object[] { Float.valueOf(getHalfIPD(EyeType.ovrEye_Right) * 1000) });
	        case HUD_OPACITY:
	        	if( this.hudOpacity > 0.99)
	        		return var4 + "Opaque";
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudOpacity) });
	        case RENDER_OWN_HEADWEAR:
	            return this.renderHeadWear ? var4 + "ON" : var4 + "OFF";
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
	        case CHROM_AB_CORRECTION:
	            return var4 + "ON";
            // 0.4.0
            case TIMEWARP:
                return this.useTimewarp ? var4 + "ON" : var4 + "OFF";
            case TIMEWARP_JIT_DELAY:
                return this.useTimewarpJitDelay ? var4 + "ON" : var4 + "OFF";
            case VIGNETTE:
                return this.useVignette ? var4 + "ON" : var4 + "OFF";
            case LOW_PERSISTENCE:
                return this.useLowPersistence ? var4 + "ON" : var4 + "OFF";
            case DYNAMIC_PREDICTION:
                return this.useDynamicPrediction ? var4 + "ON" : var4 + "OFF";
            case OVERDRIVE_DISPLAY:
                return this.useDisplayOverdrive ? var4 + "ON" : var4 + "OFF";
            //case ENABLE_DIRECT:
            //    return this.mc.isDirectMode ? var4 + "Direct" : var4 + "Extended";
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
                }
            case POS_TRACK_HIDE_COLLISION:
                return this.posTrackBlankOnCollision ? var4 + "YES" : var4 + "NO";
            case WALK_UP_BLOCKS:
                return this.walkUpBlocks ? var4 + "YES" : var4 + "NO";
            case PITCH_AFFECTS_FLYING:
                return this.allowPitchAffectsHeightWhileFlying ? var4 + "YES" : var4 + "NO";
            case VIEW_BOBBING:
                return this.mc.gameSettings.viewBobbing ? var4 + "YES" : var4 + "NO";
            case RENDER_SCALEFACTOR:
                return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.renderScaleFactor) });

            case TEXTURE_LOOKUP_OPT:
                return this.useDistortionTextureLookupOptimisation ? var4 + "Texture Lookup" : var4 + "Brute Force";
            case FXAA:
                return this.useFXAA ? var4 + "ON" : var4 + "OFF";
	        case HUD_SCALE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudScale) });
	        case RENDER_PLAYER_OFFSET:
	            if (this.renderPlayerOffset < 0.01f)
	                return var4 + "None";
	            else
	                return var4 + String.format("%.2fcm", new Object[] { Float.valueOf(this.renderPlayerOffset) });
            case MONO_FOV:
                if(this.mc.gameSettings.fovSetting==110f)
                    return var4 + "Quake Pro";
                else if(this.mc.gameSettings.fovSetting==70f)
                    return var4 + "Normal";
                else
                    return var4 + String.format("%.0f°", new Object[] { Float.valueOf(this.mc.gameSettings.fovSetting) });
	        case PITCH_AFFECTS_CAMERA:
	            return this.allowMousePitchInput ? var4 + "ON" : var4 + "OFF";
            case HUD_LOCK_TO:
                return this.hudLockToHead ? var4 + "Head" : var4 + "Hand";      // VIVE - lock to hand instead of body
	        case HUD_DISTANCE:
	            return var4 + String.format("%.2f", new Object[] { Float.valueOf(this.hudDistance) });
	        case HUD_PITCH:
	            return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.hudPitchOffset) });
            case HUD_YAW:
                return var4 + String.format("%.0f", new Object[] { Float.valueOf(this.hudYawOffset) });
	        case FOV_CHANGE:
	            return var4 + String.format("%.1f%s", new Object[] { Float.valueOf(this.fovChange), DEGREE });
            case LENS_SEPARATION_SCALE_FACTOR:
                return var4 + String.format("%.3f", new Object[] { Float.valueOf(this.lensSeparationScaleFactor) });
	        case FSAA:
	            return this.useFsaa ? var4 + "ON" : var4 + "OFF";
            case HIGH_QUALITY_DISTORTION:
                return this.useHighQualityDistortion ? var4 + "ON" : var4 + "OFF";
	        case FSAA_SCALEFACTOR:
	            return var4 + String.format("%.1fX", new Object[] { Float.valueOf(this.fsaaScaleFactor * this.fsaaScaleFactor) });
            case ASPECT_RATIO_CORRECTION:
                if (this.aspectRatioCorrection == IOculusRift.AspectCorrectionType.CORRECTION_16_10_TO_16_9.getValue())
                    return var4 + "16:10->16:9";
                else if (this.aspectRatioCorrection == IOculusRift.AspectCorrectionType.CORRECTION_16_9_TO_16_10.getValue())
                    return var4 + "16:9->16:10";
                else if (this.aspectRatioCorrection == IOculusRift.AspectCorrectionType.CORRECTION_AUTO.getValue())
                    return var4 + "Auto";
                else
                    return var4 + "None";
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
	        case CROSSHAIR_ROLL:
	            return this.crosshairRollsWithHead ? var4 + "With Head" : var4 + "With HUD";
            case CROSSHAIR_SCALES_WITH_DISTANCE:
                return this.crosshairScalesWithDistance ? var4 + "Distance" : var4 + "Static";
	        case HUD_OCCLUSION:
	            return this.hudOcclusion ? var4 + "ON" : var4 + "OFF";
            case CROSSHAIR_OCCLUSION:
                return this.useCrosshairOcclusion ? var4 + "ON" : var4 + "OFF";
            case MAX_CROSSHAIR_DISTANCE_AT_BLOCKREACH:
                return this.maxCrosshairDistanceAtBlockReach ? var4 + "Reach" : var4 + "Far";
            case MAX_FOV:
                return this.useMaxFov ? var4 + "Max" : var4 + "Default";
            case CHAT_FADE_AWAY:
                return this.chatFadeAway ? var4 + "Fades" : var4 + "Stays";
	        case SOUND_ORIENT:
	            return this.soundOrientWithHead ? var4 + "Headphones" : var4 + "Speakers";
            case MOUSE_AIM_TYPE:
                return this.mouseKeyholeTight ? var4 + "Keyhole (tight)" : var4 + "Keyhole (loose)";
	        case CHAT_OFFSET_X:
	            return var4 + String.format("%.0f%%", new Object[] { Float.valueOf(100*this.chatOffsetX) });
	        case CHAT_OFFSET_Y:
	            return var4 + String.format("%.0f%%", new Object[] { Float.valueOf(100*this.chatOffsetY) });
            case INERTIA_FACTOR:
                if (this.inertiaFactor == INERTIA_NONE)
                    return var4 + "Automan";
                else if (this.inertiaFactor == INERTIA_NORMAL)
                    return var4 + "Normal";
                else if (this.inertiaFactor == INERTIA_LARGE)
                    return var4 + "A lot";
                else if (this.inertiaFactor == INERTIA_MASSIVE)
                    return var4 + "Even more";
            case USE_VR_COMFORT:
                switch( this.useVrComfort )
                {
                    case VR_COMFORT_OFF:
                        return var4 + "OFF";
                    case VR_COMFORT_YAW:
                        return var4 + "Yaw Only";
                    case VR_COMFORT_PITCH:
                        return var4 + "Pitch Only";
                    case VR_COMFORT_PITCHANDYAW:
                        return var4 + "Yaw And Pitch";
                };
            case ALLOW_FORWARD_PLUS_STRAFE:
                return this.allowForwardPlusStrafe ? var4 + "Allow" : var4 + "Disallow";
            case VR_COMFORT_USE_KEY_BINDING_FOR_YAW:
                return this.useKeyBindingForComfortYaw ? var4 + "Key" : var4 + "Crosshair";
            case VR_COMFORT_TRANSITION_LINEAR:
                return this.vrComfortTransitionLinear ? var4 + "Linear" : var4 + "Sinusoidal";
            case MOVEMENT_ACCELERATION_SCALE_FACTOR:
                return var4 + String.format("%.1f", new Object[] { Float.valueOf(this.movementAccelerationScaleFactor) });
            case VR_COMFORT_TRANSITION_TIME_SECS:
                if (vrComfortTransitionTimeSecs < 0.01f)
                    return var4 + "Instant";
                return var4 + String.format("%.1f ms", new Object[] { Float.valueOf(this.vrComfortTransitionTimeSecs * 1000) });
            case VR_COMFORT_TRANSITION_ANGLE_DEGS:
                return var4 + String.format("%.0f°", new Object[] { Float.valueOf(this.vrComfortTransitionAngleDegs) });
            case VR_COMFORT_TRANSITION_BLANKING_MODE:
                if (this.vrComfortTransitionBlankingMode == VR_COMFORT_TRANS_BLANKING_MODE_BLANK)
                    return var4 + "Black";
                else if (this.vrComfortTransitionBlankingMode == VR_COMFORT_TRANS_BLANKING_MODE_FADE)
                    return var4 + "Blink";
                else if (this.vrComfortTransitionBlankingMode == VR_COMFORT_TRANS_BLANKING_MODE_OFF)
                        return var4 + "None";
                // VIVE START - new options
            case SIMULATE_FALLING:
                return this.simulateFalling ? var4 + "ON" : var4 + "OFF";
            case WEAPON_COLLISION:
                return this.weaponCollision ? var4 + "ON" : var4 + "OFF";
                // VIVE END - new options
                //JRBUDDA
            case ALLOW_MODE_SWITCH:
                return this.vrAllowLocoModeSwotch ? var4 + "ON" : var4 + "OFF";     
            case FREE_MOVE_DEFAULT:
                return this.vrFreeMove ? var4 + "ON" : var4 + "OFF";       
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
                //END JRBUDDA
 	        default:
	        	return "";
        }
    }

    public float getOptionFloatValue(VRSettings.VrOptions par1EnumOptions)
    {
    	switch( par1EnumOptions ) {
            case EYE_RELIEF_ADJUST:
                return this.eyeReliefAdjust;
			case NECK_LENGTH :
				return this.neckBaseToEyeHeight ;
			case MOVEMENT_MULTIPLIER :
				return this.movementSpeedMultiplier ;
            case STRAFE_MULTIPLIER :
                return this.strafeSpeedMultiplier ;
			case TOTAL_IPD:
				return getIPD();
            case LEFT_HALF_IPD:
                return getHalfIPD(EyeType.ovrEye_Left) ;
            case RIGHT_HALF_IPD:
                return getHalfIPD(EyeType.ovrEye_Right) ;
			case HUD_SCALE :
				return this.hudScale ;
			case HUD_OPACITY :
				return this.hudOpacity ;
			case RENDER_PLAYER_OFFSET :
				return this.renderPlayerOffset ;
            case MONO_FOV:
                return this.mc.gameSettings.fovSetting;
            case RENDER_SCALEFACTOR:
                return this.renderScaleFactor;
			case HUD_DISTANCE :
				return this.hudDistance ;
			case HUD_PITCH :
				return this.hudPitchOffset ;
            case HUD_YAW :
                return this.hudYawOffset ;
			case FOV_CHANGE:
				return this.fovChange;
            case LENS_SEPARATION_SCALE_FACTOR:
                return this.lensSeparationScaleFactor ;
			case FSAA_SCALEFACTOR:
				return this.fsaaScaleFactor;
			case CROSSHAIR_SCALE :
				return this.crosshairScale ;
            case MENU_CROSSHAIR_SCALE :
                return this.menuCrosshairScale ;
			case CHAT_OFFSET_X:
				return this.chatOffsetX;
			case CHAT_OFFSET_Y:
				return this.chatOffsetY;
            case MOVEMENT_ACCELERATION_SCALE_FACTOR:
                return movementAccelerationScaleFactor;
            case VR_COMFORT_TRANSITION_TIME_SECS:
                return vrComfortTransitionTimeSecs;
            case VR_COMFORT_TRANSITION_ANGLE_DEGS:
                return vrComfortTransitionAngleDegs;
            // VIVE START - new options
            case WORLD_SCALE:
            	if(vrWorldScale < 1.5){
            		return -1/vrWorldScale  + 10 ;
            	} else if (vrWorldScale == 1.5f){
            		return 10;  
            	} else {
            		return(vrWorldScale/2 + 10);            		
            	}
            case WORLD_ROTATION:
                return vrWorldRotation;
            case WORLD_ROTATION_INCREMENT:
            	if(vrWorldRotationIncrement == 10f) return 0;
            	if(vrWorldRotationIncrement == 36f) return 1;            	
            	if(vrWorldRotationIncrement == 45f) return 2;
            	if(vrWorldRotationIncrement == 90f) return 3;
            	if(vrWorldRotationIncrement == 180f) return 4;
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
	        case USE_VR:
	            this.useVRRenderer = !this.useVRRenderer;
	            //mc.setUseVRRenderer(useVRRenderer);      // TODO:
	            break;
	        case USE_DISTORTION:
	            this.useDistortion = !this.useDistortion;
	            break;
            case LOAD_MUMBLE_LIB:
                this.loadMumbleLib = !this.loadMumbleLib;
                break;
	        case RENDER_OWN_HEADWEAR:
	            this.renderHeadWear = !this.renderHeadWear;
	            break;
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
            case TIMEWARP:
                this.useTimewarp = !this.useTimewarp;
                break;
            case TIMEWARP_JIT_DELAY:
                this.useTimewarpJitDelay = !this.useTimewarpJitDelay;
                break;
            case VIGNETTE:
                this.useVignette = !this.useVignette;
                break;
            case LOW_PERSISTENCE:
                this.useLowPersistence = !this.useLowPersistence;
                break;
            case DYNAMIC_PREDICTION:
                this.useDynamicPrediction = !this.useDynamicPrediction;
                break;
            case OVERDRIVE_DISPLAY:
                this.useDisplayOverdrive = !this.useDisplayOverdrive;
                break;
            case MIRROR_DISPLAY:
                this.displayMirrorMode++;
                if (this.displayMirrorMode > MIRROR_ON_FULL_FRAME_RATE_SINGLE_VIEW)
                    this.displayMirrorMode = MIRROR_OFF;
                break;
            case POS_TRACK_HIDE_COLLISION:
                this.posTrackBlankOnCollision = !this.posTrackBlankOnCollision;
                break;
            case WALK_UP_BLOCKS:
                this.walkUpBlocks = !this.walkUpBlocks;
                break;
            case PITCH_AFFECTS_FLYING:
                this.allowPitchAffectsHeightWhileFlying = !this.allowPitchAffectsHeightWhileFlying;
                break;
            case VIEW_BOBBING:
                this.mc.gameSettings.viewBobbing = !this.mc.gameSettings.viewBobbing;
                break;
            case TEXTURE_LOOKUP_OPT:
                this.useDistortionTextureLookupOptimisation = !this.useDistortionTextureLookupOptimisation;
                break;
            case FXAA:
                this.useFXAA = !this.useFXAA;
                break;
	        case PITCH_AFFECTS_CAMERA:
	            this.allowMousePitchInput = !this.allowMousePitchInput;
	            break;
            case HUD_LOCK_TO:
                this.hudLockToHead = !this.hudLockToHead;
                break;
	        case FSAA:
	            this.useFsaa = !this.useFsaa;
	            break;
            case HIGH_QUALITY_DISTORTION:
                this.useHighQualityDistortion = !this.useHighQualityDistortion;
                break;
	        case ASPECT_RATIO_CORRECTION:
	            this.aspectRatioCorrection += 1;
	            if (this.aspectRatioCorrection > IOculusRift.AspectCorrectionType.CORRECTION_AUTO.getValue())
	                this.aspectRatioCorrection = IOculusRift.AspectCorrectionType.CORRECTION_NONE.getValue();

                setAspectCorrectionMode(this.aspectRatioCorrection);
	            break;
	        case USE_PROFILE_IPD:
	            this.useOculusProfileIpd = !this.useOculusProfileIpd;
	            break;
	        case CONFIG_IPD_MODE:
	            this.useHalfIpds = !this.useHalfIpds;
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
	        case CROSSHAIR_ROLL:
	            this.crosshairRollsWithHead = !this.crosshairRollsWithHead;
	            break;
            case CROSSHAIR_SCALES_WITH_DISTANCE:
                this.crosshairScalesWithDistance = !this.crosshairScalesWithDistance;
                break;
	        case HUD_OCCLUSION:
	            this.hudOcclusion = !this.hudOcclusion;
	            break;
            case CROSSHAIR_OCCLUSION:
                this.useCrosshairOcclusion = !this.useCrosshairOcclusion;
                break;
            case MAX_CROSSHAIR_DISTANCE_AT_BLOCKREACH:
                this.maxCrosshairDistanceAtBlockReach = !this.maxCrosshairDistanceAtBlockReach;
                break;
            case MAX_FOV:
                this.useMaxFov = !this.useMaxFov;
                break;
            case CHAT_FADE_AWAY:
                this.chatFadeAway = !this.chatFadeAway;
	        case SOUND_ORIENT:
	            this.soundOrientWithHead = !this.soundOrientWithHead;
	            break;
            case MOUSE_AIM_TYPE:
                this.mouseKeyholeTight = !this.mouseKeyholeTight;
                break;
            case USE_VR_COMFORT:
                this.useVrComfort += 1;
                if (this.useVrComfort > VR_COMFORT_PITCHANDYAW)
                    this.useVrComfort = VR_COMFORT_OFF;
                break;
            case INERTIA_FACTOR:
                this.inertiaFactor +=1;
                if (this.inertiaFactor > INERTIA_MASSIVE)
                    this.inertiaFactor = INERTIA_NONE;
                break;
            case ALLOW_FORWARD_PLUS_STRAFE:
                this.allowForwardPlusStrafe = !this.allowForwardPlusStrafe;
                break;
            case VR_COMFORT_USE_KEY_BINDING_FOR_YAW:
                this.useKeyBindingForComfortYaw = !this.useKeyBindingForComfortYaw;
                break;
            case VR_COMFORT_TRANSITION_LINEAR:
                this.vrComfortTransitionLinear = !this.vrComfortTransitionLinear;
                break;
            case VR_COMFORT_TRANSITION_BLANKING_MODE:
                this.vrComfortTransitionBlankingMode += 1;
                if (this.vrComfortTransitionBlankingMode > 2)
                    this.vrComfortTransitionBlankingMode = 0;
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
            case FREE_MOVE_DEFAULT:
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
                //JRBUDDA
                
            default:
                    break;
    	}

        this.saveOptions();
    }

    public void setOptionFloatValue(VRSettings.VrOptions par1EnumOptions, float par2)
    {
    	switch( par1EnumOptions ) {
	        case EYE_HEIGHT:
	            setMinecraftPlayerEyeHeight(par2);
	            break;
            case EYE_RELIEF_ADJUST:
                this.eyeReliefAdjust = par2;
                break;
	        case NECK_LENGTH:
	            this.neckBaseToEyeHeight = par2;
	            break;
	        case MOVEMENT_MULTIPLIER:
	            this.movementSpeedMultiplier = par2;
	            break;
            case STRAFE_MULTIPLIER:
                this.strafeSpeedMultiplier = par2;
                break;
            case TOTAL_IPD:
                setIPD(par2);
                break;
	        case LEFT_HALF_IPD:
                setIPD(par2, this.rightHalfIpd);
                break;
            case RIGHT_HALF_IPD:
                setIPD(this.leftHalfIpd, par2);
                break;
	        case HUD_SCALE:
	            this.hudScale = par2;
	        	break;
	        case HUD_OPACITY:
	            this.hudOpacity = par2;
	        	break;
	        case RENDER_PLAYER_OFFSET:
	            this.renderPlayerOffset = par2;
	        	break;
            case MONO_FOV:
                this.mc.gameSettings.fovSetting = par2;
                break;
            case RENDER_SCALEFACTOR:
                this.renderScaleFactor = par2;
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
	        case FOV_CHANGE:
	            this.fovChange = par2;
	        	break;
            case LENS_SEPARATION_SCALE_FACTOR:
                this.lensSeparationScaleFactor = par2;
                break;
	        case FSAA_SCALEFACTOR:
	            this.fsaaScaleFactor = par2;
	        	break;
	        case CROSSHAIR_SCALE:
	            this.crosshairScale = par2;
	        	break;
            case MENU_CROSSHAIR_SCALE:
                this.menuCrosshairScale = par2;
                break;
	        case CHAT_OFFSET_X:
	        	this.chatOffsetX = par2;
	        	break;
	        case CHAT_OFFSET_Y:
	        	this.chatOffsetY = par2;
	        	break;
            case MOVEMENT_ACCELERATION_SCALE_FACTOR:
                this.movementAccelerationScaleFactor = par2;
                break;
            case VR_COMFORT_TRANSITION_TIME_SECS:
                this.vrComfortTransitionTimeSecs = par2;
                break;
            case VR_COMFORT_TRANSITION_ANGLE_DEGS:
                this.vrComfortTransitionAngleDegs = par2;
                break;
            // VIVE START - new options
            case WORLD_SCALE:
            	if(par2 < 10){
            		this.vrWorldScale = 1 / (10-par2);
            	} else if (par2 == 10){
            		this.vrWorldScale = 1.5f;   
            	} else {
            		this.vrWorldScale = (par2 - 10) * 2;            		
            	}
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
            var5.println("useVRRenderer:"+ this.useVRRenderer );
            var5.println("debugPose:"+ this.debugPose );
            var5.println("playerEyeHeight:" + this.playerEyeHeight);
            var5.println("eyeReliefAdjust:" + this.eyeReliefAdjust);
            var5.println("neckBaseToEyeHeight:" + this.neckBaseToEyeHeight );
            var5.println("headTrackerPluginID:"+ this.headTrackerPluginID);
            var5.println("headPositionPluginID:"+ this.headPositionPluginID);
            var5.println("hmdPluginID:"+ this.hmdPluginID);
            var5.println("stereoProviderPluginID:"+ this.stereoProviderPluginID);
            var5.println("badStereoProviderPluginID:"+ this.badStereoProviderPluginID);
            var5.println("controllerPluginID:"+ this.controllerPluginID);
            var5.println("leftHalfIpd:" + this.leftHalfIpd);
            var5.println("rightHalfIpd:" + this.rightHalfIpd);
            var5.println("hudOpacity:" + this.hudOpacity);
            var5.println("useDistortion:" + this.useDistortion);
            var5.println("loadMumbleLib:" + this.loadMumbleLib);
            var5.println("renderHeadWear:" + this.renderHeadWear);
            var5.println("menuBackground:" + this.menuBackground);
            var5.println("renderFullFirstPersonModelMode:" + this.renderFullFirstPersonModelMode);
            var5.println("shaderIndex:" + this.shaderIndex);
            // 0.4.0
            var5.println("useTimewarp:" + this.useTimewarp);
            var5.println("useTimewarpJitDelay:" + this.useTimewarpJitDelay);
            var5.println("useVignette:" + this.useVignette);
            var5.println("useLowPersistence:" + this.useLowPersistence);
            var5.println("useDynamicPrediction:" + this.useDynamicPrediction);
            var5.println("useDisplayOverdrive:" + this.useDisplayOverdrive);
            var5.println("displayMirrorMode:" + this.displayMirrorMode);
            var5.println("posTrackBlankOnCollision:" + this.posTrackBlankOnCollision);
            var5.println("walkUpBlocks:" + this.walkUpBlocks);
            var5.println("allowPitchAffectsHeightWhileFlying:" + this.allowPitchAffectsHeightWhileFlying);
            var5.println("useDistortionTextureLookupOptimisation:" + this.useDistortionTextureLookupOptimisation);
            var5.println("useFXAA:" + this.useFXAA);
            var5.println("hudScale:" + this.hudScale);
            var5.println("renderPlayerOffset:" + this.renderPlayerOffset);
            var5.println("renderScaleFactor:" + this.renderScaleFactor);
            var5.println("allowMousePitchInput:" + this.allowMousePitchInput);
            var5.println("hudLockToHead:" + this.hudLockToHead);
            var5.println("hudDistance:" + this.hudDistance);
            var5.println("hudPitchOffset:" + this.hudPitchOffset);
            var5.println("hudYawOffset:" + this.hudYawOffset);
            var5.println("useFsaa:" + this.useFsaa);
            var5.println("useHighQualityDistortion:" + this.useHighQualityDistortion);
            var5.println("fsaaScaleFactor:" + this.fsaaScaleFactor);
            var5.println("fovChange:" + this.fovChange);
            var5.println("lensSeparationScaleFactor:" + this.lensSeparationScaleFactor);
            var5.println("headTrackSensitivity:" + this.headTrackSensitivity);
            var5.println("movementSpeedMultiplier:" + this.movementSpeedMultiplier);
            var5.println("strafeSpeedMultiplier:" + this.strafeSpeedMultiplier);
            var5.println("aspectRatioCorrection:" + this.aspectRatioCorrection);
            var5.println("renderInGameCrosshairMode:" + this.renderInGameCrosshairMode);
            var5.println("renderBlockOutlineMode:" + this.renderBlockOutlineMode);
            var5.println("showEntityOutline:" + this.showEntityOutline);
            var5.println("crosshairRollsWithHead:" + this.crosshairRollsWithHead);
            var5.println("crosshairScalesWithDistance:" + this.crosshairScalesWithDistance);
            var5.println("hudOcclusion:" + this.hudOcclusion);
            var5.println("useCrosshairOcclusion:" + this.useCrosshairOcclusion);
            var5.println("maxCrosshairDistanceAtBlockReach:" + this.maxCrosshairDistanceAtBlockReach);
            var5.println("useMaxFov:" + this.useMaxFov);
            var5.println("chatFadeAway:" + this.chatFadeAway);
            var5.println("soundOrientWithHead:" + this.soundOrientWithHead);
            var5.println("mouseKeyholeTight:" + this.mouseKeyholeTight);
            var5.println("useOculusProfileIpd:" + this.useOculusProfileIpd);
            var5.println("useHalfIpds:" + this.useHalfIpds);
            var5.println("oculusProfileLeftHalfIpd:" + this.oculusProfileLeftHalfIpd);
            var5.println("oculusProfileRightHalfIpd:" + this.oculusProfileRightHalfIpd);
            var5.println("crosshairScale:" + this.crosshairScale);
            var5.println("menuCrosshairScale:" + this.menuCrosshairScale);
            var5.println("chatOffsetX:" + this.chatOffsetX);
            var5.println("chatOffsetY:" + this.chatOffsetY);
            var5.println("inertiaFactor:" + this.inertiaFactor);
            var5.println("useVrComfort:" + this.useVrComfort);
            var5.println("allowForwardPlusStrafe:" + this.allowForwardPlusStrafe);
            var5.println("useKeyBindingForComfortYaw:" + this.useKeyBindingForComfortYaw);
            var5.println("vrComfortTransitionLinear:" + this.vrComfortTransitionLinear);
            var5.println("movementAccelerationScaleFactor:" + this.movementAccelerationScaleFactor);
            var5.println("vrComfortTransitionTimeSecs:" + this.vrComfortTransitionTimeSecs);
            var5.println("vrComfortTransitionAngleDegs:" + this.vrComfortTransitionAngleDegs);
            var5.println("vrComfortTransitionBlankingMode:" + this.vrComfortTransitionBlankingMode);
            var5.println("smoothRunTickCount:" + this.smoothRunTickCount);
            var5.println("smoothTick:" + this.smoothTick);
            var5.println("allowAvatarIK:" + this.allowAvatarIK);
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
           
            if (buttonMappings == null) resetBindings(); //defaults
            
            for (int i = 0; i<16;i++){
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
    
    public void setMinecraftIpd(float leftHalfIpd, float rightHalfIpd)
    {
        this.leftHalfIpd = Math.abs(leftHalfIpd);
        this.rightHalfIpd = Math.abs(rightHalfIpd);
    }

    public void setMinecraftIpd(float Ipd)
    {
        this.leftHalfIpd = Math.abs(Ipd)/2f;
        this.rightHalfIpd = Math.abs(Ipd)/2f;
    }

    public void setOculusProfileIpd(float leftHalfIpd, float rightHalfIpd)
    {
        this.oculusProfileLeftHalfIpd = Math.abs(leftHalfIpd);
        this.oculusProfileRightHalfIpd = Math.abs(rightHalfIpd);
    }

    public void setOculusProfileIpd(float Ipd)
    {
        this.oculusProfileLeftHalfIpd = Math.abs(Ipd)/2f;
        this.oculusProfileRightHalfIpd = Math.abs(Ipd)/2f;
    }

    public void setIPD(float leftHalfIpd, float rightHalfIpd)
    {
        if (!this.useOculusProfileIpd)
        {
            setMinecraftIpd(leftHalfIpd, rightHalfIpd);
        }
    }

    public void setIPD(float Ipd)
    {
        if (!this.useOculusProfileIpd)
        {
            setMinecraftIpd(Ipd);
        }
    }

    public float getHalfIPD(EyeType eye)
    {
        if (this.useOculusProfileIpd)
        {
            if (eye == EyeType.ovrEye_Center)
                return 0f;
            else if (eye == EyeType.ovrEye_Left)
                return -Math.abs(this.oculusProfileLeftHalfIpd);
            else
                return Math.abs(this.oculusProfileRightHalfIpd);
        }
        else
        {
            if (eye == EyeType.ovrEye_Center)
                return 0f;
            else if (eye == EyeType.ovrEye_Left)
                return -Math.abs(this.leftHalfIpd);
            else
                return Math.abs(this.rightHalfIpd);
        }
    }

    public float getOculusProfileHalfIPD(EyeType eye)
    {
        if (eye == EyeType.ovrEye_Center)
            return 0f;
        else if (eye == EyeType.ovrEye_Left)
            return -Math.abs(this.oculusProfileLeftHalfIpd);
        else
            return Math.abs(this.oculusProfileRightHalfIpd);
    }

    public float getIPD()
    {
        if (this.useOculusProfileIpd)
        {
            return Math.abs(this.oculusProfileLeftHalfIpd) + Math.abs(this.oculusProfileRightHalfIpd);
        }
        else
        {
            return Math.abs(this.leftHalfIpd) + Math.abs(this.rightHalfIpd);
        }
    }

    public void setMinecraftPlayerEyeHeight(float eyeHeight)
    {
        this.playerEyeHeight = eyeHeight;
    }



    /**
     * Parses a string into a float.
     */
    private float parseFloat(String par1Str)
    {
        return par1Str.equals("true") ? 1.0F : (par1Str.equals("false") ? 0.0F : Float.parseFloat(par1Str));
    }

    private void setAspectCorrectionMode(int mode)
    {
        switch(mode)
        {
            case 1:
                this.aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_16_9_TO_16_10;
                break;
            case 2:
                this.aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_16_10_TO_16_9;
                break;
            case 3:
                this.aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_AUTO;
                break;
            default:
                this.aspectRatioCorrectionMode = IOculusRift.AspectCorrectionType.CORRECTION_NONE;
                break;
        }
    }

    public IOculusRift.AspectCorrectionType getAspectRatioCorrectionMode()
    {
        return this.aspectRatioCorrectionMode;
    }

    public void setAspectRatioCorrectionMode(IOculusRift.AspectCorrectionType mode)
    {
        this.aspectRatioCorrection = mode.getValue();
    }

    public void setHeadTrackSensitivity(float value)
    {
        this.headTrackSensitivity = value;
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

    public boolean isComfortYawTransitionKeyAllowed() {
        if ((this.useVrComfort == VR_COMFORT_PITCHANDYAW || this.useVrComfort == VR_COMFORT_YAW) &&
            this.useKeyBindingForComfortYaw) {
            return true;
        }
        return false;
    }

    public static enum VrOptions
    {
        // Minecrift below here

        // TODO: Port to Mark's excellent VROption implementation

        //General
        USE_VR("VR mode", false, true),
        HUD_SCALE("HUD Size", true, false),
        HUD_DISTANCE("HUD Distance", true, false),
        HUD_PITCH("HUD Vertical Offset", true, false),
        HUD_YAW("HUD Horiz. Offset", true, false),
        HUD_LOCK_TO("HUD Orientation Lock", false, true),
        HUD_OPACITY("HUD Opacity", true, false),
        RENDER_MENU_BACKGROUND("Menu Background", false, true),
        HUD_HIDE("Hide HUD (F1)", false, true),
        HUD_OCCLUSION("HUD Occlusion", false, true),
        CROSSHAIR_OCCLUSION("Crosshair Occlusion", false, true),
        MAX_CROSSHAIR_DISTANCE_AT_BLOCKREACH("Max. Crosshair Dist.", false, true),
        MAX_FOV("Use FOV", false, true),
        CHAT_FADE_AWAY("Chat Persistence", false, true),
        SOUND_ORIENT("Sound Source", false, true),
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
        CHAT_OFFSET_X("Chat Offset X",true,false),
        CHAT_OFFSET_Y("Chat Offset Y",true,false),
        LOAD_MUMBLE_LIB("Load Mumble Lib", false, true),

        // Player
        EYE_HEIGHT("Eye Height", true, false),
        EYE_PROTRUSION("Eye Protrusion", true, false),
        EYE_RELIEF_ADJUST("Eye Relief Adjust", true, false),
        NECK_LENGTH("Neck Length", true, false),
        RENDER_OWN_HEADWEAR("Render Own Headwear", false, true),
        RENDER_FULL_FIRST_PERSON_MODEL_MODE("First Person Model", false, true),
        RENDER_PLAYER_OFFSET("View Body Offset", true, false),
        MONO_FOV("FOV", true, false),
        CONFIG_IPD_MODE("Set IPD", false, true),
        USE_PROFILE_PLAYER_HEIGHT("Use Height from", false, true),
        USE_PROFILE_IPD("Use IPD from", false, true),
        TOTAL_IPD("IPD", true, false),
        LEFT_HALF_IPD("Left Half IPD", true, false),
        RIGHT_HALF_IPD("Right Half IPD", true, false),
        //IPD_SCALE("IPD Scale", true, false),
        OCULUS_PROFILE_NAME("Oculus Profile", false, true),
        OCULUS_PROFILE_GENDER("Gender", false, true),

        //HMD/render
        USE_DISTORTION("Distortion", false, true),
        CHROM_AB_CORRECTION("Chrom. Ab. Correction", false, true),
        TIMEWARP("Timewarp", false, true),
        TIMEWARP_JIT_DELAY("Timewarp JIT Delay", false, true),
        VIGNETTE("Vignette", false, true),
        TEXTURE_LOOKUP_OPT("Dist. Method", false, true),
        FXAA("FXAA", false, true),
        FOV_CHANGE("FOV Border Change", true, false),
        LENS_SEPARATION_SCALE_FACTOR("Lens Sep. Scale", true, false),
        ASPECT_RATIO_CORRECTION("Asp. Correction", false, false),
        FSAA("FSAA", false, true),
        HIGH_QUALITY_DISTORTION("HQ Distortion", false, true),
        FSAA_SCALEFACTOR("FSAA", true, false),
        USE_QUATERNIONS("Orient. Mode", false, true),
        DELAYED_RENDER("Render Mode", false, true),
        // SDK 0.4.0 up
        RENDER_SCALEFACTOR("Render Scale", true, false),
        //ENABLE_DIRECT("Render Mode", false, true),
        MIRROR_DISPLAY("Mirror Display", false, true),
        LOW_PERSISTENCE("Low Persistence", false, true),
        DYNAMIC_PREDICTION("Dynamic Prediction", false, true),
        OVERDRIVE_DISPLAY("Overdrive Display", false, true),
        HMD_NAME_PLACEHOLDER("", false, true),
        EYE_RELIEF_PLACEHOLDER("", false, true),

          POS_TRACK_Y_AXIS_DISTANCE_SKEW("Distance Skew Angle", true, false),
        // SDK 0.4.0 up
        POS_TRACK_HIDE_COLLISION("Blank on collision", false, true),
        WALK_UP_BLOCKS("Walk up blocks", false, true),
        VIEW_BOBBING("View Bobbing", false, true),
        PITCH_AFFECTS_FLYING("Pitch Affects Flying", false, true),
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
        USE_VR_COMFORT("VR Comfort", false, true),
        ALLOW_FORWARD_PLUS_STRAFE("Forward + Strafe", false, true),
        VR_COMFORT_USE_KEY_BINDING_FOR_YAW("Trigger Yaw With", false, true),
        VR_COMFORT_TRANSITION_LINEAR("Transition Mode", false, true),
        MOVEMENT_ACCELERATION_SCALE_FACTOR("Player Accel.", true, false),
        VR_COMFORT_TRANSITION_TIME_SECS("Transition Time", true, false),
        VR_COMFORT_TRANSITION_ANGLE_DEGS("Transition Angle", true, false),
        VR_COMFORT_TRANSITION_BLANKING_MODE("Transition Blanking", false, false),

        // VIVE START - new options
        SIMULATE_FALLING("Simulate falling", false, true),
        WEAPON_COLLISION("Weapon collision", false, true),
        // VIVE END - new options

        //JRBUDDA VIVE
        ALLOW_CRAWLING("Allow crawling",false, true),
        ALLOW_MODE_SWITCH("Allow Mode Switch",false, true),
        FREE_MOVE_DEFAULT("Default to Free Move",false, true),
        LIMIT_TELEPORT("Limit TP in Survival",false, true),
        REVERSE_HANDS("Reverse Hands",false, true),
        STENCIL_ON("Use Eye Stencil", false, true), 
        BCB_ON("Show Body Position", false, true),    
        WORLD_SCALE("World Scale", true, false),
        WORLD_ROTATION("World Rotation", true, false),
        WORLD_ROTATION_INCREMENT("Rotation Increment", true, false),
        //END JRBUDDA
        
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

        public float normalizeValue(float p_148266_1_)
        {
            return MathHelper.clamp_float((this.snapToStepClamp(p_148266_1_) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float p_148262_1_)
        {
            return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp_float(p_148262_1_, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float p_148268_1_)
        {
            p_148268_1_ = this.snapToStep(p_148268_1_);
            return MathHelper.clamp_float(p_148268_1_, this.valueMin, this.valueMax);
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
    

    private VRControllerButtonMapping[] getBindingsDefaults(){
   	
    	VRControllerButtonMapping[] out = new VRControllerButtonMapping[16];
    	
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
    	
    	
    	return out;
    	
    }
    
}

