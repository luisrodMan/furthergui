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
import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.event.KeyStroke;
import com.ngeneration.furthergui.text.FTextComponent.TextLine;

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
		actions.add(new Command("copy", "CTRL C", e -> copySelectedText(e)));
		actions.add(new Command("paste", "CTRL V", DefaultCommands::pasteText));
		actions.add(new Command("selectAll", "CTRL A", e -> selectAll((FTextComponent) e.getSource(), e)));
		actions.add(new Command("moveLeft", "LEFT", e -> moveLeftCommand((FTextComponent) e.getSource(), e)));
		actions.add(new Command("moveRight", "RIGHT", DefaultCommands::moveRightCommand));
		actions.add(new Command("moveToNextWord", "CTRL RIGHT", DefaultCommands::moveToNextWord));
		actions.add(new Command("moveToWordStart", "CTRL LEFT", DefaultCommands::moveToWordStart));
		actions.add(new Command("moveToLineStart", "HOME", DefaultCommands::moveToLineStart));
		actions.add(new Command("moveToLineEnd", "END", DefaultCommands::moveToLineEnd));
		actions.add(new Command("selectToLineStart", "SHIFT HOME", DefaultCommands::selectToLineStart));
		actions.add(new Command("selectToLineEnd", "SHIFT END", DefaultCommands::selectToLineEnd));
		actions.add(new Command("moveToTextStart", "CTRL HOME", DefaultCommands::moveToTextStart));
		actions.add(new Command("moveToTextEnd", "CTRL END", DefaultCommands::moveToTextEnd));
		actions.add(new Command("selectLeft", "SHIFT LEFT", DefaultCommands::selectLeft));
		actions.add(new Command("selectRight", "SHIFT RIGHT", DefaultCommands::selectRight));
		actions.add(new Command("selectToTextStart", "CTRL SHIFT HOME", DefaultCommands::selectToTextStart));
		actions.add(new Command("selectToTextEnd", "CTRL SHIFT END", DefaultCommands::selectToTextEnd));
		actions.add(new Command("selectWordStart", "CTRL SHIFT LEFT", DefaultCommands::selectWordStart));
		actions.add(new Command("selectToWordEnd", "CTRL SHIFT RIGHT", DefaultCommands::selectToWordEnd));
	}

	private static int getLineLimit(FTextComponent component, int dir) {
		var caret = component.getCaretPosition();
		var line = component.getTextLineAtOffset(caret);
		if (dir == 0) {
			int i = TextUtils.getNotSpaceIndex(line.getText(), 0);
			if (i == -1)
				return line.getOffset() + (caret == line.getOffset() ? line.getText().length() : 0);
			else
				return line.getOffset() + (caret == line.getOffset() + i ? 0 : i);
		} else
			return line.getOffset() + line.getText().length();
	}

	public static void moveToLineStart(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		component.setCaretPosition(getLineLimit(component, 0));
	}

	public static void moveToLineEnd(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		component.setCaretPosition(getLineLimit(component, 1));
	}

	public static void selectToLineStart(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		int caret = component.getSelectionStart();
		component.getActionMap().get("moveToLineStart").actionPerformed(e);
		component.setSelection(caret, component.getCaretPosition());
	}

	public static void selectToLineEnd(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		int caret = component.getSelectionStart();
		component.getActionMap().get("moveToLineEnd").actionPerformed(e);
		component.setSelection(caret, component.getCaretPosition());
	}

	public static void moveToTextStart(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		component.setCaretPosition(0);
	}

	public static void moveToTextEnd(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		component.setCaretPosition(component.getLength());
	}

	public static void moveRightCommand(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		component.setCaretPosition(component.hasSelection() && !e.isShiftDown() ? component.getSelectionMax()
				: getNextCaretPosition(component), true, e.isShiftDown());
	}

	public static final void selectRight(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		component.setSelection(component.getSelectionMin(), getNextCaretPosition(component));
	}

	public static final void selectLeft(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		component.setSelection(component.getSelectionMax(), getPreviewsCaretPosition(component));
	}

	public static final void selectToTextStart(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		component.setSelection(component.getSelectionStart(), 0);
	}

	public static final void selectToTextEnd(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		component.setSelection(component.getSelectionStart(), component.getLength());
	}

	public static final void moveToWordStart(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		var lastLocation = component.getCaretPosition();
		var line = component.getTextLineAtOffset(component.getCaretPosition());
		var start = DefaultCommands.getWordStart(component.getCaretPosition() - line.getOffset(), line);
		component.setCaretPosition(line.getOffset() + start);

		if (component.getCaretPosition() == lastLocation && component.getCaretPosition() > 0) {
			var line2 = component.getTextLineAtOffset(component.getCaretPosition() - 1);
			var start2 = DefaultCommands.getWordStart(component.getCaretPosition() - line2.getOffset() - 1, line2);
			component.setCaretPosition(line2.getOffset() + start2);
		}
	}

	public static final void selectWordStart(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		var c = component.getSelectionStart();
		component.getActionMap().get("moveToWordStart").actionPerformed(e);
		component.setSelection(c, component.getCaretPosition());
	}

	public static final void moveToNextWord(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		int nc = component.getCaretPosition();
		var line = component.getTextLineAtOffset(nc);
		var end = DefaultCommands.getWordEnd(nc - line.getOffset(), line);
		component.setCaretPosition(line.getOffset() + end);
	}

	public static final void selectToWordEnd(ActionEvent e) {
		var component = (FTextComponent) e.getSource();
		int nc = component.getCaretPosition();
		var line = component.getTextLineAtOffset(nc);
		var end = DefaultCommands.getWordEnd(nc - line.getOffset(), line);
		component.setSelection(component.getSelectionStart(), line.getOffset() + end);
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
				text.insertI(clipboard.getData(DataFlavor.stringFlavor).toString());
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

	public static int getNextCaretPosition(FTextComponent component) {
		// this method jump line breaks
		int position = component.getCaretPosition();
		if (position < component.getLength()) {
			var line = component.getTextLineAtOffset(position);
			position += 1;
			if (position > line.getOffset() + line.getText().length())
				position = line.getOffset() + line.getText().length() + line.getSeparator().length();
		}
		return position;
	}

	private static int getPreviewsCaretPosition(FTextComponent component) {
		// this method jump line breaks
		int position = component.getCaretPosition();
		if (position > 0) {
			var line = component.getTextLineAtOffset(position);
			position -= 1;
			if (position < line.getOffset()) {
				line = component.getTextLineAtOffset(position);
				position = line.getOffset() + line.getText().length();
			}
		}
		return position;
	}

	public static int getWordStart(int offset, TextLine line) {
		if (offset == 0)
			return 0;

		var text = line.getText();
		if (offset >= text.length() && offset <= text.length() + line.getSeparator().length())
			return text.length();

		var fcharacter = text.charAt(offset);
		int i = offset;
		if (Character.isAlphabetic(fcharacter)) {
			while (--i > -1 && Character.isAlphabetic(text.charAt(i)))
				offset = i;
		} else if (Character.isWhitespace(fcharacter))
			while (--i > -1 && Character.isWhitespace(text.charAt(i)))
				offset = i;
		else
			while (--i > -1 && !Character.isAlphabetic(text.charAt(i)) && !Character.isWhitespace(text.charAt(i)))
				offset = i;
		return offset;
	}

	public static int getWordEnd(int offset, TextLine line) {
		var text = line.getText();
		if (offset == text.length())
			return offset + line.getSeparator().length();
		var fcharacter = text.charAt(offset);
		int i = offset;
		if (Character.isAlphabetic(fcharacter)) {
			while (++i < text.length() && Character.isAlphabetic(text.charAt(i)))
				offset = i;
		} else if (Character.isWhitespace(fcharacter)) {
			while (++i < text.length() && Character.isWhitespace(text.charAt(i)))
				offset = i;
		} else
			while (++i < text.length() && !Character.isAlphabetic(text.charAt(i))
					&& !Character.isWhitespace(text.charAt(i)))
				offset = i;
		return offset + 1;
	}

}
