package com.ngeneration.furthergui;

public class FPopupWindow extends FWindow {

	public void showVisible(FComponent component, int x, int y) {
		setLocationRelativeTo(component);
		setDimension(getPrefferedSize());
		var dim = getDimension();
		int xx = x + (component == null ? 0 : component.getScreenLocation().getX());
		int yy = y + (component == null ? 0 : component.getScreenLocation().getY());
		if (xx + dim.width > FurtherApp.getInstance().getWidth())
			x = FurtherApp.getInstance().getWidth() - dim.width
					- (component == null ? 0 : component.getScreenLocation().getX());
		if (yy + dim.height > FurtherApp.getInstance().getHeight())
			y = FurtherApp.getInstance().getHeight() - dim.height
					- (component == null ? 0 : component.getScreenLocation().getY());
		setLocation(x, y);
		FurtherApp.getInstance().invokeLater(() -> {
			setVisible(true);
		});
	}

}
