package com.ngeneration.furthergui.text;

public class TextUtils {

	public static int getNotSpaceIndex(String text, int from) {
		for (; from < text.length(); from++)
			if (!Character.isWhitespace(text.charAt(from)))
				return from;
		return -1;
	}
	
	

}
