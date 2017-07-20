package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.gui.framework.*;
import com.mtbs3d.minecrift.settings.VRSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiLocomotionSettings extends BaseGuiSettings implements GuiEventEx
{
    static VRSettings.VrOptions[] locomotionSettings = new VRSettings.VrOptions[]
    {
            VRSettings.VrOptions.WEAPON_COLLISION,
            VRSettings.VrOptions.REALISTIC_JUMP,
            VRSettings.VrOptions.ANIMAL_TOUCHING,
            VRSettings.VrOptions.REALISTIC_SNEAK,
            VRSettings.VrOptions.BCB_ON,
            VRSettings.VrOptions.REALISTIC_CLIMB,
            VRSettings.VrOptions.WALK_MULTIPLIER,
            VRSettings.VrOptions.REALISTIC_ROW,
            VRSettings.VrOptions.ALLOW_MODE_SWITCH,
            VRSettings.VrOptions.REALISTIC_SWIM
    };

    static VRSettings.VrOptions[] teleportSettings = new VRSettings.VrOptions[]
    {
            VRSettings.VrOptions.WALK_UP_BLOCKS,
            VRSettings.VrOptions.LIMIT_TELEPORT,
            VRSettings.VrOptions.SIMULATE_FALLING

    };
    static VRSettings.VrOptions[] freeMoveSettings = new VRSettings.VrOptions[]
    {
    		VRSettings.VrOptions.FREEMOVE_MODE,
            VRSettings.VrOptions.MOVEMENT_MULTIPLIER,
            VRSettings.VrOptions.INERTIA_FACTOR,
            VRSettings.VrOptions.FOV_REDUCTION,

    };
    
    public GuiLocomotionSettings(GuiScreen guiScreen, VRSettings guivrSettings) {
        super( guiScreen, guivrSettings );
        screenTitle = "Locomotion Settings";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DEFAULTS, this.width / 2 - 155 ,  this.height -25 ,150,20, "Reset To Defaults"));
        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DONE, this.width / 2 - 155  + 160, this.height -25,150,20, "Done"));
        VRSettings.VrOptions[] buttons = locomotionSettings;
        addButtons(buttons,0);
        mc.vrSettings.vrFreeMove = mc.vrPlayer.getFreeMoveMode();
        GuiSmallButtonEx mode = new GuiSmallButtonEx(VRSettings.VrOptions.MOVE_MODE.returnEnumOrdinal(), this.width / 2 - 68, this.height / 6 + 102,VRSettings.VrOptions.MOVE_MODE, this.guivrSettings.getKeyBinding(VRSettings.VrOptions.MOVE_MODE));
        mode.setEventHandler(this);
        this.buttonList.add(mode);
        if(mc.vrPlayer.getFreeMoveMode())
        	addButtons(freeMoveSettings,134);
        else
        	addButtons(teleportSettings,134);        
    }

	private void addButtons(VRSettings.VrOptions[] buttons, int startY) {
		int extra = startY;
        for (int var12 = 2; var12 < buttons.length + 2; ++var12)
        {
            VRSettings.VrOptions var8 = buttons[var12 - 2];
            int width = this.width / 2 - 155 + var12 % 2 * 160;
            int height = this.height / 6 + 21 * (var12 / 2) - 10 + extra;

            if (var8 == VRSettings.VrOptions.DUMMY)
                continue;

            if (var8 == VRSettings.VrOptions.DUMMY_SMALL) {
                extra += 5;
                continue;
            }

            if (var8.getEnumFloat())
            {
                float minValue = 0.0f;
                float maxValue = 1.0f;
                float increment = 0.01f;

                if (var8 == VRSettings.VrOptions.MOVEMENT_MULTIPLIER)
                {
                    minValue = 0.15f;
                    maxValue = 1.3f;
                    increment = 0.01f;
                }
                if (var8 == VRSettings.VrOptions.STRAFE_MULTIPLIER)
                {
                    minValue = 0f;
                    maxValue = 1.0f;
                    increment = 0.01f;
                }
                else if (var8 == VRSettings.VrOptions.WALK_MULTIPLIER){
                    minValue=1f;
                    maxValue=10f;
                    increment=0.1f;
                }
                // VIVE START - new options
                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height - 20, var8, this.guivrSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guivrSettings.getOptionFloatValue(var8));
                slider.setEventHandler(this);
                slider.enabled = getEnabledState(var8);
                this.buttonList.add(slider);
            }
            else
            {
                GuiSmallButtonEx smallButton = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height - 20, var8, this.guivrSettings.getKeyBinding(var8));
                smallButton.setEventHandler(this);
                smallButton.enabled = getEnabledState(var8);
                this.buttonList.add(smallButton);
            }
        }
	}

    private boolean getEnabledState(VRSettings.VrOptions var8)
    {
        String s = var8.getEnumString();

        if(s==VRSettings.VrOptions.ALLOW_CRAWLING.getEnumString()) return false;
        if(s.equals(VRSettings.VrOptions.REALISTIC_JUMP.getEnumString()) ||
                s.equals(VRSettings.VrOptions.REALISTIC_SNEAK.getEnumString()))
            return !Minecraft.getMinecraft().vrSettings.seated;
        

        return true;
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

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        VRSettings vr = mc.vrSettings;

        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id == ID_GENERIC_DONE)
            {
                Minecraft.getMinecraft().gameSettings.saveOptions();
                Minecraft.getMinecraft().vrSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == ID_GENERIC_DEFAULTS)
            {
                vr.inertiaFactor = VRSettings.INERTIA_NORMAL;
                vr.movementSpeedMultiplier = 1f;
                vr.simulateFalling = true;
                //jrbudda//
                vr.weaponCollision = true;
                vr.animaltouching = true;
                vr.vrAllowCrawling = false;
                vr.vrAllowLocoModeSwotch = true;
                vr.vrFreeMove = false;
                vr.vrLimitedSurvivalTeleport = true;
                vr.vrShowBlueCircleBuddy = true;
                vr.walkMultiplier=1;
                vr.vrFreeMoveMode = vr.FREEMOVE_CONTROLLER;
                vr.realisticClimbEnabled = true;
                vr.realisticJumpEnabled = true;
                vr.realisticSneakEnabled = true;
                vr.realisticSwimEnabled = true;
                vr.realisticRowEnabled = true;
                vr.vehicleRotation = false;
                vr.useFOVReduction = false;
                vr.walkUpBlocks = true;
                //end jrbudda
                
                Minecraft.getMinecraft().gameSettings.viewBobbing = true;

                Minecraft.getMinecraft().gameSettings.saveOptions();
                Minecraft.getMinecraft().vrSettings.saveOptions();
                this.reinit = true;
            }
            else if (par1GuiButton instanceof GuiSmallButtonEx)
            {
                VRSettings.VrOptions num = VRSettings.VrOptions.getEnumOptions(par1GuiButton.id);
                    this.guivrSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnVrEnumOptions(), 1);
                    par1GuiButton.displayString = this.guivrSettings.getKeyBinding(VRSettings.VrOptions.getEnumOptions(par1GuiButton.id));
                    
                    if(num == VRSettings.VrOptions.MOVE_MODE){
                    	this.reinit = true;
                    }
                    
            }
        }
    }

    @Override
    public boolean event(int id, VRSettings.VrOptions enumm)
    {
        return true;
    }

    @Override
    public boolean event(int id, String s) {
        return true;
    }

    @Override
    protected String[] getTooltipLines(String displayString, int buttonId)
    {
        VRSettings.VrOptions e = VRSettings.VrOptions.getEnumOptions(buttonId);
        if( e != null )
            switch(e)
            {
                case MOVEMENT_MULTIPLIER:
                    return new String[] {
                            "Sets a movement multiplier, allowing slower movement",
                            "than default. This may help reduce locomotion induced",
                            "simulator sickness.",
                            "WARNING: May trigger anti-cheat warnings if on a",
                            "Multiplayer server!!",
                            "Defaults to standard Minecraft movement (1.0)",
                            "speed)."
                    } ;
                case STRAFE_MULTIPLIER:
                    return new String[] {
                            "Sets an additional strafe (side-to-side) movement",
                            "multiplier. This is applied on top of the movement",
                            "multiplier. A value of zero will disable strafe.",
                            "This may help reduce locomotion induced simulator",
                            "sickness. WARNING: May trigger anti-cheat warnings",
                            "if on a Multiplayer server!!",
                            "Defaults to 0.33 (1.0 is standard Minecraft movement",
                            "speed)."
                    } ;
                case WALK_UP_BLOCKS:
                    return new String[] {
                            "Allows you to set the ability to walk up blocks without",
                            "having to jump. HOTKEY - RCtrl-B",
                            "WARNING: May trigger anti-cheat warnings if on a",
                            "Multiplayer server!!",
                            "  OFF: (Default) You will have to jump up blocks.",
                            "  ON:  You can walk up single blocks. May reduce",
                            "       locomotion induced simulator sickness for some."
                    } ;
                case INERTIA_FACTOR:
                    return new String[]{
                            "Sets the player's movement inertia in single player",
                            "mode. Lower inertia means faster acceleration, higher",
                            "inertia slower accelaration. High inertia may reduce",
                            "motion sickness for some, but beware of cliff edges!!",
                            "  Normal: (Default) Standard Minecraft player",
                            "           movement.",
                            "  Automan < Normal < A lot < Even More. Does not",
                            "  affect lava, water or jumping movement currently."
                    };
                // VIVE START - new options
                case SIMULATE_FALLING:
                    return new String[] {
                            "If enabled the player will falls to the ground in TP mode",
                            "when standing above empty space. Also allows jumping"
                    } ;
                case WEAPON_COLLISION:
                    return new String[] {
                            "If enabled, you can swing your pickaxe at blocks to",
                            "mine them, or your sword at enemies to hit them."
                    } ;
                case ANIMAL_TOUCHING:
                    return new String[] {
                            "If enabled, touching a passive mob (animal) without a",
                            "weapon will right-click (interact) instead of attacking.",
                            "Turn off for Piggy Slapping, Josh.",
                    } ;
                // VIVE END - new options
                    //JRBUDDA
                case ALLOW_MODE_SWITCH:
                    return new String[] {
                            "Allows the use of the Pick Block button to switch between",
                            "Teleport and Free Move mode."
                    } ;
                case ALLOW_CRAWLING:
                    return new String[] {
                            "If enabled the player will be able to duck under block"
                    } ;
                case MOVE_MODE:
                    return new String[] {
                            "Current move mode. Teleport or Free Move."
                    } ;
                case LIMIT_TELEPORT:
                    return new String[] {
                            "If enabled the arc teleporter will be have restrictions",
                            "in survival mode. It will not be able to jump up the side", 
                            "of blocks, it will consume food, and it will have an energy",
                            "bar that refills over time."
                    } ;
                case BCB_ON:
                    return new String[] {
                            "Shows your body position as a square shadow on the ground.",
                            "This is your Square Shadow Buddy (tm).",
                            "Do not lose your Square Shadow Buddy."
                    };
                case REALISTIC_JUMP:
                    return new String[]{
                            "If turned on, once you jump in real life",
                            "Your player will also jump. Also enables",
                            "Jump Boots."
                    };
                case REALISTIC_SNEAK:
                    return new String[]{
                            "If turned on, once you duck in real life",
                            "Your player will also sneak"
                    };
                case REALISTIC_CLIMB:
                    return new String[]{
                            "If turned on, allow climbing ladders and vines",
                            "by touching them. Also enables Climb Claws."
                    };
                case REALISTIC_SWIM:
                    return new String[]{
                            "If turned on, allow swimming by doing the breaststoke",
                            "with the controllers."
                    };
                case REALISTIC_ROW:
                    return new String[]{
                            "Row, row, row your boat... by flapping your arms like mad."
                    };
                case WALK_MULTIPLIER:
                    return new String[]{
                            "Multiplies your position in the room by a factor",
                            "Allows you to walk around more,",
                            "but may cause motion sickness"
                    };
                case FREEMOVE_MODE:
                    return new String[] {
                            "The source for freemove direction. Options are",
                            "Controller: Uses left controller direction, max speed",
                            "HMD: Uses head direction, max speed",
                            "Run In Place: Use average controllers direction. Speed based",
                            "on controller motion.",
                            "Joy/Pad: Uses the left touchpad or joystick for all motion.",
                            "Overrides all 4 movement direction keybinds."
                            
                    } ;
                case VEHICLE_ROTATION:
                    return new String[] {
                            "Riding in a vehicle will rotate the world",
                            "as the vehicle rotates. May be disorienting."
                            
                    } ;
                case FOV_REDUCTION:
                    return new String[] {
                            "Shrinks the field of view while moving. Can help with",
                            "motion sickness."
                    } ;
                default:
                    return null;
            }
        else
            switch(buttonId)
            {
//                case 201:
//                    return new String[] {
//                            "Open this configuration screen to adjust the Head",
//                            "  Tracker orientation (direction) settings. ",
//                            "  Ex: Head Tracking Selection (Hydra/Oculus), Prediction"
//                    };
                default:
                    return null;
            }
    }
}
