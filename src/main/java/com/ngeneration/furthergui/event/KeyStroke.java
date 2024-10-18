package com.ngeneration.furthergui.event;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyStroke {

	private int modifiers;
	private int[] keys;

	public static KeyStroke getKeyStroke(String binding) {
		String[] datas = binding.trim().split("\\s+");
		int modifiers = 0;
		List<Integer> keys = new LinkedList<>();
		for (String data : datas) {
			if (data.equalsIgnoreCase("ctrl") || data.equalsIgnoreCase("CONTROL"))
				modifiers |= KeyEvent.CTRL_MASK;
			else if (data.equalsIgnoreCase("SHIFT"))
				modifiers |= KeyEvent.SHIFT_MASK;
			else if (data.equalsIgnoreCase("ALT"))
				modifiers |= KeyEvent.ALT_MASK;
			else {
				try {
					var field = KeyEvent.class.getField("VK_" + data.toUpperCase());
					keys.add(field.getInt(null));
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
						| IllegalAccessException e) {
					throw new RuntimeException("Invalid binding key: " + data, e);
				}
			}
		}
		int[] keyData = new int[keys.size()];
		for (int i = 0; i < keyData.length; i++)
			keyData[i] = keys.get(i);
		return new KeyStroke(modifiers, keyData);
	}

	public static KeyStroke getKeyStroke(String vkS, int ctrlMask) {
		throw new RuntimeException("Not implemented");
	}

	public static KeyStroke getKeyStroke(int keyCode, int modifiers) {
		return new KeyStroke(modifiers, new int[] { keyCode });
	}

}
