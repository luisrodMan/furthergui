package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.graphics.Icon;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.Layout;
import com.ngeneration.furthergui.math.Padding;

public class FFrame extends FWindow {

	public static final int EXIT_ON_CLOSE = 1;

	private String title;
	private FComponent container = new FPanel();

	private int defaultCloseOperation;

	public FFrame() {
		this(null);
	}

	public FFrame(String title) {
		super.setLayout(new BorderLayout());
		super.setPadding(new Padding());
		container.setLayout(new BorderLayout());
		container.setPadding(new Padding());
		setTitle(title);
		super.add(container, BorderLayout.CENTER);
		super.add(createHeader(), BorderLayout.NORTH);
		setName("window");
		container.setName("window-container ");
	}

	protected FComponent createHeader() {
		return new DefaultFrameHeader(this);
	}

	@Override
	public void setLayout(Layout layout) {
		getContainerComponent().setLayout(layout);
	}

	public void setTitle(String string) {
		this.title = string;
	}

	public void setDefaultCloseOperation(int operation) {
		defaultCloseOperation = operation;
	}

	public FComponent getContainerComponent() {
		return container;
	}

	public void setMenuBar(FMenuBar menubar) {

	}

	public void pack() {
		setDimension(getPrefferedSize());
	}

	public String getTitle() {
		return title;
	}

	public static class DefaultFrameHeader extends FPanel {

		public static int BUTTON_MINIMIZE = 1;
		public static int BUTTON_MAZIMIZE = 2;
		public static int BUTTON_CLOSE = 4;

		private FFrame frame;
		private int buttons;

		public DefaultFrameHeader(FFrame frame) {
			this(frame, BUTTON_MINIMIZE | BUTTON_MAZIMIZE | BUTTON_CLOSE);
		}

		public DefaultFrameHeader(FFrame frame, int buttons) {
			super(new BorderLayout());
			this.frame = frame;
			setButtons(buttons);
			setBackground(new Color(55, 65, 65));
		}

		private void setButtons(int buttons) {
			this.buttons = buttons;
			updateUI();
		}

		private void updateUI() {
			removeAll();
			FPanel buttonsPanel = new FPanel(new FlowLayout(FlowLayout.RIGHT, 0, false));
			if ((buttons & BUTTON_CLOSE) == BUTTON_CLOSE)
				buttonsPanel.add(new DefaultButton(new CloseIcon(), BUTTON_CLOSE));
			if ((buttons & BUTTON_MAZIMIZE) == BUTTON_MAZIMIZE)
				buttonsPanel.add(new DefaultButton(new MaximizeIcon(), BUTTON_MAZIMIZE));
			if ((buttons & BUTTON_MINIMIZE) == BUTTON_MINIMIZE)
				buttonsPanel.add(new DefaultButton(new MenimizeIcon(), BUTTON_MINIMIZE));

			setPadding(new Padding(15, 0, 0, 0));
			if (frame.getTitle() != null) {
				var label = new FLabel(frame.getTitle());
				label.setForeground(Color.WHITE);
				add(label, BorderLayout.WEST);
			}
			buttonsPanel.setBackground(Color.TRANSLUCENT);
			add(buttonsPanel, BorderLayout.EAST);
		}

		private class DefaultButton extends FButton {

			public DefaultButton(Icon menimizeIcon, int type) {
				super(menimizeIcon);
				setPadding(new Padding(15, 4));
				setBackground(Color.TRANSLUCENT);
				setHoverBackground(Color.DARK_GRAY.withAlpha(100));
				addActionListener(e -> {
					if (type == BUTTON_CLOSE) {
						if (frame instanceof FDialog)
							frame.dispose();
						else
							throw new RuntimeException("Not implemented");
					}
				});
			}

		};

		private static final int ICON_SIZE = 10;

		private class MenimizeIcon extends DrawableIcon {

			public MenimizeIcon() {
				super(ICON_SIZE);
			}

			@Override
			public void paint(int x, int y, Graphics g) {
				g.setColor(Color.LIGTH_GRAY);
				g.setPenSize(1);
				g.drawLine(x, y + getHeight() / 2, x + getWidth(), y + getHeight() / 2);
			}
		}

		private class MaximizeIcon extends DrawableIcon {

			public MaximizeIcon() {
				super(ICON_SIZE);
			}

			@Override
			public void paint(int x, int y, Graphics g) {
				g.setColor(Color.LIGTH_GRAY);
				g.setPenSize(1);
				g.drawRect(x, y, getWidth(), getHeight());
			}

		}

		private class CloseIcon extends DrawableIcon {
			public CloseIcon() {
				super(ICON_SIZE);
			}

			@Override
			public void paint(int x, int y, Graphics g) {
				g.setColor(Color.LIGTH_GRAY);
				g.setPenSize(1);
				g.drawLine(x, y, x + getWidth(), y + getHeight());
				g.drawLine(x, y + getHeight(), x + getWidth(), y);
			}
		}

	}

}
