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
