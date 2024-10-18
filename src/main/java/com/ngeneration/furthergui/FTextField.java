package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.text.FTextComponent;

public class FTextField extends FTextComponent {

	public FTextField() {
		this("");
	}
	public FTextField(int columns) {
		this(columns, "");
	}
	
	public FTextField(String value) {
		this(0, value);
	}

	public FTextField(int cols, String text) {
		super(text);
		setCenterVertical(true);
		setPadding(new Padding(5));
		setFocusedColor(Color.BLACK.lighter());
	}
}
