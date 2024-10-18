package com.ngeneration.furthergui;

import java.util.LinkedList;
import java.util.List;

import com.ngeneration.furthergui.event.WindowEvent;
import com.ngeneration.furthergui.event.WindowListener;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.math.Point;

public class FWindow extends FPanel {

	private boolean firstTimeVisible = false;
	private List<WindowListener> listeners = new LinkedList<>();
	private boolean disposed;
	private FComponent relativeTo;
	private Point location;
	private boolean alwaysOnTop;

	public FWindow() {
		super.setVisible(false);
		super.setPadding(new Padding());
	}

	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		location = new Point(x, y);
	}

	public void setVisible(boolean value) {
		if ((value && isVisible()) || (!value && !isVisible()))
			return;

		Dimension size = getDimension();
		var relativeLocation = new Point();
		Dimension relativeSize = FurtherApp.getInstance().getDimension();
		if (relativeTo != null) {
			relativeLocation = relativeTo.getScreenLocation();
			relativeSize = relativeTo.getDimension();
		}
		var loc = relativeSize.substract(size).divide(2);
		if (this.location != null)
			loc = new Dimension(this.location);
		loc.add(relativeLocation);

		super.setLocation(loc.width, loc.height);

		if (value) {
			FurtherApp.getInstance().addWindow(this);
			if (!isValidated())
				validate();
			if (!firstTimeVisible) {
				firstTimeVisible = true;
				WindowEvent event = new WindowEvent(this);
				for (var listener : listeners) {
					if (event.isConsumed())
						break;
					listener.windowOpenned(event);
				}
			}
		} else
			FurtherApp.getInstance().removeWindow(this);
		if (value && !isValidated())
			validate();
		super.setVisible(value);
	}

	public void dispose() {
		if (disposed)
			throw new RuntimeException("Windows already disposed");
		disposed = true;
		if (isVisible())
			setVisible(false);
		WindowEvent event = new WindowEvent(this);
		for (var listener : listeners) {
			if (event.isConsumed())
				break;
			listener.windowClossin(event);
		}
	}

	public void addWindowsListener(WindowListener windowListener) {
		listeners.add(windowListener);
	}

	public void removeWindowsListener(WindowListener windowListener) {
		listeners.remove(windowListener);
	}

	public void setLocationRelativeTo(FComponent point) {
		relativeTo = point;
	}

	public void setAlwaysOnTop(boolean alwaysOnTop) {
		this.alwaysOnTop = alwaysOnTop;
	}

	public boolean isAlwaysOnTop() {
		return alwaysOnTop;
	}

}
