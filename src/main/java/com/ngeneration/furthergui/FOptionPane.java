package com.ngeneration.furthergui;

import java.util.function.BiConsumer;

import com.ngeneration.furthergui.event.ChangeListener;
import com.ngeneration.furthergui.event.KeyStroke;
import com.ngeneration.furthergui.event.PropertyListener;
import com.ngeneration.furthergui.event.WindowAdapter;
import com.ngeneration.furthergui.event.WindowEvent;
import com.ngeneration.furthergui.event.WindowListener;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.GridBagConstraints;
import com.ngeneration.furthergui.layout.GridBagLayout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;

public class FOptionPane {

	public static final int YES = 2;
	public static final int NO = 1;

	public static Color bg = new Color(40, 40, 40);

	public static int showDialog(String title, FComponent content, WindowListener listener) {
		FDialog dialog = new FDialog(title);
		dialog.getContainerComponent().setPadding(new Padding(25, 10, 25, 15));
		dialog.getContainerComponent().setLayout(new FlowLayout(FlowLayout.TOP_TO_BOTTOM, 10, true));
		dialog.getContainerComponent().add(content);

		FPanel buttonsPanel = new FPanel(new FlowLayout(FlowLayout.RIGHT));
		FButton aceptBtn = new FButton("Accept");
		FButton cancelBtn = new FButton("Cancel");
		buttonsPanel.add(cancelBtn);
		buttonsPanel.add(aceptBtn);
		buttonsPanel.add(new FLabel(" ".repeat(50)));
		dialog.getContainerComponent().add(buttonsPanel);
		int[] pressedBtn = new int[] { NO };
		aceptBtn.addActionListener(e -> {
			pressedBtn[0] = YES;
			dialog.dispose();
		});
		cancelBtn.addActionListener(e -> {
			dialog.dispose();
		});

		FTextField text = null;
		// hack
		if (content instanceof FTextField t)
			text = t;
		else if (content != null) {
			text = (FTextField) content.getComponents().stream().filter(FTextField.class::isInstance).findAny()
					.orElse(null);
		}
		if (text != null) {
			text.getInputMap(FComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "fire");
			text.getActionMap().put("fire", (e) -> {
				aceptBtn.doClick();
			});
		} else {
			listener = new WindowAdapter() {
				@Override
				public void windowOpenned(WindowEvent event) {
					aceptBtn.requestFocus();
				}
			};
		}

		dialog.getContainerComponent().setBackground(bg);
		buttonsPanel.setBackground(bg);

		dialog.pack();
		if (listener != null)
			dialog.addWindowsListener(listener);
		dialog.setVisible(true);
		return pressedBtn[0];
	}

	public static String showInputDialog(String title) {
		return showInputDialog(title, "");
	}

	public static String showInputDialog(String title, String initialText) {
		return showInputDialog(title, "", initialText);
	}

	public static String showInputDialog(String title, String msg, String initialText) {
		FTextField input = new FTextField(initialText);
		input.selectAll();
		FComponent content = input;
		content = new FPanel(new BorderLayout(0, 10));
		content.setBackground(bg);
		if (msg != null) {
			content.add(new FLabel(msg), BorderLayout.NORTH);
		}
		content.add(input, BorderLayout.CENTER);
		return showDialog(title, content, new WindowAdapter() {
			@Override
			public void windowOpenned(WindowEvent event) {
				input.requestFocus();
			}
		}) == 1 ? null : input.getText();
	}

	public static int showConfirmDialog(FComponent parent, String title, String msg) {
		return showDialog(title, new FLabel(msg), new WindowAdapter());
	}

	public static Color showColorDialog(FComponent parent, Color initialColor) {
		int margin = 5;
		FPanel container = new FPanel(new GridBagLayout(5, 3));
		container.setPrefferedSize(new Dimension(300, 200));
		FPanel sampleCanvas = new FPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		FSlider sliderRed = new FSlider();
		sliderRed.setSize(12);
		sliderRed.setForeground(Color.RED);
		FSlider sliderGreen = new FSlider();
		sliderGreen.setSize(12);
		sliderGreen.setForeground(Color.GREEN);
		FSlider sliderBlue = new FSlider();
		sliderBlue.setSize(12);
		sliderBlue.setForeground(Color.BLUE);
		FSlider sliderAlpha = new FSlider();
		sliderAlpha.setSize(12);
		sliderAlpha.setForeground(Color.LIGTH_GRAY);

		FTextField txt1 = new FTextField();
		txt1.setText("255");
		txt1.setPrefferedSize(txt1.getPrefferedSize());
		FTextField txt2 = new FTextField();
		FTextField txt3 = new FTextField();
		FTextField txt4 = new FTextField();
		txt2.setPrefferedSize(txt1.getPrefferedSize());
		txt3.setPrefferedSize(txt1.getPrefferedSize());
		txt4.setPrefferedSize(txt1.getPrefferedSize());

		final BiConsumer<Color, FComponent> colorListener = (color, comp) -> {
			if (comp instanceof FSlider) {
				txt1.setText("" + color.getRedInt());
				txt2.setText("" + color.getGreenInt());
				txt3.setText("" + color.getBlueInt());
				txt4.setText("" + color.getAlphaInt());
			} else {
				sliderRed.setValue(color.getRed());
				sliderGreen.setValue(color.getGreen());
				sliderBlue.setValue(color.getBlue());
				sliderAlpha.setValue(color.getAlpha());
			}
			sampleCanvas.setBackground(color);
			sampleCanvas.repaint();
		};
		colorListener.accept(initialColor, sliderRed);
		colorListener.accept(initialColor, txt1);

		PropertyListener textListener = p -> {
			if (p.getProperty().equals(FTextField.TEXT_PROPERTY))
				colorListener.accept(new Color(Integer.parseInt(txt1.getText()), Integer.parseInt(txt2.getText()),
						Integer.parseInt(txt3.getText()), Integer.parseInt(txt4.getText())), p.getSource());
		};
		ChangeListener chgListener = e -> {
			Color newColor = new Color((int) (sliderRed.getValue() * 255), (int) (sliderGreen.getValue() * 255),
					(int) (sliderBlue.getValue() * 255), (int) (sliderAlpha.getValue() * 255));
			colorListener.accept(newColor, e.getSource());
		};
		txt1.addPropertyListener(textListener);
		txt2.addPropertyListener(textListener);
		txt3.addPropertyListener(textListener);
		txt4.addPropertyListener(textListener);
		sliderRed.addChangeListener(chgListener);
		sliderGreen.addChangeListener(chgListener);
		sliderBlue.addChangeListener(chgListener);
		sliderAlpha.addChangeListener(chgListener);

		var constraints = new GridBagConstraints(0, 0, 1, 3, 1, 1);
		sampleCanvas.setBackground(initialColor);
		constraints.fillHorizontal = true;
		constraints.fillVertical = true;
		container.add(sampleCanvas, constraints);

		constraints = new GridBagConstraints(1, 0, 1, 1, 0, 0);
		constraints.anchor = GridBagConstraints.LEFT;
		container.add(new FLabel("R:"), constraints);
		constraints = new GridBagConstraints(1, 1, 1, 1, 1, 0);
		constraints.fillHorizontal = true;
		constraints.anchor = GridBagConstraints.LEFT;
		constraints.margin = new Padding(margin, 0);
		container.add(sliderRed, constraints);
		constraints = new GridBagConstraints(1, 2, 1, 1, 0, 0);
		container.add(txt1, constraints);

		constraints = new GridBagConstraints(2, 0, 1, 1, 0, 0);
		constraints.anchor = GridBagConstraints.LEFT;
		container.add(new FLabel("G:"), constraints);
		constraints = new GridBagConstraints(2, 1, 1, 1, 1, 0);
		constraints.fillHorizontal = true;
		constraints.anchor = GridBagConstraints.LEFT;
		constraints.margin = new Padding(margin, 0);
		container.add(sliderGreen, constraints);
		constraints = new GridBagConstraints(2, 2, 1, 1, 0, 0);
		container.add(txt2, constraints);

		constraints = new GridBagConstraints(3, 0, 1, 1, 0, 0);
		constraints.anchor = GridBagConstraints.LEFT;
		container.add(new FLabel("B:"), constraints);
		constraints = new GridBagConstraints(3, 1, 1, 1, 1, 0);
		constraints.fillHorizontal = true;
		constraints.anchor = GridBagConstraints.LEFT;
		constraints.margin = new Padding(margin, 0);
		container.add(sliderBlue, constraints);
		constraints = new GridBagConstraints(3, 2, 1, 1, 0, 0);
		container.add(txt3, constraints);

		constraints = new GridBagConstraints(4, 0, 1, 1, 0, 0);
		constraints.anchor = GridBagConstraints.LEFT;
		container.add(new FLabel("A:"), constraints);
		constraints = new GridBagConstraints(4, 1, 1, 1, 1, 0);
		constraints.fillHorizontal = true;
		constraints.anchor = GridBagConstraints.LEFT;
		constraints.margin = new Padding(margin, 0);
		container.add(sliderAlpha, constraints);
		constraints = new GridBagConstraints(4, 2, 1, 1, 0, 0);
		container.add(txt4, constraints);

		return showDialog("Color Picker", container, new WindowAdapter() {
			@Override
			public void windowOpenned(WindowEvent event) {
//				input.requestFocus();
			}
		}) == 1 ? null : sampleCanvas.getBackground();

	}

}
