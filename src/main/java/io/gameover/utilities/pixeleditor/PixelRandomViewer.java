/*
 * Copyright © 2013, Olivier MARTIN, aka ekki77
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * The Software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. In no event shall the authors or copyright holders X be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or the use or other dealings in the Software.
 *
 * Except as contained in this notice, the name of the copyright holders shall not be used in advertising or otherwise to promote the sale, use or other dealings in this Software without prior written authorization from the copyright holders.
 */

package io.gameover.utilities.pixeleditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public class PixelRandomViewer extends JFrame{
	
	static final int size = 20;
	static final int margin = 1;
	static final int nb = 10;
	static final int offsetLeft = 8;
	static final int offsetRight = 8;
	static final int offsetTop = 35;
	static final int offsetBottom = 8;
	static final int w =(size+margin)*nb+margin+offsetRight+offsetLeft;
	static final int h = (size+margin)*nb+margin+offsetBottom+offsetTop;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PixelRandomViewer(){
		setTitle("Pixel viewer");
		setSize(new Dimension(w, h));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width-w)/2, (screenSize.height-h)/2);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createBufferStrategy(2);
		while(true)
			draw();
	}
	
	public static void main(String[] args) {
		new PixelRandomViewer();
	}
	
	public void draw() {
		BufferStrategy strategy = getBufferStrategy();
		Graphics2D g2d = 
		(Graphics2D)strategy.getDrawGraphics();
		g2d.setPaint(Color.BLACK);
		g2d.fillRect(0, 0, w, h);
		for(int i=0; i<nb; i++){
			for(int j=0; j<nb; j++){
				g2d.setPaint(new Color((int)(Math.random()*256), (int)(Math.random()*256), (int)(Math.random()*256), 255));
				g2d.fillRect(offsetLeft+margin+i*(size+margin), offsetTop+margin+j*(size+margin), size, size);
			}
		}
		g2d.dispose();
		strategy.show();
		Toolkit.getDefaultToolkit().sync();
	}
}
