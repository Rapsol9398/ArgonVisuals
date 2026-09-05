package com.rapsol.utils;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import static com.rapsol.Argon.mc;

public final class KeyUtils {

	public static CharSequence getKey(int key) {
		switch (key) {
			case GLFW.GLFW_MOUSE_BUTTON_3 -> {
				return printString.of("MMB");
			}
			case GLFW.GLFW_KEY_UNKNOWN -> {
				return printString.of("Unknown");
			}
			case GLFW.GLFW_KEY_ESCAPE -> {
				return printString.of("Esc");
			}
			case GLFW.GLFW_KEY_GRAVE_ACCENT -> {
				return printString.of("Grave Accent");
			}
			case GLFW.GLFW_KEY_WORLD_1 -> {
				return printString.of("World 1");
			}
			case GLFW.GLFW_KEY_WORLD_2 -> {
				return printString.of("World 2");
			}
			case GLFW.GLFW_KEY_PRINT_SCREEN -> {
				return printString.of("Print Screen");
			}
			case GLFW.GLFW_KEY_PAUSE -> {
				return printString.of("Pause");
			}
			case GLFW.GLFW_KEY_INSERT -> {
				return printString.of("Insert");
			}
			case GLFW.GLFW_KEY_DELETE -> {
				return printString.of("Delete");
			}
			case GLFW.GLFW_KEY_HOME -> {
				return printString.of("Home");
			}
			case GLFW.GLFW_KEY_PAGE_UP -> {
				return printString.of("Page Up");
			}
			case GLFW.GLFW_KEY_PAGE_DOWN -> {
				return printString.of("Page Down");
			}
			case GLFW.GLFW_KEY_END -> {
				return printString.of("End");
			}
			case GLFW.GLFW_KEY_TAB -> {
				return printString.of("Tab");
			}
			case GLFW.GLFW_KEY_LEFT_CONTROL -> {
				return printString.of("Left Control");
			}
			case GLFW.GLFW_KEY_RIGHT_CONTROL -> {
				return printString.of("Right Control");
			}
			case GLFW.GLFW_KEY_LEFT_ALT -> {
				return printString.of("Left Alt");
			}
			case GLFW.GLFW_KEY_RIGHT_ALT -> {
				return printString.of("Right Alt");
			}
			case GLFW.GLFW_KEY_LEFT_SHIFT -> {
				return printString.of("Left Shift");
			}
			case GLFW.GLFW_KEY_RIGHT_SHIFT -> {
				return printString.of("Right Shift");
			}
			case GLFW.GLFW_KEY_UP -> {
				return printString.of("Arrow Up");
			}
			case GLFW.GLFW_KEY_DOWN -> {
				return printString.of("Arrow Down");
			}
			case GLFW.GLFW_KEY_LEFT -> {
				return printString.of("Arrow Left");
			}
			case GLFW.GLFW_KEY_RIGHT -> {
				return printString.of("Arrow Right");
			}
			case GLFW.GLFW_KEY_APOSTROPHE -> {
				return printString.of("Apostrophe");
			}
			case GLFW.GLFW_KEY_BACKSPACE -> {
				return printString.of("Backspace");
			}
			case GLFW.GLFW_KEY_CAPS_LOCK -> {
				return printString.of("Caps Lock");
			}
			case GLFW.GLFW_KEY_MENU -> {
				return printString.of("Menu");
			}
			case GLFW.GLFW_KEY_LEFT_SUPER -> {
				return printString.of("Left Super");
			}
			case GLFW.GLFW_KEY_RIGHT_SUPER -> {
				return printString.of("Right Super");
			}
			case GLFW.GLFW_KEY_ENTER -> {
				return printString.of("Enter");
			}
			case GLFW.GLFW_KEY_KP_ENTER -> {
				return printString.of("Numpad Enter");
			}
			case GLFW.GLFW_KEY_NUM_LOCK -> {
				return printString.of("Num Lock");
			}
			case GLFW.GLFW_KEY_SPACE -> {
				return printString.of("Space");
			}
			case GLFW.GLFW_KEY_F1 -> {
				return printString.of("F1");
			}
			case GLFW.GLFW_KEY_F2 -> {
				return printString.of("F2");
			}
			case GLFW.GLFW_KEY_F3 -> {
				return printString.of("F3");
			}
			case GLFW.GLFW_KEY_F4 -> {
				return printString.of("F4");
			}
			case GLFW.GLFW_KEY_F5 -> {
				return printString.of("F5");
			}
			case GLFW.GLFW_KEY_F6 -> {
				return printString.of("F6");
			}
			case GLFW.GLFW_KEY_F7 -> {
				return printString.of("F7");
			}
			case GLFW.GLFW_KEY_F8 -> {
				return printString.of("F8");
			}
			case GLFW.GLFW_KEY_F9 -> {
				return printString.of("F9");
			}
			case GLFW.GLFW_KEY_F10 -> {
				return printString.of("F10");
			}
			case GLFW.GLFW_KEY_F11 -> {
				return printString.of("F11");
			}
			case GLFW.GLFW_KEY_F12 -> {
				return printString.of("F12");
			}
			case GLFW.GLFW_KEY_F13 -> {
				return printString.of("F13");
			}
			case GLFW.GLFW_KEY_F14 -> {
				return printString.of("F14");
			}
			case GLFW.GLFW_KEY_F15 -> {
				return printString.of("F15");
			}
			case GLFW.GLFW_KEY_F16 -> {
				return printString.of("F16");
			}
			case GLFW.GLFW_KEY_F17 -> {
				return printString.of("F17");
			}
			case GLFW.GLFW_KEY_F18 -> {
				return printString.of("F18");
			}
			case GLFW.GLFW_KEY_F19 -> {
				return printString.of("F19");
			}
			case GLFW.GLFW_KEY_F20 -> {
				return printString.of("F20");
			}
			case GLFW.GLFW_KEY_F21 -> {
				return printString.of("F21");
			}
			case GLFW.GLFW_KEY_F22 -> {
				return printString.of("F22");
			}
			case GLFW.GLFW_KEY_F23 -> {
				return printString.of("F23");
			}
			case GLFW.GLFW_KEY_F24 -> {
				return printString.of("F24");
			}
			case GLFW.GLFW_KEY_F25 -> {
				return printString.of("F25");
			}
			case GLFW.GLFW_KEY_SCROLL_LOCK -> {
				return printString.of("Scroll Lock");
			}
			case GLFW.GLFW_KEY_LEFT_BRACKET -> {
				return printString.of("Left Bracket");
			}
			case GLFW.GLFW_KEY_RIGHT_BRACKET -> {
				return printString.of("Right Bracket");
			}
			case GLFW.GLFW_KEY_SEMICOLON -> {
				return printString.of("Semicolon");
			}
			case GLFW.GLFW_KEY_EQUAL -> {
				return printString.of("Equals");
			}
			case GLFW.GLFW_KEY_BACKSLASH -> {
				return printString.of("Backslash");
			}
			case GLFW.GLFW_KEY_COMMA -> {
				return printString.of("Comma");
			}
			case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
				return printString.of("LMB");
			}
			case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
				return printString.of("RMB");
			}
			default -> {
				String keyName = GLFW.glfwGetKeyName(key, 0);
				if (keyName == null) return printString.of("None");
				return StringUtils.capitalize(keyName);
			}
		}
	}

	public static boolean isKeyPressed(int keyCode) {
		if (keyCode <= 8)
			return GLFW.glfwGetMouseButton(mc.getWindow().handle(), keyCode) == GLFW.GLFW_PRESS;

		return GLFW.glfwGetKey(mc.getWindow().handle(), keyCode) == GLFW.GLFW_PRESS;
	}
}
