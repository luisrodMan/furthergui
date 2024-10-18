package com.ngeneration.furthergui;

import com.ngeneration.furthergui.event.WindowAdapter;
import com.ngeneration.furthergui.event.WindowEvent;
import com.ngeneration.furthergui.event.WindowListener;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.math.Padding;

public class FOptionPane {

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
		int[] pressedBtn = new int[] { 1 };
		aceptBtn.addActionListener(e -> {
			pressedBtn[0] = 2;
			dialog.dispose();
		});
		cancelBtn.addActionListener(e -> {
			dialog.dispose();
		});
		dialog.pack();
		dialog.addWindowsListener(listener);
		dialog.setVisible(true);
		return pressedBtn[0];
	}

	public static String showInputDialog(String title) {
		return showInputDialog(title, "");
	}

	public static String showInputDialog(String title, String initialText) {
		FTextField input = new FTextField(initialText);
		return showDialog(title, input, new WindowAdapter() {
			@Override
			public void windowOpenned(WindowEvent event) {
				input.requestFocus();
			}
		}) == 1 ? null : input.getText();
	}

	public static int showConfirmDialog(FComponent parent, String title, String msg) {
		return showDialog(title, new FLabel(msg), new WindowAdapter());
	}

}
