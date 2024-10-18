package com.ngeneration.furthergui.graphics;

public interface Icon {

	int getWidth();

	int getHeight();
	
	void dispose();

	void paint(int x, int y, Graphics g);
	
}
