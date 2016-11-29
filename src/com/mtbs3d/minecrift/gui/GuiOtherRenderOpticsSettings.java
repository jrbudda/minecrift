package com.mtbs3d.minecrift.gui;

import com.mtbs3d.minecrift.gui.framework.*;
import com.mtbs3d.minecrift.settings.VRSettings;

import de.fruitfly.ovr.enums.EyeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiOtherRenderOpticsSettings extends BaseGuiSettings implements GuiEventEx
{
    static VRSettings.VrOptions[] oculusOptionsUseSingleIpd = new VRSettings.VrOptions[]
    {

    };

    static VRSettings.VrOptions[] oculusOptionsUseTwinIpd = new VRSettings.VrOptions[]
    {

    };

    public GuiOtherRenderOpticsSettings(GuiScreen guiScreen, VRSettings guivrSettings) {
        super( guiScreen, guivrSettings );
        screenTitle = "IPD / FOV Settings";
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
//        this.buttonList.clear();
//        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DEFAULTS, this.width / 2 - 155 ,  this.height -25 ,150,20, "Reset To Defaults"));
//        this.buttonList.add(new GuiButtonEx(ID_GENERIC_DONE, this.width / 2 - 155  + 160, this.height -25,150,20, "Done"));
//        String eyeRelief = "";//String.format("Base Eye Relief: %.3fmm ", new Object[] { this.mc.entityRenderer.getBaseEyeRelief() * 1000f });
//        VRSettings.VrOptions[] buttons = null;
//        if (Minecraft.getMinecraft().vrSettings.useHalfIpds == false)
//            buttons = oculusOptionsUseSingleIpd;
//        else
//            buttons = oculusOptionsUseTwinIpd;
//
//        for (int var12 = 2; var12 < buttons.length + 2; ++var12)
//        {
//            VRSettings.VrOptions var8 = buttons[var12 - 2];
//            int width = this.width / 2 - 155 + var12 % 2 * 160;
//            int height = this.height / 6 + 21 * (var12 / 2) - 10;
//
//            if (var8 == VRSettings.VrOptions.DUMMY)
//                continue;
//
//            if (var8.getEnumFloat())
//            {
//                float minValue = 0.0f;
//                float maxValue = 1.0f;
//                float increment = 0.01f;
//
//                if (var8 == VRSettings.VrOptions.EYE_RELIEF_ADJUST)
//                {
//                    minValue = -0.035f;
//                    maxValue =  0.035f;
//                    increment = 0.00001f;
//                }
//                else if (var8 == VRSettings.VrOptions.FOV_CHANGE)
//                {
//                    minValue  = -10f;
//                    maxValue  = 10f;
//                    increment = 0.1f;
//                }
//                else if (var8 == VRSettings.VrOptions.TOTAL_IPD)
//                {
//                    minValue = 0.055f;
//                    maxValue = 0.075f;
//                    increment = 0.0001f;
//                }
//                else if (var8 == VRSettings.VrOptions.LEFT_HALF_IPD)
//                {
//                    minValue = -0.0375f;
//                    maxValue = -0.0225f;
//                    increment = 0.0001f;
//                }
//                else if (var8 == VRSettings.VrOptions.RIGHT_HALF_IPD)
//                {
//                    minValue = 0.0225f;
//                    maxValue = 0.0375f;
//                    increment = 0.0001f;
//                }
//
//                GuiSliderEx slider = new GuiSliderEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8), minValue, maxValue, increment, this.guivrSettings.getOptionFloatValue(var8));
//                slider.setEventHandler(this);
//                slider.enabled = getEnabledState(var8);
//                this.buttonList.add(slider);
//            }
//            else
//            {
//                if (var8 == VRSettings.VrOptions.EYE_RELIEF_PLACEHOLDER)
//                {
//                    GuiSmallButtonEx button = new GuiSmallButtonEx(9999, width, height, var8, eyeRelief);
//                    button.enabled = false;
//                    this.buttonList.add(button);
//                }
//                else
//                {
//                    GuiSmallButtonEx smallButton = new GuiSmallButtonEx(var8.returnEnumOrdinal(), width, height, var8, this.guivrSettings.getKeyBinding(var8));
//                    smallButton.setEventHandler(this);
//                    smallButton.enabled = getEnabledState(var8);
//                    this.buttonList.add(smallButton);
//                }
//            }
//        }
    }

    private boolean getEnabledState(VRSettings.VrOptions var8)
    {
        String s = var8.getEnumString();

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
        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id == ID_GENERIC_DONE)
            {
                Minecraft.getMinecraft().vrSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
            else if (par1GuiButton.id == ID_GENERIC_DEFAULTS)
            {

                Minecraft.getMinecraft().vrSettings.saveOptions();
                this.reinit = true;
            }
            else if (par1GuiButton instanceof GuiSmallButtonEx)
            {
                VRSettings.VrOptions num = VRSettings.VrOptions.getEnumOptions(par1GuiButton.id);
                this.guivrSettings.setOptionValue(((GuiSmallButtonEx)par1GuiButton).returnVrEnumOptions(), 1);
                par1GuiButton.displayString = this.guivrSettings.getKeyBinding(VRSettings.VrOptions.getEnumOptions(par1GuiButton.id));
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
//            case EYE_RELIEF_PLACEHOLDER:
//                return new String[] {
//                        "The current un-adjusted eye relief value, as reported",
//                        "from the Oculus Profile.",
//                        "Positive values are towards your face, negative away.",
//                };
//            case EYE_RELIEF_ADJUST:
//                return new String[] {
//                        "Adjusts the eye relief value (the distance of the HMD",
//                        "screen from the center eye position). Positive values",
//                        "are towards your face, negative away.",
//                        " Adjust this if vertical lines do not seem to maintain",
//                        " their position in the world (as you slowly rotate your",
//                        " head from side-to-side; while keeping your gaze fixed",
//                        " on the line).",
//                        " Can account for squashed foam padding on the HMD etc.",
//                };
//            case FOV_CHANGE:
//                return new String[] {
//                        "Adjusts the position of the rendered distortion border.",
//                        " Positive values: reduce the size of the distortion",
//                        "  border, possibly increasing Field-of-View (FOV) at",
//                        "  the expense of increased rendering cost.",
//                        " Negative values: increase the size of the distortion",
//                        "  border, reducing render cost at the expense of a",
//                        "  noticeable reduction in FOV.",
//                };
//            case USE_PROFILE_IPD:
//            case CONFIG_IPD_MODE:
//            case LEFT_HALF_IPD:
//            case RIGHT_HALF_IPD:
//            case TOTAL_IPD:
//                return new String[] {
//                        "Allows adjustment of IPD if not using Oculus profile",
//                        "settings. NOTE: It is STRONGLY RECOMMENDED to use",
//                        "the Oculus Profile settings!!",
//                        " Select total or half IPD configuration.",
//                        " Adjusting the IPD manually may alter in game scale;",
//                        " however various HMD parameters configured by",
//                        " Oculus will not be correct. USE AT YOUR OWN RISK."
//                };
//            case MAX_FOV:
//                return new String[] {
//                        "Toggles use of the default Field-of-View (FOV),",
//                        "or the maximum FOV possible with the specified HMD.",
//                        " Default: Default HMD FOV. A balance of FOV versus",
//                        "          render cost.",
//                        " Maximum: *MAY* enlarge viewable FOV at the expense of",
//                        "          increased rendering cost.",
//                        " NOTE with the DK2, Default and Max are almost",
//                        " identical.",
//                };
            default:
                return null;
            }
        else
            switch(buttonId)
            {
            default:
                return null;
            }
    }
}
