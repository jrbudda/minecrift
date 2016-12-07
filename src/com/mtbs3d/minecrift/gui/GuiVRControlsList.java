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
import net.minecraft.util.EnumChatFormatting;
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

    public ArrayList<String>  getPossibleFunctions(){
    	ArrayList<String> out = new ArrayList<String>();
    	
    	out.add("none");

    	for (KeyBinding key : mc.gameSettings.keyBindings) {
			out.add(key.getKeyDescription());
		}
    	
    	out.add("keyboard(press)");
    	out.add("keyboard(hold)");
    	out.add("keyboard-shift");
    	out.add("keyboard-ctrl");
    	out.add("keyboard-alt");
    	
    	return out;  	
    }
    
    public void bindKey(VRControllerButtonMapping key){
    	if(key.FunctionDesc.equals("none")){
    		key.key = null;
    		key.FunctionExt = 0;
    		return;
    	}
    	if(key.FunctionDesc.startsWith("keyboard")){
    		key.key = null;
    		if(key.FunctionDesc.contains("-")) key.FunctionExt = 0;
    		return;
    	}
        KeyBinding[] var3 = mc.gameSettings.keyBindings;
        for (final KeyBinding keyBinding : var3) {	
        	if (keyBinding.getKeyDescription().equals(key.FunctionDesc)){
        		key.key = keyBinding;    
        		key.FunctionExt = 0;
        		return;
        	}
		}	
        System.out.println("Keybind not found for " + key.FunctionDesc);
    }
    
    public GuiVRControlsList(GuiVRControls parent, Minecraft mc)
    {
        super(mc, parent.width, parent.height, 63, parent.height - 32, 20);
        this.parent = parent;
        this.mc = mc;
        
        
        ArrayList<VRControllerButtonMapping> bindings = new ArrayList<VRControllerButtonMapping>();
        for (VRControllerButtonMapping vb : mc.vrSettings.buttonMappings) {
        	if(MCOpenVR.isVive && vb.Button.name().startsWith("BUTTON")) bindings.add(vb);
        	if(!MCOpenVR.isVive && vb.Button.name().startsWith("OCULUS")) bindings.add(vb); 
        }

        this.listEntries = new GuiListExtended.IGuiListEntry[bindings.size()];

        //  Arrays.sort(bindings);
        String var5 = null;
        int var4 = 0;
        int var7 = bindings.size();
        for (int i = 0; i < var7; i++)
        {
        	VRControllerButtonMapping kb = bindings.get(i);
            String cat = "VR"; // kb.getKeyCategory();

            if (!cat.equals(var5))
            {
                var5 = cat;
          //      this.listEntries[var4++] = new GuiVRControlsList.CategoryEntry(cat);
            }

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

        public void drawEntry(int p_148279_1_, int p_148279_2_, int p_148279_3_, int p_148279_4_, int p_148279_5_, Tessellator p_148279_6_, int p_148279_7_, int p_148279_8_, boolean p_148279_9_)
        {
            GuiVRControlsList.this.mc.fontRendererObj.drawString(this.labelText, GuiVRControlsList.this.mc.currentScreen.width / 2 - this.labelWidth / 2, p_148279_3_ + p_148279_5_ - GuiVRControlsList.this.mc.fontRendererObj.FONT_HEIGHT - 1, 16777215);
        }

        public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
        {
            return false;
        }

        public void mouseReleased(int p_148277_1_, int p_148277_2_, int p_148277_3_, int p_148277_4_, int p_148277_5_, int p_148277_6_) {}
    }

    public class MappingEntry implements GuiListExtended.IGuiListEntry
    {
        private final VRControllerButtonMapping myKey;
        private final GuiButton btnChangeKeyBinding;
        private final GuiButton btnKey;
        private final ArrayList<String> possibilites;
        private int myi;
        private static final String __OBFID = "CL_00000735";
        private GuiEnterText guiEnterText;
        
        private MappingEntry(VRControllerButtonMapping key)
        {
            this.myKey = key;
            this.btnChangeKeyBinding = new GuiButton(0, 0, 0, 150, 18, I18n.format(key.FunctionDesc, new Object[0]));
            this.possibilites = GuiVRControlsList.this.getPossibleFunctions();
            myi = this.possibilites.indexOf(myKey.FunctionDesc);    
            btnKey =new GuiButton(0, 0, 0, 18, 18, "");
        }

        public void drawEntry(int p_148279_1_, int x, int y, int p_148279_4_, int p_148279_5_, Tessellator p_148279_6_, int p_148279_7_, int p_148279_8_, boolean p_148279_9_)
        {

        	GuiVRControlsList.this.mc.fontRendererObj.drawString(myKey.Button.toString().replace("BUTTON_", "").replace("OCULUS_", ""), x + 40  - GuiVRControlsList.this.maxListLabelWidth, y + p_148279_5_ / 2 - GuiVRControlsList.this.mc.fontRendererObj.FONT_HEIGHT / 2, 16777215);
        	this.btnChangeKeyBinding.xPosition = x + 90;
        	this.btnChangeKeyBinding.yPosition = y;
        	this.btnChangeKeyBinding.displayString = I18n.format(this.myKey.FunctionDesc, new Object[0]);             
        
        	this.btnKey.xPosition = x+240;
        	this.btnKey.yPosition = y;
        	this.btnKey.visible = (myKey.FunctionDesc.startsWith("keyboard("));
            this.btnKey.displayString = String.valueOf((myKey.FunctionExt));        		

        	boolean var10 = GuiVRControlsList.this.parent.buttonId == myKey;
        	
        	if (var10)
        	{
        		this.btnKey.displayString = EnumChatFormatting.WHITE + "> " + EnumChatFormatting.YELLOW + this.btnKey.displayString + EnumChatFormatting.WHITE + " <";
        	}
        	else if (false)
        	{ //alow multi binding.
        		this.btnKey.displayString = EnumChatFormatting.RED + this.btnKey.displayString;
        	}

        	this.btnChangeKeyBinding.drawButton(GuiVRControlsList.this.mc, p_148279_7_, p_148279_8_);
        	this.btnKey.drawButton(GuiVRControlsList.this.mc, p_148279_7_, p_148279_8_);
        }
        
        public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
        {
        	
            if (this.btnChangeKeyBinding.mousePressed(GuiVRControlsList.this.mc, p_148278_2_, p_148278_3_))
            {
            	//cycle? select from list?
            	myi++;
            	if(myi >= possibilites.size()) myi = 0;
            	this.myKey.FunctionDesc = possibilites.get(myi);
            	bindKey(myKey);
            	
                return true;
            }
            else if (this.btnKey.mousePressed(GuiVRControlsList.this.mc, p_148278_2_, p_148278_3_))
            {       	
                GuiVRControlsList.this.parent.buttonId = myKey; 
                
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
      //      this.btnReset.mouseReleased(p_148277_2_, p_148277_3_);
        }

        MappingEntry(VRControllerButtonMapping p_i45030_2_, Object p_i45030_3_)
        {
            this(p_i45030_2_);
        }
    }
}
