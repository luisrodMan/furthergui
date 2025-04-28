package com.ngeneration.furthergui.layout;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.math.Dimension;

import lombok.AllArgsConstructor;

import static com.ngeneration.furthergui.layout.GridBagConstraints.*;

public class GridBagLayout implements Layout {

	@AllArgsConstructor
	private class Data {
		FComponent component;
		GridBagConstraints data;
	}

	private Data[][] grid;

	public GridBagLayout(int rows, int cols) {
		grid = new Data[rows][cols];
	}

	@Override
	public void addComponent(FComponent component, Object constraints) {
		if (constraints instanceof GridBagConstraints c)
			grid[c.row][c.col] = new Data(component, c);
		else
			grid[0][0] = new Data(component, new GridBagConstraints());
	}

	@Override
	public Dimension getPrefferedDimension(FComponent container) {
		int[] widths = new int[grid[0].length];
		int[] heights = new int[grid.length];
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				var data = grid[i][j];
				if (data != null) {
					var dim = data.component.getPrefferedSize();
					if (data.data.cols == 1)
						widths[j] = Math.max(widths[j],
								dim.width + (data.data.margin == null ? 0 : data.data.margin.getHorizontal()));
					if (data.data.rows == 1)
						heights[i] = Math.max(heights[i],
								dim.height + (data.data.margin == null ? 0 : data.data.margin.getVertical()));
				}
			}
		}
		return container.getPadding().toDimension().add(sum(widths), sum(heights));
	}

	private int sum(int[] widths) {
		return sum(widths, 0, widths.length);
	}

	private float sum(float[] widths) {
		var val = 0.0f;
		for (int i = 0; i < widths.length; i++)
			val += widths[i];
		return val;
	}

	private int sum(int[] widths, int from, int count) {
		var val = 0;
		for (int i = from; i < from + count; i++)
			val += widths[i];
		return val;
	}

	@Override
	public void layout(FComponent container) {
		int[] widths = new int[grid[0].length];
		float[] weights = new float[grid[0].length];
		int[] heights = new int[grid.length];
		float[] weightsH = new float[grid.length];

		int[] maxWidths = new int[grid[0].length];
		for (int i = 0; i < grid.length; i++) {
			int maxHeight = 0;
			for (int j = 0; j < grid[i].length; j++) {
				var data = grid[i][j];
				if (data != null) {
					var dim = data.component.getPrefferedSize();
					maxWidths[j] = Math.max(maxWidths[j],
							dim.width + (data.data.margin == null ? 0 : data.data.margin.getHorizontal()));
					if (data.data.cols == 1) {
						weights[j] = Math.max(weights[j], data.data.weightH);
						if (!data.data.fillHorizontal)
							widths[j] = maxWidths[j];
					}
					if (data.data.rows == 1) {
						maxHeight = Math.max(heights[i],
								dim.height + (data.data.margin == null ? 0 : data.data.margin.getVertical()));
						weightsH[i] = Math.max(weightsH[i], data.data.weightV);
						if (!data.data.fillVertical)
							heights[i] = maxHeight;
					}
				}
			}
			//
			if (heights[i] == 0 && weightsH[i] == 0) {
				heights[i] = maxHeight;
			}
		}

		for (int i = 0; i < widths.length; i++)
			if (widths[i] == 0 && weights[i] == 0)
				widths[i] = maxWidths[i];

		int twidth = sum(widths);
		int theighs = sum(heights);
		float tweights = sum(weights);
		float tweightsH = sum(weightsH);
		if (tweights < 1)
			tweights = 1;
		if (tweightsH < 1)
			tweightsH = 1;

		int extraWidth = Math.max(0, container.getWidth() - twidth - container.getPadding().getHorizontal());
		int extraHeight = Math.max(0, container.getHeight() - theighs - container.getPadding().getVertical());
		for (int i = 0; i < grid[0].length; i++)
			widths[i] += (int) (weights[i] / tweights * extraWidth);
		for (int i = 0; i < grid.length; i++)
			heights[i] += (int) (weightsH[i] / tweightsH * extraHeight);

		int y = container.getPadding().top;
		for (int i = 0; i < grid.length; i++) {
			int x = container.getPadding().left;
			for (int j = 0; j < grid[i].length; j++) {
				var data = grid[i][j];
				if (data != null) {
					var margin = data.data.margin;
					var dim = data.component.getPrefferedSize();
					int xx = x + (data.data.margin == null ? 0 : data.data.margin.left);
					int yy = y + (data.data.margin == null ? 0 : data.data.margin.top);
					int aw = Math.max(0,
							sum(widths, data.data.col, data.data.cols) - (margin != null ? margin.getHorizontal() : 0));
					int ah = Math.max(0,
							sum(heights, data.data.row, data.data.rows) - (margin != null ? margin.getVertical() : 0));
					int w = data.data.fillHorizontal ? aw : Math.min(aw, dim.getWidth());
					int h = data.data.fillVertical ? ah : Math.min(ah, dim.getHeight());

					int a = data.data.anchor;
					xx += is(a, RIGHT) ? aw - w : (is(a, CENTER_H) ? ((aw - w) / 2) : 0);
					yy += is(a, BOTTOM) ? ah - h : (is(a, CENTER_V) ? ((ah - h) / 2) : 0);

					data.component.setBounds(xx, yy, w, h);
				}
				x += widths[j];
			}
			y += heights[i];
		}
	}

	private boolean is(int a, int centerH) {
		return (a & centerH) == centerH;
	}

	@Override
	public void remove(FComponent component) {

	}

}
