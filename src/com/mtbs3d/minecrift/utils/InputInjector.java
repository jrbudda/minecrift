package com.mtbs3d.minecrift.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import com.google.common.base.Throwables;

/**
 * This is super dirty hacks into LWJGL core code.
 * Do not look unless you want your eyeballs to implode.
 */
public class InputInjector {
	private static boolean supportChecked = false;
	private static boolean supported = true;
	//private static Method putMouseEvent;
	private static Method putKeyboardEvent;
	private static Method putMouseEventWithCoords;
	private static Object keyboard;
	private static Object mouse;
	private static Class displayClass;
	private static Class keyboardClass;
	private static Class mouseClass;
	
	private static void checkSupported() {
		if (!supportChecked) {
			supported = LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS || LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_LINUX;
			if (supported) System.out.println("Yay, InputInjector is supported on this platform!");
			else System.out.println("InputInjector is not supported on this platform, will fall back to less compatible methods.");
			supportChecked = true;
		}
	}
	
	private static boolean loadObjects() throws ReflectiveOperationException {
		checkSupported();
		if (supported) { // They need to be loaded every time because apparently they can be recreated without warning
			if (displayClass == null) loadClasses();
			Object displayImpl = getFieldValue(Display.class, "display_impl", null);
			switch (LWJGLUtil.getPlatform()) {
				case LWJGLUtil.PLATFORM_WINDOWS:
					keyboard = getFieldValue(displayClass, "keyboard", displayImpl);
					mouse = getFieldValue(displayClass, "mouse", displayImpl);
					break;
				case LWJGLUtil.PLATFORM_LINUX:
					keyboard = getFieldValue(displayClass, "keyboard", displayImpl);
					mouse = getFieldValue(displayClass, "mouse", displayImpl);
					break;
			}
		}
		return supported;
	}
	
	private static void loadClasses() throws ReflectiveOperationException {
		switch (LWJGLUtil.getPlatform()) {
			case LWJGLUtil.PLATFORM_WINDOWS:
				displayClass = Class.forName("org.lwjgl.opengl.WindowsDisplay");
				keyboardClass = Class.forName("org.lwjgl.opengl.WindowsKeyboard");
				mouseClass = Class.forName("org.lwjgl.opengl.WindowsMouse");
				break;
			case LWJGLUtil.PLATFORM_LINUX:
				displayClass = Class.forName("org.lwjgl.opengl.LinuxDisplay");
				keyboardClass = Class.forName("org.lwjgl.opengl.LinuxKeyboard");
				mouseClass = Class.forName("org.lwjgl.opengl.LinuxMouse");
				break;
		}
	}
	
	private static Object getFieldValue(Class clazz, String name, Object obj) throws ReflectiveOperationException {
		Field field = clazz.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(obj);
	}
	
	private static void putMouseEventWithCoords(int button, boolean state, int coord1, int coord2, int dz, long nanos) throws ReflectiveOperationException {
		if (!loadObjects()) return;
		if (putMouseEventWithCoords == null) {
			putMouseEventWithCoords = mouseClass.getDeclaredMethod("putMouseEventWithCoords", Byte.TYPE, Byte.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Long.TYPE);
			putMouseEventWithCoords.setAccessible(true);
		}
		putMouseEventWithCoords.invoke(mouse, (byte)button, state ? (byte)1 : (byte)0, coord1, coord2, dz, nanos);
	}
	
	private static void putKeyboardEvent(int keycode, boolean state, int ch) throws ReflectiveOperationException {
		if (!loadObjects()) return;
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
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void releaseKey(int code, char ch) {
		try {
			putKeyboardEvent(code, false, ch);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void typeKey(int code, char ch) {
		pressKey(code, ch);
		releaseKey(code, ch);
	}
	
	public static void mouseMoveEvent(int x, int y) {
		try {
			int dx = x - Mouse.getX();
			int dy = y - Mouse.getY(); // TODO: Find out if linux needs transformed (inverted) Y
			if (dx != 0 || dy != 0) {
				long nanos = System.nanoTime();
				if (Mouse.isGrabbed()) {
					putMouseEventWithCoords(-1, false, dx, dy, 0, nanos);
				} else {
					putMouseEventWithCoords(-1, false, x, y, 0, nanos);
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isSupported() {
		checkSupported();
		return supported;
	}
}
