package com.mtbs3d.minecrift.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.Display;

import com.google.common.base.Throwables;

/**
 * This is super dirty hacks into LWJGL core code.
 * Do not look unless you want your eyeballs to implode.
 */
public class InputInjector {
	private static boolean supported = true;
	//private static Method putMouseEvent;
	private static Method putKeyboardEvent;
	private static Object keyboard;
	private static Object mouse;
	private static Class keyboardClass;
	private static Class mouseClass;
	
	private static void lazyLoad() {
		if (supported && (keyboard == null || mouse == null)) {
			try {
				Object displayImpl = getFieldValue(Display.class, "display_impl", null);
				switch (LWJGLUtil.getPlatform()) {
					case LWJGLUtil.PLATFORM_WINDOWS:
						keyboard = getFieldValue(Class.forName("org.lwjgl.opengl.WindowsDisplay"), "keyboard", displayImpl);
						mouse = getFieldValue(Class.forName("org.lwjgl.opengl.WindowsDisplay"), "mouse", displayImpl);
						keyboardClass = Class.forName("org.lwjgl.opengl.WindowsKeyboard");
						mouseClass = Class.forName("org.lwjgl.opengl.WindowsMouse");
						break;
					case LWJGLUtil.PLATFORM_LINUX:
						keyboard = getFieldValue(Class.forName("org.lwjgl.opengl.LinuxDisplay"), "keyboard", displayImpl);
						mouse = getFieldValue(Class.forName("org.lwjgl.opengl.LinuxDisplay"), "mouse", displayImpl);
						keyboardClass = Class.forName("org.lwjgl.opengl.LinuxKeyboard");
						mouseClass = Class.forName("org.lwjgl.opengl.LinuxMouse");
						break;
					default:
						supported = false;
						System.out.println("InputInjector is not supported on this platform");
						return;
				}
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		}
	}
	
	private static Object getFieldValue(Class clazz, String name, Object obj) throws ReflectiveOperationException {
		Field field = clazz.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(obj);
	}
	
	private static void putKeyboardEvent(int keycode, boolean state, int ch) throws ReflectiveOperationException {
		lazyLoad();
		boolean windows = LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS;
		if (putKeyboardEvent == null) {
			putKeyboardEvent = keyboardClass.getDeclaredMethod(windows ? "putEvent" : "putKeyboardEvent", Integer.TYPE, Byte.TYPE, Integer.TYPE, Long.TYPE, Boolean.TYPE);
			putKeyboardEvent.setAccessible(true);
		}
		putKeyboardEvent.invoke(keyboard, keycode, state ? (byte)1 : (byte)0, ch, windows ? System.nanoTime() / 1000000 : System.nanoTime(), false);
	}
	
	public static void pressKey(int code, char ch) {
		try {
			putKeyboardEvent(code, true, ch);
		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}
	
	public static void releaseKey(int code, char ch) {
		try {
			putKeyboardEvent(code, false, ch);
		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}
	
	public static void typeKey(int code, char ch) {
		pressKey(code, ch);
		releaseKey(code, ch);
	}
	
	public static boolean isSupported() {
		return supported;
	}
}
