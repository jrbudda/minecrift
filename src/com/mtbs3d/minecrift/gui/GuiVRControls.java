/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package com.mtbs3d.minecrift.gui;

import java.io.IOException;

import com.mtbs3d.minecrift.control.VRControllerButtonMapping;
import com.mtbs3d.minecrift.gui.framework.BaseGuiSettings;
import com.mtbs3d.minecrift.gui.framework.GuiButtonEx;
import com.mtbs3d.minecrift.settings.VRSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;

public class GuiVRControls extends BaseGuiSettings {

	public VRControllerButtonMapping buttonId; 
	public boolean selectionMode = false;
    public boolean waitingForKey;
    
	private GuiVRControlsList guiList;
	private GuiKeyBindingSelection guiSelection;
    private GuiButton btnDefaults;
    private GuiButton btnDone;
    private GuiButton btnCancel;

	public GuiVRControls(GuiScreen par1GuiScreen, VRSettings par2vrSettings) {
		super(par1GuiScreen, par2vrSettings);
        screenTitle = "VR Control Remapping";
	}

    /**
     * Fired when a key is typed (except F11 who toggle full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode)
    {
        if (waitingForKey)
        {
        	buttonId.FunctionExt = typedChar;
            waitingForKey = false;
        }
        else
        {
        	super.keyTyped(typedChar, keyCode);
        }
    }


    
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui() {
    	this.guiList = new GuiVRControlsList(this, mc);
    	this.guiSelection = new GuiKeyBindingSelection(this, mc);
        this.buttonList.clear();
        btnDefaults = (new GuiButtonEx(ID_GENERIC_DEFAULTS, this.width / 2 - 155 ,  this.height -25 ,150,20, "Reset To Defaults"));
        btnDone = (new GuiButtonEx(ID_GENERIC_DONE, this.width / 2 - 155  + 160, this.height -25,150,20, "Done"));
        btnCancel = (new GuiButtonEx(99, this.width / 2 - 155  + 80, this.height -25,150,20, "Cancel"));
        btnCancel.visible = false;
        this.buttonList.add(btnDefaults);
        this.buttonList.add(btnDone);
        this.buttonList.add(btnCancel);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3) {
    	if (reinit) {
    		initGui();
    		reinit = false;
    	}    

    	if(waitingForKey){
    		screenTitle = "Press Keyboard Key...";
    		btnCancel.visible = true;
    		btnDone.visible = false;
    		btnDefaults.visible = false;
    	}else {
    		if(this.selectionMode && this.buttonId != null){
    			btnCancel.visible = true;
    			btnDone.visible = false;
    			btnDefaults.visible = false;
    			screenTitle = "Choose action for " + this.buttonId.Button.toString() + " (Current: " + I18n.format(this.buttonId.FunctionDesc)+")";
    			this.guiSelection.drawScreen(par1, par2, par3);
    		}
    		else{
    			btnCancel.visible = false;
    			btnDone.visible = true;
    			btnDefaults.visible = true;
    			this.selectionMode = false;
    			screenTitle = "VR Control Remapping";
    			this.guiList.drawScreen(par1, par2, par3);       
    		}
    	}
    	super.drawScreen(par1,par2,par3,false);

    }
    
    public void bindKey(VRControllerButtonMapping key){
    	if(key.FunctionDesc.equals("None")){
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
    
    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton) {
    	if (par1GuiButton.id == ID_GENERIC_DONE) {
            this.guivrSettings.saveOptions();
            this.mc.displayGuiScreen(this.parentGuiScreen);
        } else if (par1GuiButton.id == ID_GENERIC_DEFAULTS){
        	mc.vrSettings.resetBindings();
        	this.initGui();
        }   else if (par1GuiButton.id == 99){ //selection cancel
        	this.selectionMode = false;
        	this.waitingForKey = false;
        }
    }
    
    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
      
        boolean flag = false;
        
        if(this.selectionMode){
        		flag = this.guiSelection.func_148179_a(mouseX, mouseY, mouseButton);
        		this.guiList.setEnabled(true);
        }else{
        		flag = this.guiList.func_148179_a(mouseX, mouseY, mouseButton);
        		this.guiSelection.setEnabled(true);
        }
      if (!flag)
        {
    	  super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton\n \n@param state Will be negative
     * to indicate mouse move and will be either 0 or 1 to indicate mouse up.
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (state != 0 || !this.guiList.func_148181_b(mouseX, mouseY, state))
        {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    
}
