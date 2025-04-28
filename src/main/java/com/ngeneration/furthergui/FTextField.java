package com.ngeneration.furthergui;

import com.ngeneration.furthergui.event.FocusEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.text.DocumentFilter;
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
		setCenteredVertical(true);
		setPadding(new Padding(5));
		setFocusedColor(Color.BLACK.lighter());
		super.setDocumentFilter(new DocumentFilter() {

			@Override
			public void remove(Bypass filter, int offset, int length) {
				filter.remove(offset, length);
			}

			@Override
			public void insertString(Bypass filter, int offset, String string) {
				string = string.replaceAll("\r?\n", "");
				filter.insertString(offset, string);
			}

			@Override
			public void replace(Bypass filter, int offset, int length, String text) {
				text = text.replaceAll("\r?\n", "");
				filter.replace(offset, length, text);
			}

		});
	}

	@Override
	protected void processFocusEvent(FocusEvent focusEvent) {
		super.processFocusEvent(focusEvent);
		if (!focusEvent.isConsumed() && !focusEvent.isFocusGained())
			clearSelection();
	}

}
