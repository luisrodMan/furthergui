package com.ngeneration.furthergui;

import java.util.ArrayList;
import java.util.List;

import com.ngeneration.furthergui.FRadioButton.RadioIcon;
import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.ActionEvent;
import com.ngeneration.furthergui.event.ActionListener;
import com.ngeneration.furthergui.event.ItemEvent;
import com.ngeneration.furthergui.event.ItemListener;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.FFont;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.graphics.Icon;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;

public class FAbstractButton extends FComponent {

	public static final int LEFT_ALIGN = 0;
	public static final int CENTER_ALIGN = 1;

	private FFont font;
	private String text;
	private boolean selected;
	private List<ActionListener> listeners = new ArrayList<>(1);
	private List<ItemListener> itemListeners = new ArrayList<>(1);
	private Icon icon, selectedIcon;
	private int iconGap = 5;
	private Color hoverColor = Color.LIGTH_GRAY.darker().darker();
	private boolean opaqueOnHover = true;

	private int align = LEFT_ALIGN;

	private static Color defDisabledForeground = new Color(120, 120, 120);
	private Color disabledForeground = defDisabledForeground;

	public FAbstractButton(String text, Icon icon) {
		this.text = text;
		this.icon = icon;
		setFont(FurtherApp.getInstance().getDefaultFont());
		var padding = new Padding(5, 5);
		setPadding(padding);

//		setPrefferedSize(padding.toDimension().add(w, h));
		setBackground(Color.GRAY);
	}

	public void setAlign(int align) {
		this.align = align;
	}

	public int getAlign() {
		return align;
	}

	@Override
	public Dimension getPrefferedSize() {
		int w = text == null ? 0 : getFont().getStringWidth(text, 0, text.length());
		int h = getFont().getFontHeight();
		if (icon != null) {
			w += icon.getWidth() + (text != null && !text.isEmpty() ? iconGap : 0);
			h = Math.max(h, icon.getHeight());
		}
		return getPadding().toDimension().add(w, h);
	}

	public void setHoverBackground(Color hoverColor) {
		this.hoverColor = hoverColor;
	}

	public Color getHoverColor() {
		return hoverColor;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setSelectedIcon(RadioIcon radioIcon) {
		selectedIcon = radioIcon;
	}

	public Icon getSelectedIcon() {
		return selectedIcon;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		boolean change = this.selected != selected;
		this.selected = selected;
		if (change) {
			var event = new ItemEvent(this);
			itemListeners.stream().filter((v) -> !event.isConsumed()).peek((l) -> l.itemStateChanged(event)).count();
			repaint();
		}
	}

	public void setAction(Action action) {

	}

	public void setText(String string) {
		this.text = string;
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		super.processMouseEvent(event);
		if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == MouseEvent.MOUSE_EXITED) {
			repaint();
		}
		if (isEnabled() && !event.isConsumed() && event.getEventType() == MouseEvent.MOUSE_RELEASED
				&& containsOnVisible(event.getLocation())) {
			event.consume();
			fireActionEvent();
		}
	}

	protected void fireActionEvent() {
		listeners.forEach(l -> l.actionPerformed(new ActionEvent(this)));
	}

	public void addActionListener(ActionListener actionListener) {
		if (actionListener == null)
			throw new RuntimeException("xddx" + this);
		listeners.add(actionListener);
	}

	public void removeActionListener(ActionListener actionListener) {
		listeners.remove(actionListener);
	}

	public void setFont(FFont font) {
		this.font = font;
	}

	public FFont getFont() {
		return font;
	}

	public String getText() {
		return text;
	}

	public void doClick() {
		fireActionEvent();
	}

	public boolean isOpaqueOnHover() {
		return opaqueOnHover;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (isOpaque() || isOpaqueOnHover()) {
			g.setColor(hasStatus(MOUSE_ENTERED) ? hoverColor : getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		var icon = isSelected() ? selectedIcon : this.icon;
		if (icon == null)
			icon = this.icon;

		int x = getPadding().left;
		if (align == CENTER_ALIGN) {
			int t = icon.getWidth();
			if (text != null)
				t += getFont().getStringWidth(text);
			t += icon != null && text != null ? iconGap : 0;
			x = (int) ((getWidth() - t) * 0.5f);
		}

		if (icon != null) {
			icon.paint(x, (getHeight() - icon.getHeight()) / 2, g);
			x += icon.getWidth() + iconGap;
		}
		String text = getText();
		if (text != null) {
			g.setFont(getFont());
//			g.setColor(Color.CYAN);
//			g.fillRect(x, (getHeight() - font.getFontHeight()) * 0.5f, 60, font.getFontHeight());
			g.setColor(!isEnabled() ? disabledForeground : getForeground());
			g.drawString(x, (getHeight() - font.getFontHeight()) * 0.5f, text);
		}
	}

	public void addItemListener(ItemListener itemListener) {
		itemListeners.add(itemListener);
	}

}
