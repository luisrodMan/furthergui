package com.ngeneration.furthergui.text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.ngeneration.furthergui.Cursor;
import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.event.DocumentListener;
import com.ngeneration.furthergui.event.FocusEvent;
import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.MathUtil;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.furthergui.text.DocumentFilter.Bypass;

import lombok.AllArgsConstructor;
import lombok.Data;

public class FTextComponent extends FPanel {

	public static final String TEXT_PROPERTY = "p_text";

	private String lineSeparator = System.lineSeparator();
	private List<TextLine> lines = new ArrayList<>();
	private ViewToModel caret = new ViewToModel(0, 0, 0, 0);
	private int selectionStart;
	private int selectionEnd;

	private List<DocumentListener> documentListeners = new LinkedList<>();
	private Color caretColor = new Color(255, 255, 255);
	private Color selectionColor = new Color(20, 175, 255);

	private int documentLength = 0;
	private int caretTargetLocalLocation;
	private boolean fixedFont;
	private Color focusedColor = Color.DARK_GRAY.darker().darker().darker();
	private boolean centerVertical = false;
	private TextContainerComponent textContainerComponent = new TextContainerComponent();

	private DocumentFilter documentFilter;

	public FTextComponent() {
		this("");
	}

	public FTextComponent(String text) {
		super(new BorderLayout());
		add(textContainerComponent);
		setFocusable(true);
		setBackground(new Color(30, 30, 30));
		setCursor(Cursor.getStandardCursor(Cursor.TEXT_CURSOR));
		if (text == null || "".equals(text)) {
			lines.add(new TextLine("", 0, "", 0));
		} else
			setText(text);
		DefaultCommands.installCommands(this);
	}

	@Override
	public void setCursor(Cursor cursor) {
		super.setCursor(cursor);
		textContainerComponent.setCursor(cursor);
	}

	protected void setCenteredVertical(boolean centerVertical) {
		this.centerVertical = centerVertical;
	}

	@Override
	protected void processFocusEvent(FocusEvent focusEvent) {
		super.processFocusEvent(focusEvent);
		if (!focusEvent.isConsumed())
			repaint();
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		super.processMouseEvent(event);
		if (event.isConsumed())
			return;
		if (event.getEventType() == MouseEvent.MOUSE_PRESSED || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
			var data = viewToModel2(event.getLocation());
			setSelection(event.getEventType() == MouseEvent.MOUSE_DRAGGED || event.isShiftDown() ? getSelectionStart()
					: data.getOffset(), data.getOffset());
		} else if (event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton() == MouseEvent.BUTTON1
				&& event.getClickCount() == 2) {
			var model = viewToModel2(event.getLocation());
			if (model != null) {
				var line = getTextLineAtIndex(model.getLine());
				var start = model.getLocalOffset() == 0 ? 0
						: DefaultCommands.getWordStart(model.getLocalOffset() - 1, line);
				var end = DefaultCommands.getWordEnd(model.getLocalOffset(), line);
				setSelection(line.offset + start, line.offset + end);
			}
		}
		event.consume();
	}

	@Override
	protected void processKeyEvent(KeyEvent keyEvent) {
		super.processKeyEvent(keyEvent);
		if (!keyEvent.isConsumed()) {
			if (keyEvent.getAction() == KeyEvent.KEY_TYPED) {
				insertI("" + keyEvent.getChar());
			} else if (keyEvent.getAction() == KeyEvent.KEY_PRESSED || keyEvent.getAction() == KeyEvent.KEY_REPEATED) {
				if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
					setCaretPosition(getPrevNextLineCaretLocation(0), false, keyEvent.isShiftDown());
				} else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
					setCaretPosition(getPrevNextLineCaretLocation(1), false, keyEvent.isShiftDown());
				} else if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
					insertI(lineSeparator);
				} else if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					removeString();
				}
//				System.out.println("pressed: " + keyEvent.getKeyCode());
			}
//			if (!keyEvent.isControlDown())
//				keyEvent.consume();
		}
	}

	void insertI(String string) {
		if (hasSelection())
			replaceString(getSelectionMin(), getSelectionLength(), "" + string);
		else
			insertString(getCaretPosition(), "" + string, null);
	}

	private int getPrevNextLineCaretLocation(int direction) {
		var lineidx = getTextLineIndexAtOffset(getCaretPosition());
		if (lineidx == 0 && direction == 0)
			return 0;
		else if (lineidx == getLineCount() - 1 && direction == 1)
			return getLength();
		else {
			var otherline = getTextLineAtIndex(lineidx + (direction == 0 ? -1 : 1));
			return otherline.getOffset() + Math.min(caretTargetLocalLocation, otherline.getText().length());
		}
	}

	public TextLine getTextLineAtOffset(int position) {
		validateDocumentPosition(position);
		return lines.get(getTextLineIndexAtOffset(position));
	}

	public int getTextLineIndexAtOffset(int position) {
		validateDocumentPosition(position);
		for (int i = 1; i < lines.size(); i++) {
			var line = lines.get(i);
			if (line.getOffset() > position)
				return i - 1;
		}
		return lines.size() - 1;
	}

	private TextLine getTextLineAtIndex(int idx) {
		return lines.get(idx);
	}

	private void removeString() {
		var caret = getCaretPosition();
		if (hasSelection()) {
			remove(getSelectionMin(), getSelectionLength());
		} else if (caret > 0) {
			var lineidx = getTextLineIndexAtOffset(caret);
			var textline = getTextLineAtIndex(lineidx);
			if (textline.getOffset() < caret)// remove character
				remove(caret - 1, 1);
			else {
				textline = getTextLineAtIndex(lineidx - 1);// remove line break
				remove(caret - textline.getSeparator().length(), textline.getSeparator().length());
			}
		}
	}

	private ViewToModel viewToModel2(Point point) {
		var padding = getPadding();
		int y = point.getY() - padding.top - textContainerComponent.offset.getY();
		int lineIndex = MathUtil.clamp(y / getLineHeight(), 0, getLineCount() - 1);
		var line = lines.get(lineIndex);

		int screenX = point.getX() - padding.left - textContainerComponent.offset.getX();
		var font = getFont();
		int location = 0;
		int x = 0;
		for (int i = 0; i < line.getText().length(); i++) {
			int cw = getStringWidth(line.getText(), i, 1);
			if (screenX < x + cw / 2)
				break;
			location++;
			x += cw;
		}
		int globalLocation = location;
		for (int i = 0; i < lineIndex; i++) {
			var iter = lines.get(i);
			globalLocation += iter.getText().length() + iter.getSeparator().length();
		}
		return new ViewToModel(lineIndex, globalLocation, location, x);
	}

	@Data
	@AllArgsConstructor
	private class ViewToModel {
		private int line;
		private int offset;
		private int localOffset;
		private int screenOffset;
	}

	private List<TextLine> getTextLines(String text) {
		List<TextLine> lines = new LinkedList<>();
		int f = 0;
		int t = 0;
//		int offset = 0;
		do {
			boolean carriage = false;
			t = text.indexOf("\n", f);
			if (t > 0 && (carriage = text.charAt(t - 1) == '\r')) {
				t--;
			}
			// if \r at the end????
			t = t == -1 ? text.length() : t;
			String line = text.substring(f, t);
			String separator = t == text.length() ? null : (carriage ? "\r\n" : "\n");
			var textLine = new TextLine(line, f, separator, getStringWidth(line, 0, line.length()));
			f = t + (separator == null ? 0 : separator.length());// \r?\n
//			offset += line.length() + lineSeparator.length();
			lines.add(textLine);
		} while (f < text.length());
		if (text.endsWith("\n")) {
			var textLine = new TextLine("", text.length(), "", 0);
			lines.add(textLine);
		}
		return lines;
	}

	public int getLineHeight() {
		return getFont().getFontHeight();
	}

	public String getText() {
		if (lines.isEmpty())
			return null;
		StringBuilder builder = new StringBuilder();
		lines.forEach(l -> builder.append(l.getText()).append(l.separator != null ? l.separator : ""));
		return builder.toString();
	}

	public void setText(String text) {
		caret.localOffset = caret.line = caret.screenOffset = selectionEnd = selectionStart = caret.offset = 0;
		if (text == null)
			text = "";
		if (text.equals(getDocumentText()))
			return;
		lines.clear();
		clearSelection();
		lines = getTextLines(text);
		documentLength = text.length();
		setCaretPosition(getLength());
		revalidate();
		firePropertyListeners(TEXT_PROPERTY, null);
	}

	public void clearSelection() {
		selectionStart = selectionEnd = getCaretPosition();
		repaint();
	}

	public int getLineCount() {
		return lines.size();
	}

	public String getDocumentText() {
		return getText();
	}

	public String getDocumentText(int from, int to) {
		return getDocumentText().substring(from, to);
	}

	private void shiftNextLines(int fromLine, int offset) {
		for (; fromLine < getLineCount(); fromLine++)
			getTextLineAtIndex(fromLine).offset += offset;
	}

	public void remove(int from, int length) {
		if (documentFilter != null) {
			documentFilter.remove(new Bypass(this), from, length);
		} else
			remove(from, length, true);
	}

	void remove(int offset, int length, boolean fireEvent) {
		var curlineIndex = getTextLineIndexAtOffset(offset);
		var secondlineIndex = getTextLineIndexAtOffset(offset + length);
		var curline = getTextLineAtIndex(curlineIndex);
		var secondline = getTextLineAtIndex(secondlineIndex);
		String ft = curline.getText() + curline.getSeparator();
		String st = secondline.getText() + secondline.getSeparator();
		var text1 = ft.substring(0, offset - curline.getOffset());
		var text2 = st.substring(offset + length - secondline.getOffset());
		String ftext = text1 + text2;
		if (ftext.endsWith("\r\n")) {
			curline.setText(ftext.substring(0, ftext.length() - 2));
			curline.setSeparator(ftext.substring(ftext.length() - 2));
		} else if (ftext.endsWith("\n")) {
			curline.setText(ftext.substring(0, ftext.length() - 1));
			curline.setSeparator(ftext.substring(ftext.length() - 1));
		} else {
			curline.setText(ftext);
			curline.setSeparator("");
		}
		curline.setWidth(getStringWidth(curline.text, 0, curline.text.length()));
		// remove lines in between
		for (int i = 0; i < secondlineIndex - curlineIndex; i++) {
			lines.remove(curlineIndex + 1);
		}
		shiftNextLines(curlineIndex + 1, -length);
		documentLength -= length;
		setCaretPosition(offset);
		revalidate();
		if (fireEvent) {
			firePropertyListeners(TEXT_PROPERTY, null);
			// fire remove event
		}
	}

	public void replaceString(int from, int length, String replace) {
		if (documentFilter != null) {
			documentFilter.replace(new Bypass(this), from, length, replace);
		} else
			replaceStringInternal(from, length, replace);
	}

	void replaceStringInternal(int from, int length, String replace) {
		remove(from, length, false);
		insertString(from, replace, null, false);
		firePropertyListeners(TEXT_PROPERTY, null);
	}

	public void insertString(int offset, String string, Object object) {
		if (documentFilter != null) {
			documentFilter.insertString(new Bypass(this), offset, string);
		} else
			insertString(offset, string, object, true);
	}

	public void insertString(int offset, String string, Object object, boolean fireEvent) {
//		System.out.println("inserting at " + offset + "<" + replace + ">");
		var newlines = getTextLines(string);
//		System.out.println("new lines count: " + newlines.size());
		var curlineIndex = getTextLineIndexAtOffset(offset);
		var curline = lines.get(curlineIndex);
		String text1 = curline.getText().substring(0, offset - curline.getOffset());
		String text2 = curline.getText().substring(offset - curline.getOffset());
		if (newlines.size() == 1) {
			curline.setText(text1 + string + text2);
			curline.width += newlines.get(0).width;
		} else {
//			System.out.println("text1: " + text1);
//			System.out.println("text2: " + text2);
//			for (var l : newlines)
//				System.out.println("n:" + l.getText() + "~");
			curline.setText(text1 + newlines.get(0).getText());
			int previousWidth1 = getStringWidth(text1, 0, text1.length());
			int previousWidth2 = curline.getWidth() - previousWidth1;
			curline.width = previousWidth1 + newlines.get(0).width;
			var lastLine = newlines.get(newlines.size() - 1);
			lastLine.setText(lastLine.getText() + text2);
			lastLine.width += previousWidth2;
			// last line uses curline separator before it is changed
			lastLine.setSeparator(curline.getSeparator());
			curline.setSeparator(newlines.get(0).getSeparator());
			for (int i = 1; i < newlines.size(); i++) {
				newlines.get(i).offset += curline.getOffset() + text1.length();
				lines.add(curlineIndex + i, newlines.get(i));
			}
		}

		// offset next lines
		shiftNextLines(curlineIndex + newlines.size(), string.length());

//		System.out.println("doc1: " + documentLength);
		documentLength += string.length();
//		System.out.println("doc2: " + documentLength);
		revalidate();
		setCaretPosition(offset + string.length());
		if (fireEvent) {
			firePropertyListeners(TEXT_PROPERTY, null);
			// insert event!!
		}
	}

	public void setFixedFont(boolean value) {
		fixedFont = value;
	}

	public boolean isFixedFont() {
		return fixedFont;
	}

	private int getStringWidth(String text, int i, int j) {
		return isFixedFont() ? getFont().getStringWidth("a") * text.length() : getFont().getStringWidth(text, i, j);
	}

	public void setCaretPosition(int position) {
		setCaretPosition(position, true, false);
	}

	// updateLocalTarget?? delete
	public void setCaretPosition(int position, boolean updateLocalTarget, boolean keepSelection) {
		validateDocumentPosition(position);
		boolean hasSelection = hasSelection();
		selectionEnd = position;
		boolean repaint = caret.getOffset() != position;

		if (repaint) {
			// update caret
			caret.offset = position;
			caret.line = getTextLineIndexAtOffset(position);
			caret.localOffset = position - getTextLineAtIndex(caret.line).getOffset();
			caret.screenOffset = getScreenOffset(getTextLineAtOffset(position), position);
		}

		textContainerComponent.updateOffset();

		if (updateLocalTarget)
			caretTargetLocalLocation = caret.localOffset;
		if (!keepSelection) {
			selectionStart = selectionEnd;
			if (hasSelection)
				repaint = true;
		}
		if (repaint)
			repaint();
	}

	private int getScreenOffset(TextLine line, int position) {
		int screenX = 0;
		var font = getFont();
		for (int i = 0; i < line.getText().length(); i++) {
			if (line.getOffset() + i == position)
				break;
			screenX += getStringWidth(line.getText(), i, 1);
		}
		return screenX;
	}

	private void validateDocumentPosition(int position) {
		if (position < 0 || position > getLength())
			throw new IndexOutOfBoundsException("Index: " + position + " length: " + getLength());
	}

	public int getCaretPosition() {
		return caret.getOffset();
	}

	public void setSelectionStart(int offset) {
		validateDocumentPosition(offset);
		selectionStart = offset;
		if (hasSelection())
			repaint();
	}

	public void setSelectionEnd(int offset) {
		setCaretPosition(offset, true, true);
	}

	public void selectAll() {
		setSelection(0, getLength());
	}

	public void setSelection(int from, int to) {
		if (from == to)
			setCaretPosition(to);
		else {
			validateDocumentPosition(from);
			validateDocumentPosition(to);
			selectionStart = from;
			caret.offset = from;// change this
			setCaretPosition(to, true, true);
		}
	}

	public int getSelectionStart() {
		return selectionStart;
	}

	public int getSelectionMin() {
		return Math.min(selectionStart, selectionEnd);
	}

	public int getSelectionMax() {
		return Math.max(selectionStart, selectionEnd);
	}

	public int getSelectionEnd() {
		return selectionEnd;
	}

	public boolean hasSelection() {
		return getSelectionStart() != getSelectionEnd();
	}

	private int getSelectionLength() {
		return getSelectionMax() - getSelectionMin();
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public String getSelection() {
		return !hasSelection() ? "" : getSubString(getSelectionMin(), getSelectionLength());
	}

	private String getSubString(int offset, int length) {
		validateDocumentPosition(offset);
		validateDocumentPosition(offset + length);
		int idx = getTextLineIndexAtOffset(offset);
		StringBuilder builder = new StringBuilder();
		for (; idx < getLineCount(); idx++) {
			var line = getTextLineAtIndex(idx);
			if (offset + length > line.getOffset()) {
				int ls = Math.max(offset, line.offset) - line.offset;
				if (offset + length <= line.getOffset() + line.text.length())
					builder.append(
							line.getText().substring(ls, Math.min(line.text.length(), offset + length - line.offset)));
				else if (offset + length < line.getOffset() + line.text.length() + line.getSeparator().length())
					builder.append((line.getText() + line.getSeparator()).substring(ls, Math
							.min(line.text.length() + line.getSeparator().length(), offset + length - line.offset)));
				else
					builder.append(line.getText().substring(ls) + line.getSeparator());
			} else
				break;
		}
		return builder.toString();
	}

	/**
	 * Real document length Accounting line breaks characters
	 * 
	 * @return
	 */
	public int getLength() {
		return documentLength;
	}

	public void addDocumentListener(DocumentListener documentListener) {
		documentListeners.add(documentListener);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(hasFocus() ? getFocusedColor() : getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	public void setFocusedColor(Color focusedColor) {
		this.focusedColor = focusedColor;
	}

	public Color getFocusedColor() {
		return focusedColor;
	}

	@Override
	public void setValidated(boolean value) {
		super.setValidated(value);
		if (!value)
			textContainerComponent.offset.set(0, 0);
	}

	private class TextContainerComponent extends FComponent {

		private Point offset = new Point();
		int caretWidth = 1;

		private void updateOffset() {
			if (!isValidated())
				return;
			boolean moveToVisible = false;
			var visible = getVisibleRectangle();
			if (offset.getX() + caret.screenOffset + caretWidth > getWidth())
				offset.setX(getWidth() - caret.screenOffset - caretWidth);
			else if (offset.getX() + caret.screenOffset < 0)
				offset.setX(offset.getX() + Math.abs(offset.getX() + caret.screenOffset));
			else if (!visible.contains(offset.getX() + caret.screenOffset + caretWidth, visible.y)
					|| !visible.contains(offset.getX() + caret.screenOffset, visible.y)) {
				if (offset.getX() + caret.screenOffset + caretWidth > visible.x + visible.width) {
					int diff = offset.getX() + caret.screenOffset + caretWidth - visible.width - visible.x;
					visible.setLocation(visible.x + visible.width, visible.y);
					visible.setSize(diff, visible.height);
					moveToVisible = true;
				} else if (offset.getX() + caret.screenOffset < visible.x) {
					visible.x -= Math.abs(offset.getX() + caret.screenOffset - visible.x);
					moveToVisible = true;
				}
			}

			int y = getTextLineIndexAtOffset(caret.offset) * getLineHeight();
			if (offset.getY() + y + getLineHeight() > getHeight())
				offset.setY(getHeight() - y - getLineHeight());
			else if (offset.getY() + y < 0)
				offset.setY(offset.getY() + Math.abs(offset.getY() + y));
			else if (!visible.contains(visible.x, offset.getY() + y + getLineHeight())
					|| !visible.contains(visible.x, offset.getY() + y)) {
				if (offset.getY() + y + getLineHeight() > visible.y + visible.height) {
					int diff = offset.getY() + y + getLineHeight() - visible.height - visible.y;
					visible.setLocation(visible.x, visible.y + visible.height);
					visible.setSize(visible.width, diff);
					moveToVisible = true;
				} else if (offset.getY() + y < visible.y) {
					visible.y -= Math.abs(offset.getY() + y - visible.y);
					moveToVisible = true;
				}
			}
			if (moveToVisible)
				makeVisible(visible);
		}

		@Override
		public Dimension getPrefferedSize() {
			var dimension = getPadding().toDimension();
			dimension.height += getFont().getFontHeight() * getLineCount();
			int maxWidth = 0;
			for (TextLine line : lines) {
				maxWidth = Math.max(line.width, maxWidth);
			}
			dimension.width += maxWidth + caretWidth;
			return dimension;
		}

		@Override
		protected void paintComponent(Graphics g) {
			var padding = new Padding(0);

			padding.left = offset.getX();
			padding.top = offset.getY();

			float startY = centerVertical ? ((getHeight() - getFont().getFontHeight()) * getLineCount()) * 0.5f
					: padding.top;

			float y = startY;
			final int LH = getLineHeight();
			boolean hasFocus = FTextComponent.this.hasFocus();

			// selection
			if (hasSelection()) {
				var l1 = getTextLineIndexAtOffset(getSelectionMin());
				var l2 = getTextLineIndexAtOffset(getSelectionMax());
				var line1 = getTextLineAtIndex(l1);
				var line2 = getTextLineAtIndex(l2);
				g.setColor(getSelectionColor());
				int xx = getStringWidth(line1.getText(), 0, getSelectionMin() - line1.getOffset());
				int xx2 = getStringWidth(line2.getText(), 0, getSelectionMax() - line2.getOffset());
				float yyy = centerVertical ? startY : padding.top;
				if (l1 == l2) {
					g.fillRect(padding.left + xx, yyy + l1 * LH, xx2 - xx, LH);
				} else {
					g.fillRect(padding.left + xx, yyy + l1 * LH, -padding.left + FTextComponent.this.getWidth(), LH);
					var hh = l2 - l1 - 1;
					if (hh > 0) {
						g.fillRect(0, yyy + (l1 + 1) * LH, FTextComponent.this.getWidth(), LH * hh);
					}
					g.fillRect(padding.left, yyy + l2 * LH, xx2, LH);
				}
			}

			g.setColor(getForeground());
			g.setFont(getFont());
			for (TextLine line : lines) {
				g.drawString(padding.left, y, line.text);
				y += g.getFont().getFontHeight();
			}

			boolean caretVisible = true;
			if (caretVisible && hasFocus) {
				g.setColor(caretColor);
				g.fillRect(padding.left + caret.screenOffset,
						centerVertical ? startY : (padding.top + caret.line * getLineHeight()), 1, getLineHeight());
			}
		}

	}

	@Data
	public class TextLine {

		private String text;
		private int offset;
		private String separator;
		private int width;

		public TextLine(String text, int offset, String separator, int width) {
			this.text = text;
			this.offset = offset;
			this.width = width;
			this.separator = separator;
			if (this.separator == null)
				this.separator = "";
		}

	}

	public void setDocumentFilter(DocumentFilter documentFilter) {
		this.documentFilter = documentFilter;
	}

	public DocumentFilter getDocumentFilter() {
		return documentFilter;
	}

}
