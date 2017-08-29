package com.mtbs3d.minecrift.gui;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.gui.GuiScreen;

import org.apache.commons.lang3.ArrayUtils;

import com.mtbs3d.minecrift.control.VRControllerButtonMapping;
import com.mtbs3d.minecrift.control.ViveButtons;
import com.mtbs3d.minecrift.gui.framework.GuiEnterText;
import com.mtbs3d.minecrift.provider.MCOpenVR;

public class GuiVRControlsList extends GuiListExtended
{
    private final GuiVRControls parent;
    private final Minecraft mc;
    private final GuiListExtended.IGuiListEntry[] listEntries;
    private int maxListLabelWidth = 0;
    private static final String __OBFID = "CL_00000732";
    
    public GuiVRControlsList(GuiVRControls parent, Minecraft mc)
    {
        super(mc, parent.width, parent.height, 63, parent.height - 32, 20);
        this.parent = parent;
        this.mc = mc;
        
        ArrayList<VRControllerButtonMapping> bindings = new ArrayList<VRControllerButtonMapping>();
        for (VRControllerButtonMapping vb : mc.vrSettings.buttonMappings) {
			if(MCOpenVR.isVive() && vb.Button.name().startsWith("BUTTON")) bindings.add(vb);
			if(!MCOpenVR.isVive() && vb.Button.name().startsWith("OCULUS")) bindings.add(vb); 
		}
       
        this.listEntries = new GuiListExtended.IGuiListEntry[bindings.size()];
        
        String var5 = null;
        int var4 = 0;
        int var7 = bindings.size();
        for (int i = 0; i < var7; i++)
        {
        	VRControllerButtonMapping kb = bindings.get(i);
            String cat = "VR";

            int width = mc.fontRendererObj.getStringWidth(I18n.format(kb.FunctionDesc, new Object[0]));

            if (width > this.maxListLabelWidth)
            {
                this.maxListLabelWidth = width;
            }

            this.listEntries[i] = new GuiVRControlsList.MappingEntry(kb, null);
        }
    }

    protected int getSize()
    {
        return this.listEntries.length;
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public GuiListExtended.IGuiListEntry getListEntry(int i)
    {
        return this.listEntries[i];
    }

    protected int getScrollBarX()
    {
        return super.getScrollBarX() + 15;
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth()
    {
        return super.getListWidth() + 32;
    }

    public class CategoryEntry implements GuiListExtended.IGuiListEntry
    {
        private final String labelText;
        private final int labelWidth;
        private static final String __OBFID = "CL_00000734";

        public CategoryEntry(String p_i45028_2_)
        {
            this.labelText = I18n.format(p_i45028_2_, new Object[0]);
            this.labelWidth = GuiVRControlsList.this.mc.fontRendererObj.getStringWidth(this.labelText);
        }

		@Override
        public void drawEntry(int p_148279_1_, int p_148279_2_, int p_148279_3_, int p_148279_4_, int p_148279_5_,Tessellator t,  int p_148279_7_, int p_148279_8_, boolean p_148279_9_)
        {
            mc.fontRendererObj.drawString(this.labelText, GuiVRControlsList.this.mc.currentScreen.width / 2 - this.labelWidth / 2, p_148279_3_ + p_148279_5_ - GuiVRControlsList.this.mc.fontRendererObj.FONT_HEIGHT - 1, 16777215);
        }

        public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
        {
            return false;
        }

        public void mouseReleased(int p_148277_1_, int p_148277_2_, int p_148277_3_, int p_148277_4_, int p_148277_5_, int p_148277_6_) 
        {
        }

    }

    public class MappingEntry implements GuiListExtended.IGuiListEntry
    {
        private final VRControllerButtonMapping myKey;
        private final GuiButton btnChangeKeyBinding;
        private static final String __OBFID = "CL_00000735";
        private GuiEnterText guiEnterText;
        private GuiVRControls parentScreen;
        
        private MappingEntry(VRControllerButtonMapping key, GuiVRControls parent)
        {
            this.myKey = key;
            this.btnChangeKeyBinding = new GuiButton(0, 0, 0, 150, 18, I18n.format(key.FunctionDesc, new Object[0]));
            this.parentScreen = parent;
        }
        
		@Override
        public void drawEntry(int p_148279_1_, int x, int y, int p_148279_4_, int p_148279_5_,Tessellator t, int p_148279_7_, int p_148279_8_, boolean p_148279_9_)
        {

        	GuiVRControlsList.this.mc.fontRendererObj.drawString(myKey.Button.toString().replace("BUTTON_", "").replace("OCULUS_", ""), x + 40  - GuiVRControlsList.this.maxListLabelWidth, y + p_148279_5_ / 2 - GuiVRControlsList.this.mc.fontRendererObj.FONT_HEIGHT / 2, 16777215);
        	this.btnChangeKeyBinding.xPosition = x + 90;
        	this.btnChangeKeyBinding.yPosition= y;
        	this.btnChangeKeyBinding.displayString = I18n.format(this.myKey.FunctionDesc, new Object[0]) + " " + this.myKey.FunctionExt;             
        
        	boolean var10 = GuiVRControlsList.this.parent.buttonId == myKey;
        	this.btnChangeKeyBinding.drawButton(GuiVRControlsList.this.mc, p_148279_7_, p_148279_8_);
        }
        
        public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
        {
            if (this.btnChangeKeyBinding.mousePressed(GuiVRControlsList.this.mc, p_148278_2_, p_148278_3_))
            {           	
            	parent.selectionMode = true;
            	parent.buttonId = myKey;         	
            	return true;          
            }
            else
            {
                return false;
            }
        }

        public void mouseReleased(int p_148277_1_, int p_148277_2_, int p_148277_3_, int p_148277_4_, int p_148277_5_, int p_148277_6_)
        {
            this.btnChangeKeyBinding.mouseReleased(p_148277_2_, p_148277_3_);
        }


    }
}
