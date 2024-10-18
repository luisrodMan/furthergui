package com.ngeneration.furthergui;

import java.util.LinkedList;
import java.util.List;

import com.ngeneration.furthergui.event.ItemEvent;
import com.ngeneration.furthergui.event.ItemListener;

public class ButtonGroup {

	List<FAbstractButton> buttons = new LinkedList<>();

	public void add(FAbstractButton contentTypeOption) {
		buttons.add(contentTypeOption);
		contentTypeOption.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				var item = (FAbstractButton) event.getSource();
				setSelected(item, item.isSelected());
			}
		});
	}

	public int getButtonCount() {
		return buttons.size();
	}

	public FAbstractButton getSelected() {
		return buttons.stream().filter(b -> b.isSelected()).findAny().orElse(null);
	}

	private FAbstractButton initial;

	public void setSelected(FAbstractButton selected, boolean value) {
		if (selected == null)
			throw new RuntimeException();
		boolean init = initial == null;
		if (init)
			initial = selected;
		buttons.stream().filter(b -> b != initial).forEach(b -> b.setSelected(false));
		if (init) {
			initial.setSelected(value);
			initial = null;
		}
	}

}
