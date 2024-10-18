package com.ngeneration.furthergui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.furthergui.math.Rectangle;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class FTable extends FComponent {
	private DefaultTableModel model;
	private TableColumnModel columnmModel;
	private TableCellRenderer renderer = new DefaultTableCellRenderer();
	private int rowHeight = 0;
	private List<Integer> rowsHeight = new LinkedList<>();
	private HeaderComponent headerComponent;
	private FComponent editableComponent;
	private CellEditor cellEditor;
	private ViewToModel editableData;

	public FTable(String[] header) {
		new FTable(new DefaultTableModel(header, 0));
	}

	public FTable(DefaultTableModel defaultTableModel) {
		this.model = defaultTableModel;
		columnmModel = new DefaultTableColumnModel(this);
		headerComponent = new HeaderComponent(this);
		cellEditor = new DefaultCellEditor(this);
	}

	public void setCellRenderer(TableCellRenderer renderer) {
		this.renderer = renderer;
	}

	public TableCellRenderer getCellRenderer() {
		return renderer;
	}

	public Object getValueAt(int row, int col) {
		return model.getValueAt(row, col);
	}

	public boolean isFixedHeight() {
		return rowHeight > 0;
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		super.processMouseEvent(event);
		if (!event.isConsumed()) {
			if (event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton() == MouseEvent.BUTTON1) {
				var model = viewToModel(event.getLocation());
				if (model != null) {
					if (event.getClickCount() > 1) {
						setToEditMode(model.getRow(), model.getCol());
					}
				}
				if (event.getClickCount() == 1) {
					if (editableData != null && editableComponent != null && (model == null
							|| model.getRow() != editableData.getRow() || model.getCol() != editableData.getCol())) {
						clearEditable();
					}
				}
			}
		}
	}

	public void clearEditable() {
		if (editableComponent != null) {
			getCellEditor().shouldStopEditing(editableComponent, editableData.row, editableData.col);
			setValue(editableData.row, editableData.col,
					getCellEditor().getValue(editableComponent, editableData.row, editableData.col));
			remove(editableComponent);
			editableComponent = null;
			editableData = null;
			revalidate();
		}
	}

	public void setCellEditor(CellEditor cellEditor) {
		this.cellEditor = cellEditor;
	}

	public CellEditor getCellEditor() {
		return cellEditor;
	}

	public void setToEditMode(int row, int col) {
		if (editableComponent != null && editableData != null && editableData.row == row && editableData.col == col)
			return;
		clearEditable();
		if (getModel().isEditable(row, col)) {
			var model = modelToView(row, col);
			editableComponent = getCellEditor().getEditorComponent(row, col, model.getValue(), false);
			if (editableComponent != null) {
				add(editableComponent);
				editableComponent.requestFocus();
				editableComponent.setBounds(model.getBounds());
				editableData = model;
			}
		}
		if (!isValidated())
			revalidate();
	}

	public static interface CellEditor {

		FComponent getEditorComponent(int row, int col, Object value, boolean selected);

		Object getValue(FComponent cComponent, int row, int col);

		boolean shouldStopEditing(FComponent component, int row, int col);

	}

	public static class DefaultCellEditor implements CellEditor {

		private FTable table;

		public DefaultCellEditor(FTable table) {
			this.table = table;
		}

		protected FTable getTable() {
			return table;
		}

		@Override
		public FComponent getEditorComponent(int row, int col, Object value, boolean selected) {
			FTextField field = new FTextField(String.valueOf(value));
			field.setSelection(0, field.getLength());
			return field;
		}

		@Override
		public Object getValue(FComponent component, int row, int col) {
			return ((FTextField) component).getText();
		}

		@Override
		public boolean shouldStopEditing(FComponent editableComponent, int row, int col) {
			return true;
		}

	}

	@AllArgsConstructor
	@Getter
	public static class ViewToModel {
		private int row;
		private int col;
		private Rectangle bounds;
		private Object value;
	}

	public ViewToModel modelToView(int row, int col) {
		Rectangle bounds = new Rectangle();
		if (isFixedHeight()) {
			bounds.y = row * getRowHeight();
			bounds.height = getRowHeight();
		} else {
			for (int r = 0; r < row + 1; r++) {
				if (r == row) {
					bounds.height = rowsHeight.get(r);
					break;
				}
				bounds.y += rowsHeight.get(r);
			}
		}
		for (int i = 0; i < col + 1; i++) {
			int w = getColumnmModel().getColumnWidth(i);
			if (i == col) {
				bounds.width = w;
				break;
			}
			bounds.x += w;
		}
		var p = getPadding();
		bounds.x += p.left;
		bounds.y += p.top;
		return new ViewToModel(row, col, bounds, getModel().getValueAt(row, col));
	}

	public ViewToModel viewToModel(Point location) {
		Padding p = getPadding();
		int x = location.getX() - p.left;
		int y = location.getY() - p.top;
		int row = -1;
		int col = -1;
		Rectangle bounds = new Rectangle();
		if (y >= 0) {
			if (isFixedHeight()) {
				row = y / getRowHeight();
				bounds.y = row * getRowHeight();
				bounds.height = getRowCount();
			} else {
				int yy = 0;
				for (int r : rowsHeight) {
					++row;
					if (y >= yy && y < yy + r)
						break;
					yy += r;
				}
			}
		}
		for (int i = 0, xx = 0; i < getColumsCount(); i++) {
			int w = getColumnmModel().getColumnWidth(i);
			if (x >= xx && x < xx + w) {
				col = i;
				bounds.x = xx;
				bounds.width = w;
				break;
			}
			xx += w;
		}
		if (row < 0 || row >= getRowCount() || col < 0 || col >= getColumsCount())
			return null;
		return new ViewToModel(row, col, bounds, getModel().getValueAt(row, col));
	}

	@Override
	protected void paintComponent(Graphics g) {
		int row = 0;
		boolean hasFocus = hasFocus();
		var padding = getPadding();
		int y = g.getTranslationY() + padding.top;
		int x = g.getTranslationX() + padding.left;
		Point originalTransalation = g.getTranslation();

		int twidth = getWidth() - padding.getHorizontal();
		int rowCount = model.getRowCount();
		int ttHeight = 0;
		boolean fixedHeight = isFixedHeight();

		var heightIterator = rowsHeight.iterator();
		for (var rowData : model.getData()) {
			int rowHeight = isFixedHeight() ? getRowHeight() : heightIterator.next();
			int xx = 0;
			for (int col = 0; col < model.getColumnsCount(); col++) {
				Object value = model.getValueAt(row, col);
				int width = columnmModel.getColumnWidth(col);
				if (editableComponent == null || editableData == null || editableData.row != row
						|| editableData.col != col) {
					var rendererComponent = renderer.getRendererComponent(this, value, false, hasFocus, row, col);
					rendererComponent.setBounds(0, 0, width, rowHeight);
					g.setTranslation(x + xx, y);
					rendererComponent.validate();
					rendererComponent.paint(g);
				}
				xx += width;
			}
			y += rowHeight;
			ttHeight += rowHeight;
			row++;
		}

		Color gridColor = Color.GRAY;
		g.setTranslation(originalTransalation);
		g.setColor(gridColor);
		g.setPenSize(1);

		int yy = padding.top;
		for (int i = 1; i < rowCount; i++) {
			int rowHeight = isFixedHeight() ? getRowHeight() : heightIterator.next();
			yy += rowHeight;
			g.drawLine(padding.left, yy, padding.left + twidth, yy);
		}
		// vertical
		int xx = 0;
		for (int i = 1; i < model.getColumnsCount(); i++) {
			int cwidth = columnmModel.getColumnWidth(i);
			xx += cwidth;
			g.drawLine(padding.left + xx, padding.top, padding.left + xx, padding.top + ttHeight);
		}

		g.drawRect(padding.left + 1, padding.top + 1, twidth - 1, ttHeight);
	}

	public int getRowHeight() {
		return this.rowHeight;
	}

	@Override
	public Dimension getPrefferedSize() {
		// header model get column sizes
		// row height?
		var padding = getPadding();
		int prefferedWidth = 0;
		int prefferedHeight = isFixedHeight() ? getRowHeight() * getRowCount() : 0;
		for (int i = 0; i < model.getColumnsCount(); i++)
			prefferedWidth += this.columnmModel.getColumnPrefferedWidth(i);
		if (!isFixedHeight()) {
			rowsHeight.clear();
			int row = 0;
			for (var rowData : model.getData()) {
				int maxHeight = 0;
				for (int col = 0; col < model.getColumnsCount(); col++) {
					Object value = model.getValueAt(row, col);
					var rendererComponent = renderer.getRendererComponent(this, value, false, hasFocus(), row, col);
					maxHeight = Math.max(maxHeight, rendererComponent.getPrefferedSize().height);
				}
				rowsHeight.add(maxHeight);
				prefferedHeight += maxHeight;
				row++;
			}
		}
		return padding.toDimension().add(prefferedWidth, prefferedHeight);
	}

	public TableColumnModel getColumnmModel() {
		return columnmModel;
	}

	public int getColumsCount() {
		return model.getColumnsCount();
	}

	public int getRowCount() {
		return model.getRowCount();
	}

	public void setRowHeight(int height) {
		if (height < 1)
			throw new RuntimeException("Invalid height: " + height + " min: 0");
		if (height != this.getRowHeight()) {
			rowHeight = height;
			if (isFixedHeight())
				rowsHeight.clear();
		}
	}

	public DefaultTableModel getModel() {
		return model;
	}

	public HeaderComponent getHeaderComponent() {
		return headerComponent;
	}

	public static interface TableColumnModel {

		int getColumnWidth(int index);

		int getColumnPrefferedWidth(int index);

		void setColumnWidth(int index, int width);

	}

	public static class HeaderComponent extends FPanel {

		private FTable table;
		private List<FComponent> headerComponents = new LinkedList<>();

		public HeaderComponent(FTable table) {
			this.table = table;
			settup();
			setBackground(Color.DARK_GRAY.lighter());
		}

		private void settup() {
			for (int i = 0; i < table.getModel().getColumnsCount(); i++)
				setHeaderComponent(i, new Header(String.valueOf(table.getModel().getColumnName(i))));
		}

		public void setHeaderComponent(int index, FComponent header) {
			if (header == null)
				throw new RuntimeException("header can not be null");
			remove(header);
			add(header);
			headerComponents.remove(header);
			headerComponents.add(index, header);
		}

		@Override
		public Dimension getPrefferedSize() {
			var dim = new Dimension();
			int ttwidth = 0;
			int index = 0;
			for (var c : headerComponents) {
				dim.height = Math.max(dim.height, c.getPrefferedSize().height);
				ttwidth += table.getColumnmModel().getColumnPrefferedWidth(index++);
			}
			dim.width += ttwidth;
			return dim.add(getPadding().getHorizontal(), 0);
		}

		@Override
		protected void doLayout() {
			var padding = getPadding();
			int index = 0;
			int x = padding.left;
			int height = getHeight() - padding.getVertical();
			for (var h : headerComponents) {
				int width = table.getColumnmModel().getColumnWidth(index++);
				h.setBounds(x, padding.top, width, height);
				x += width;
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Color gridColor = Color.LIGTH_GRAY.darker().darker().darker();
			g.setColor(gridColor);
			g.setPenSize(1);
			int x = getPadding().left;
			g.drawRect(x + 1, 1, getWidth() - 1, getHeight() - 1);
			for (int i = 1; i < table.getColumsCount(); i++) {
				x += table.getColumnmModel().getColumnWidth(i);
				g.drawLine(x, 0, x, getHeight());
			}
		}

		private class Header extends FLabel {

			public Header(String text) {
				super(text);
				setPadding(new Padding(5, 5));
				setForeground(Color.LIGTH_GRAY);
			}

		}

	}

	public static class DefaultTableColumnModel implements TableColumnModel {

		private FTable table;
		private int[] widths;

		public DefaultTableColumnModel(FTable owner) {
			table = owner;
			widths = new int[owner.getColumsCount()];
			setupWidths();
			owner.addPropertyListener((event) -> {
				if (event.getProperty().equals(FComponent.DIMENSION_PROPERTY)) {
					setupWidths();
				}
			});
		}

		private void setupWidths() {
			int width = table.getWidth() - table.getPadding().getHorizontal();
			if (width < 0) {
				Arrays.setAll(widths, x -> 0);
			} else {
				int twidth = IntStream.of(widths).sum();
				if (twidth <= 0) {
					int w = width / widths.length;
					Arrays.setAll(widths, x -> w);
				} else {
					for (int i = 0; i < widths.length; i++) {
						float p = (float) widths[i] / twidth;
						widths[i] = (int) (p * width);
					}
				}
			}
		}

		@Override
		public int getColumnWidth(int index) {
			return widths[index];
		}

		@Override
		public void setColumnWidth(int index, int width) {
			widths[index] = width;
		}

		@Override
		public int getColumnPrefferedWidth(int index) {
			return 10;
		}

	}

	public static interface TableCellRenderer {
		FComponent getRendererComponent(FTable component, Object value, boolean isSelected, boolean cellHasFocus,
				int row, int col);
	}

	public static class DefaultTableCellRenderer extends DefaultItemCellRenderer<Object> implements TableCellRenderer {

		@Override
		public FComponent getRendererComponent(FTable table, Object value, boolean isSelected, boolean cellHasFocus,
				int row, int col) {
			return super.getRendererComponent(table, value, isSelected, cellHasFocus);
		}

	}

	public void setValue(int row, int col, Object value) {
		getModel().setValue(row, col, value);
	}

}
