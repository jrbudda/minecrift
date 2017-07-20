package com.mtbs3d.minecrift.gui;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.ArrayUtils;

public class GuiKeyBindingSelection extends GuiListExtended
{
    private final GuiVRControls controlsScreen;
    private final Minecraft mc;
    private final GuiListExtended.IGuiListEntry[] listEntries;
    private int maxListLabelWidth;

    public GuiKeyBindingSelection(GuiVRControls controls, Minecraft mcIn)
    {
        super(mcIn, controls.width + 45, controls.height, 63, controls.height - 32, 20);
        this.controlsScreen = controls;
        this.mc = mcIn;
        KeyBinding[] akeybinding = (KeyBinding[])ArrayUtils.clone(mcIn.gameSettings.keyBindings);
        this.listEntries = new GuiListExtended.IGuiListEntry[akeybinding.length + KeyBinding.getKeybinds().size()+7];
        Arrays.sort((Object[])akeybinding);
        int i = 0;
        String s = null;

        this.listEntries[i++] = new GuiKeyBindingSelection.KeyEntry("None");
        
        for (KeyBinding keybinding : akeybinding)
        {
            String s1 = keybinding.getKeyCategory();

            if (!s1.equals(s))
            {
                s = s1;
                this.listEntries[i++] = new GuiKeyBindingSelection.CategoryEntry(s1);
            }

            int j = mcIn.fontRendererObj.getStringWidth(I18n.format(keybinding.getKeyDescription()));

            if (j > this.maxListLabelWidth)
            {
                this.maxListLabelWidth = j;
            }

            this.listEntries[i++] = new GuiKeyBindingSelection.KeyEntry(keybinding);
        }
        this.listEntries[i++] = new GuiKeyBindingSelection.CategoryEntry("Keyboard Emulation");
        this.listEntries[i++] = new GuiKeyBindingSelection.KeyEntry("keyboard (press)");
        this.listEntries[i++] = new GuiKeyBindingSelection.KeyEntry("keyboard (hold)");
        this.listEntries[i++] = new GuiKeyBindingSelection.KeyEntry("keyboard-shift");
        this.listEntries[i++] = new GuiKeyBindingSelection.KeyEntry("keyboard-ctrl");
        this.listEntries[i++] = new GuiKeyBindingSelection.KeyEntry("keyboard-alt");
                
    }

    protected int getSize()
    {
        return this.listEntries.length;
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public GuiListExtended.IGuiListEntry getListEntry(int index)
    {
        return this.listEntries[index];
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

        public CategoryEntry(String name)
        {
            this.labelText = I18n.format(name);
            this.labelWidth = GuiKeyBindingSelection.this.mc.fontRendererObj.getStringWidth(this.labelText);
        }

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,Tessellator p_148279_6_,int mouseX, int mouseY, boolean isSelected) {
            GuiKeyBindingSelection.this.mc.fontRendererObj.drawString(EnumChatFormatting.AQUA + this.labelText, GuiKeyBindingSelection.this.mc.currentScreen.width / 2 - this.labelWidth / 2, y + slotHeight - GuiKeyBindingSelection.this.mc.fontRendererObj.FONT_HEIGHT - 1, 16777215);
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            return false;
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
        {
        }


    }

    public class KeyEntry implements GuiListExtended.IGuiListEntry
    {
        private final String keyDesc;
        private final String keyPrettyDesc;

        private KeyEntry(String keydesc)
        {
            this.keyDesc = keydesc;
        	this.keyPrettyDesc= I18n.format(keydesc);
        }

        
        public KeyEntry(KeyBinding keybinding) {
            this.keyDesc = keybinding.getKeyDescription();
        	this.keyPrettyDesc= I18n.format(this.keyDesc);
		}


		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,Tessellator p_148279_6_, int mouseX, int mouseY,
				boolean isSelected) {
            boolean flag = (mouseX <= GuiKeyBindingSelection.this.width * .6) && mouseY < y + slotHeight && mouseY > y;
            if(flag)
                GuiKeyBindingSelection.this.mc.fontRendererObj.drawString(EnumChatFormatting.GREEN + this.keyPrettyDesc, x + 190 - GuiKeyBindingSelection.this.maxListLabelWidth, y + slotHeight / 2 - GuiKeyBindingSelection.this.mc.fontRendererObj.FONT_HEIGHT / 2, 16777215);
            else
                GuiKeyBindingSelection.this.mc.fontRendererObj.drawString(EnumChatFormatting.WHITE + this.keyPrettyDesc, x + 190 - GuiKeyBindingSelection.this.maxListLabelWidth, y + slotHeight / 2 - GuiKeyBindingSelection.this.mc.fontRendererObj.FONT_HEIGHT / 2, 16777215);
           
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
        	if(mouseX > GuiKeyBindingSelection.this.width * .6) return false;
        	if(GuiKeyBindingSelection.this.controlsScreen.buttonId == null) return false; //how did u get here?
        	GuiKeyBindingSelection.this.controlsScreen.selectionMode = false;
        	GuiKeyBindingSelection.this.controlsScreen.buttonId.FunctionDesc = this.keyDesc;
        	GuiKeyBindingSelection.this.controlsScreen.bindKey(GuiKeyBindingSelection.this.controlsScreen.buttonId);
        	if(this.keyDesc.startsWith("keyboard")) GuiKeyBindingSelection.this.controlsScreen.waitingForKey = true;
        	return true;
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
        {
        }


    }
}
