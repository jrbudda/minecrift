package com.mtbs3d.minecrift.utils;

import static java.awt.event.KeyEvent.*;

import java.awt.AWTException;
import java.awt.Robot;

public class KeyboardSimulator {
	public static Robot robot;

	static{
        try {
			robot = new Robot();
		} catch (AWTException e) {
		}		
	}

    public static void type(CharSequence characters) {
        int length = characters.length();
        for (int i = 0; i < length; i++) {
            char character = characters.charAt(i);
            type(character);
        }
    }

    public static void press(char character){
        int[] chars = getCodes(character);
        for (int i : chars) {
        	  robot.keyPress(i);
		}
    }
    
    public static void unpress(char character){
        int[] chars = getCodes(character);
        for (int i : chars) {
        	  robot.keyRelease(i);
		}
    }
    
	public static void type(char character) {
        int[] chars = getCodes(character);
        doType(chars, 0, chars.length);
	}
    
    public static int[] getCodes(char character) {
        switch (character) {
	        case 'a': return codes(VK_A); 
	        case 'b': return codes(VK_B); 
	        case 'c': return codes(VK_C); 
	        case 'd': return codes(VK_D); 
	        case 'e': return codes(VK_E); 
	        case 'f': return codes(VK_F); 
	        case 'g': return codes(VK_G); 
	        case 'h': return codes(VK_H); 
	        case 'i': return codes(VK_I); 
	        case 'j': return codes(VK_J); 
	        case 'k': return codes(VK_K); 
	        case 'l': return codes(VK_L); 
	        case 'm': return codes(VK_M); 
	        case 'n': return codes(VK_N); 
	        case 'o': return codes(VK_O); 
	        case 'p': return codes(VK_P); 
	        case 'q': return codes(VK_Q); 
	        case 'r': return codes(VK_R); 
	        case 's': return codes(VK_S); 
	        case 't': return codes(VK_T); 
	        case 'u': return codes(VK_U); 
	        case 'v': return codes(VK_V); 
	        case 'w': return codes(VK_W); 
	        case 'x': return codes(VK_X); 
	        case 'y': return codes(VK_Y); 
	        case 'z': return codes(VK_Z); 
	        case 'A': return codes(VK_SHIFT, VK_A); 
	        case 'B': return codes(VK_SHIFT, VK_B); 
	        case 'C': return codes(VK_SHIFT, VK_C); 
	        case 'D': return codes(VK_SHIFT, VK_D); 
	        case 'E': return codes(VK_SHIFT, VK_E); 
	        case 'F': return codes(VK_SHIFT, VK_F); 
	        case 'G': return codes(VK_SHIFT, VK_G); 
	        case 'H': return codes(VK_SHIFT, VK_H); 
	        case 'I': return codes(VK_SHIFT, VK_I); 
	        case 'J': return codes(VK_SHIFT, VK_J); 
	        case 'K': return codes(VK_SHIFT, VK_K); 
	        case 'L': return codes(VK_SHIFT, VK_L); 
	        case 'M': return codes(VK_SHIFT, VK_M); 
	        case 'N': return codes(VK_SHIFT, VK_N); 
	        case 'O': return codes(VK_SHIFT, VK_O); 
	        case 'P': return codes(VK_SHIFT, VK_P); 
	        case 'Q': return codes(VK_SHIFT, VK_Q); 
	        case 'R': return codes(VK_SHIFT, VK_R); 
	        case 'S': return codes(VK_SHIFT, VK_S); 
	        case 'T': return codes(VK_SHIFT, VK_T); 
	        case 'U': return codes(VK_SHIFT, VK_U); 
	        case 'V': return codes(VK_SHIFT, VK_V); 
	        case 'W': return codes(VK_SHIFT, VK_W); 
	        case 'X': return codes(VK_SHIFT, VK_X); 
	        case 'Y': return codes(VK_SHIFT, VK_Y); 
	        case 'Z': return codes(VK_SHIFT, VK_Z); 
	        case '`': return codes(VK_BACK_QUOTE); 
	        case '0': return codes(VK_0); 
	        case '1': return codes(VK_1); 
	        case '2': return codes(VK_2); 
	        case '3': return codes(VK_3); 
	        case '4': return codes(VK_4); 
	        case '5': return codes(VK_5); 
	        case '6': return codes(VK_6); 
	        case '7': return codes(VK_7); 
	        case '8': return codes(VK_8); 
	        case '9': return codes(VK_9); 
	        case '-': return codes(VK_MINUS); 
	        case '=': return codes(VK_EQUALS); 
	        case '~': return codes(VK_SHIFT,VK_BACK_QUOTE); 
	        case '!': return codes(VK_SHIFT,VK_1); 
	        case '@': return codes(VK_SHIFT,VK_2); 
	        case '#': return codes(VK_SHIFT,VK_3); 
	        case '$': return codes(VK_SHIFT,VK_4); 
	        case '%': return codes(VK_SHIFT, VK_5); 
	        case '^': return codes(VK_SHIFT,VK_6); 
	        case '&': return codes(VK_SHIFT,VK_7); 
	        case '*': return codes(VK_SHIFT,VK_8); 
	        case '(': return codes(VK_SHIFT,VK_9); 
	        case ')': return codes(VK_SHIFT,VK_0); 
	        case '_': return codes(VK_SHIFT,VK_MINUS); 
	        case '+': return codes(VK_SHIFT,VK_EQUALS); 
	        case '\t': return codes(VK_TAB); 
	        case '\n': return codes(VK_ENTER); 
	        case '[': return codes(VK_OPEN_BRACKET); 
	        case ']': return codes(VK_CLOSE_BRACKET); 
	        case '\\': return codes(VK_BACK_SLASH); 
	        case '{': return codes(VK_SHIFT, VK_OPEN_BRACKET); 
	        case '}': return codes(VK_SHIFT, VK_CLOSE_BRACKET); 
	        case '|': return codes(VK_SHIFT, VK_BACK_SLASH); 
	        case ';': return codes(VK_SEMICOLON); 
	        case ':': return codes(VK_SHIFT,VK_SEMICOLON); 
	        case '\'': return codes(VK_QUOTE); 
	        case '"': return codes(VK_SHIFT,VK_QUOTE); 
	        case ',': return codes(VK_COMMA); 
	        case '<': return codes(VK_SHIFT, VK_COMMA); 
	        case '.': return codes(VK_PERIOD); 
	        case '>': return codes(VK_SHIFT, VK_PERIOD); 
	        case '/': return codes(VK_SLASH); 
	        case '?': return codes(VK_SHIFT, VK_SLASH); 
	        case ' ': return codes(VK_SPACE); 
        	case '\b': return codes(VK_BACK_SPACE); 
        	case '\r': return codes(VK_ENTER); 
	        default:
	            throw new IllegalArgumentException("Cannot type character " + character);
        }
    }

    private static int[] codes(int... keyCodes) {
        return keyCodes;
    }

    private static void doType(int[] keyCodes, int offset, int length) {
    	try {
			
        if (length == 0) {
            return;
        }
        robot.keyPress(keyCodes[offset]);
        doType(keyCodes, offset + 1, length - 1);
        robot.keyRelease(keyCodes[offset]);
		} catch (Exception e) {
			System.out.println("Cannot type keycode: " + keyCodes[offset]);
		}
    }


}
