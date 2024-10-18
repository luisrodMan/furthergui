package com.ngeneration.furthergui;

public class FPopupWindow extends FWindow {

	public void showVisible(FComponent component, int x, int y) {
		setLocationRelativeTo(component);
		setDimension(getPrefferedSize());
		setLocation(x, y);
		FurtherApp.getInstance().invokeLater(() -> {
			setVisible(true);
		});
	}

}
