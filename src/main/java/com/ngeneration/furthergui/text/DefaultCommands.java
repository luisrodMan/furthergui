package com.ngeneration.furthergui.text;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.ActionEvent;
import com.ngeneration.furthergui.event.KeyStroke;

import lombok.Data;

public class DefaultCommands {

	private static List<Command> actions = new LinkedList<>();

	@Data
	private static class Command {
		String name;
		KeyStroke keyStroke;
		Action action;

		Command(String name, String binding, Action action) {
			this.name = name;
			this.keyStroke = KeyStroke.getKeyStroke(binding);
			this.action = action;
		}
	}

	static {
		actions.add(new Command("moveLeft", "LEFT", e -> moveLeftCommand((FTextComponent) e.getSource(), e)));
		actions.add(new Command("selectAll", "CTRL A", e -> selectAll((FTextComponent) e.getSource(), e)));
		actions.add(new Command("copy", "CTRL C", e -> copySelectedText(e)));
		actions.add(new Command("paste", "CTRL V", e -> pasteText(e)));
	}

	public static final void installCommands(FTextComponent component) {
		for (var entry : actions) {
			component.getInputMap(FTextComponent.WHEN_FOCUSED).put(entry.getKeyStroke(), entry.getName());
			component.getActionMap().put(entry.getName(), entry.getAction());
		}
	}

	public static void copySelectedText(ActionEvent e) {
		String myString = ((FTextComponent) e.getSource()).getSelection();
		StringSelection stringSelection = new StringSelection(myString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	public static void pasteText(ActionEvent e) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			FTextComponent text = (FTextComponent) e.getSource();
			try {
				text.insertString(text.getCaretPosition(), clipboard.getData(DataFlavor.stringFlavor).toString(), null);
			} catch (UnsupportedFlavorException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private static void selectAll(FTextComponent source, ActionEvent e) {
		source.setSelection(0, source.getLength());
	}

	public static void moveLeftCommand(FTextComponent component, ActionEvent event) {
		component.setCaretPosition(component.hasSelection() && !event.isShiftDown() ? component.getSelectionMin()
				: getPreviewsCaretPosition(component), true, event.isShiftDown());
	}

	private static int getPreviewsCaretPosition(FTextComponent component) {
		// this method jump line breaks
		int position = component.getCaretPosition();
		if (position > 0) {
			var line = component.getTextLineAtOffset(position);
			System.out.println("first line: " + line.getText() + ":" + line.getOffset());
			position -= 1;
			if (position < line.getOffset()) {
				line = component.getTextLineAtOffset(position);
				System.out.println("second: " + line.getText() + ":" + line.getOffset());
				position = line.getOffset() + line.getText().length();
			}
		}
		return position;
	}

}
