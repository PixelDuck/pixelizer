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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main class.
 */
public class Pixelizer extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int PIXEL_SIZE = 10;
	private static final int MARGIN = 1;

	private int nbPixelsHorizontal = 32;
	private int nbPixelsVertical = 32;
	private ImagePanel imagePanel;
	private JMenuBar menuBar;
	private int[][] pixelsRgba;
    private List<Integer[][]> savedState;
	private ColorChooser colorChooser;

    /**
     * Constructor.
     */
	public Pixelizer() {
        reset();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("Pixelizer");

        savedState = new ArrayList<Integer[][]>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.menuBar = new JMenuBar();

		JMenu menuOpen = new JMenu("File");
		JMenuItem openMenu = new JMenuItem("Open...");
		openMenu.setActionCommand("open");
        InternalActionListener actionListener = new InternalActionListener(this);
        openMenu.addActionListener(actionListener);
		menuOpen.add(openMenu);
        JMenuItem saveMenuAsPng = new JMenuItem("Save as PNG...");
        saveMenuAsPng.setActionCommand("save_as_PNG");
        saveMenuAsPng.addActionListener(actionListener);
        menuOpen.add(saveMenuAsPng);
		this.menuBar.add(menuOpen);

		JMenu menuEdit = new JMenu("Edit");
		JMenuItem clearChangesMenu = new JMenuItem("Clear changes");
		clearChangesMenu.setActionCommand("clear");
		clearChangesMenu.addActionListener(actionListener);
		menuEdit.add(clearChangesMenu);
		menuBar.add(menuEdit);

		setJMenuBar(menuBar);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		this.imagePanel = new ImagePanel(this);
		InternalMouseListener mouseListener = new InternalMouseListener(this);
		this.imagePanel.addMouseListener(mouseListener);
		this.imagePanel.addMouseMotionListener(mouseListener);
		mainPanel.add(imagePanel, BorderLayout.CENTER);
		setContentPane(mainPanel);

		setSize(new Dimension(getImageWidth() + 6, getImageHeight()
				+ (int) menuBar.getPreferredSize().getHeight() + 60));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width) / 2,
				(screenSize.height - getSize().height) / 2);

		setVisible(true);

		this.imagePanel.setIgnoreRepaint(true);
		colorChooser = new ColorChooser();
	}

	private class ColorChooser extends JFrame {
		private JColorChooser colorChooser;
		private boolean noColor = true;
		private JToggleButton noColorBTN;

		public ColorChooser() {
			setTitle("Color");
			setSize(600, 400);
			JPanel colorChooserPanel = new JPanel();
			colorChooserPanel.setLayout(new BorderLayout());
			noColorBTN = new JToggleButton("No color");
			noColorBTN.setSelected(noColor);
			noColorBTN.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					noColor = true;
				}
			});
			colorChooserPanel.add(noColorBTN, BorderLayout.NORTH);
			this.colorChooser = new JColorChooser();
			this.colorChooser.getSelectionModel().addChangeListener(
					new ChangeListener() {

						@Override
						public void stateChanged(ChangeEvent e) {
							noColor = false;
						}
					});
			colorChooserPanel.add(this.colorChooser);
			setContentPane(colorChooserPanel);
		}

		public Color getColor() {
			if (noColor)
				return null;
			else
				return colorChooser.getColor();
		}

		public void setColor(Color c) {
			if (c == null) {
				noColorBTN.setSelected(true);
				noColorBTN.setEnabled(false);
				colorChooser.setColor(null);
				noColor = true;
			} else {
				noColorBTN.setSelected(false);
				noColorBTN.setEnabled(true);
				colorChooser.setColor(c);
				noColor = false;
			}
			this.noColorBTN.updateUI();
			this.colorChooser.updateUI();
		}
	}


	private int getImageWidth() {
		return (PIXEL_SIZE + MARGIN) * getNbH() + MARGIN;
	}

	private int getImageHeight() {
		return (PIXEL_SIZE + MARGIN) * getNbV() + MARGIN;
	}

	private class InternalMouseListener extends MouseAdapter {
		private Pixelizer parent;
		private boolean isMouseIn;
		private boolean isMousePressed;

		InternalMouseListener(Pixelizer parent) {
			this.parent = parent;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (isMouseIn && isMousePressed) {
				this.isMousePressed = false;
				switch (e.getButton()) {
				default:
				case MouseEvent.BUTTON1:
					updatePaint(e.getX(), e.getY());
					break;
				case MouseEvent.BUTTON3:
					int[] xy = findPixelIndices(e.getX(), e.getY());
					this.parent.updateColorChooser(xy[0], xy[1]);
					break;
				}
			}
		}

		private void updatePaint(int x, int y) {
			int[] xy = findPixelIndices(x, y);
			this.parent.updatePixelFromSelectedColor(xy[0], xy[1]);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			this.isMouseIn = true;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			this.isMouseIn = false;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			this.isMousePressed = true;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (isMouseIn && isMousePressed) {
				updatePaint(e.getX(), e.getY());
			}
		}

		private int[] findPixelIndices(int x, int y) {
			return new int[] { (int) (x / (PIXEL_SIZE + MARGIN)),
					(int) (y / (PIXEL_SIZE + MARGIN)) };
		}
	}

	private class InternalActionListener implements ActionListener {
		private Pixelizer parent;

		InternalActionListener(Pixelizer parent) {
			this.parent = parent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("open")) {
				this.parent.actionOpenFile();
			} else if (e.getActionCommand().equals("save_as_code")) {
				this.parent.actionSaveSourceCode();
            } else if (e.getActionCommand().equals("save_as_PNG")) {
                this.parent.actionSaveAsPNG();
			} else if (e.getActionCommand().equals("clear")) {
				this.parent.actionClearChange();
			}
		}
	}

	private class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private BufferedImage image;
		private Pixelizer parent;

		public ImagePanel(Pixelizer parent) {
			this.parent = parent;
			setDoubleBuffered(true);
		}

		public void setImage(File imageFile) {
			try {
				image = ImageIO.read(imageFile);
				this.parent.convertCurentImageToPixels();
				repaint();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;
			int w = this.parent.getImageWidth();
			int h = this.parent.getImageHeight();

			if (image != null)
				g2d.drawImage(image, 0, 0, w, h, null);

            for (int i = 0; i < this.parent.getNbH(); i++) {
                g2d.setPaint(Color.black);
                g2d.fillRect((i + 1) * (PIXEL_SIZE + MARGIN), 0, MARGIN, h);
                for (int j = 0; j < this.parent.getNbV(); j++) {
                    g2d.setPaint(Color.GRAY);
                    g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN), MARGIN
                            + j * (PIXEL_SIZE + MARGIN), PIXEL_SIZE / 2,
                            PIXEL_SIZE / 2);
                    g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN)
                            + PIXEL_SIZE / 2, MARGIN + j
                            * (PIXEL_SIZE + MARGIN) + PIXEL_SIZE / 2,
                            PIXEL_SIZE / 2, PIXEL_SIZE / 2);
                    g2d.setPaint(Color.LIGHT_GRAY);
                    g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN)
                            + PIXEL_SIZE / 2, MARGIN + j
                            * (PIXEL_SIZE + MARGIN), PIXEL_SIZE / 2,
                            PIXEL_SIZE / 2);
                    g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN), MARGIN
                            + j * (PIXEL_SIZE + MARGIN) + PIXEL_SIZE / 2,
                            PIXEL_SIZE / 2, PIXEL_SIZE / 2);
                    g2d.setPaint(Color.black);
                    g2d.fillRect(0, (j + 1) * (PIXEL_SIZE + MARGIN), w,
                            MARGIN);
                }
            }
            g2d.setPaint(Color.black);
            g2d.fillRect(0, 0, w, MARGIN);
            g2d.fillRect(0, 0, MARGIN, h);
            for (int i = 0; i < this.parent.getNbH(); i++) {
                for (int j = 0; j < this.parent.getNbV(); j++) {
                    //int c = 0xffffffff;
                    int c = pixelsRgba[i][j];
                    if (c != 0xffffffff) {
                        g2d.setPaint(new Color(c));
                        g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN),
                                MARGIN + j * (PIXEL_SIZE + MARGIN),
                                PIXEL_SIZE, PIXEL_SIZE);
                    }
                }
            }
			g2d.dispose();
			Toolkit.getDefaultToolkit().sync();
		}
	}

	public int getNbH() {
		return this.nbPixelsHorizontal;
	}

	public void updateColorChooser(int i, int j) {
		updateColorChooser(i, j, this.pixelsRgba[i][j]);
	}

	public void updateColorChooser(int i, int j, int p) {
		int r = (p & 0x00ff0000) >> 16;
		int g = (p & 0x0000ff00) >> 8;
		int b = p & 0x000000ff;
		if (p == 0x00000000)
			this.colorChooser.setColor(null);
		else
			this.colorChooser.setColor(new Color(r, g, b));
	}

	public void updatePixelFromSelectedColor(int x, int y) {
		if (pixelsRgba.length > x && pixelsRgba[0].length > y) {
			Color c = this.colorChooser.getColor();
			if (c != null) {
				pixelsRgba[x][y] = 0x00000000;
				pixelsRgba[x][y] += c.getRed() << 16 & 0x00ff0000;
				pixelsRgba[x][y] += c.getGreen() << 8 & 0x0000ff00;
				pixelsRgba[x][y] += c.getBlue() & 0x000000ff;
			} else {
				pixelsRgba[x][y] = 0xffffffff;
			}
			this.imagePanel.repaint();
		}
	}

	public void convertCurentImageToPixels() {
		convertToPixelImage(this.imagePanel.image);
	}

	public void convertToPixelImage(BufferedImage image) {
		if (image != null) {
            actionClearChange();
			int nbImgPixelPerHPixel = image.getWidth() / getNbH();
			int nbImgPixelPerVPixel = image.getHeight() / getNbV();
			for (int h = 0; h < getNbH(); h++) {
				for (int v = 0; v < getNbV(); v++) {
					int count = 0;
					long sumR = 0;
					long sumG = 0;
					long sumB = 0;
					for (int x = h * nbImgPixelPerHPixel; x < (h + 1)
							* nbImgPixelPerHPixel; x++) {
						for (int y = v * nbImgPixelPerVPixel; y < (v + 1)
								* nbImgPixelPerVPixel; y++) {
							int p = image.getRGB(x, y);
							int r = (p & 0x00ff0000) >> 16;
							sumR += r;
							int g = (p & 0x0000ff00) >> 8;
							sumG += g;
							int b = p & 0x000000ff;
							sumB += b;
							count++;
						}
					}
                    pixelsRgba[h][v] = 0x00000000;
					if (count != 0) {
                        pixelsRgba[h][v] += (sumR / count << 16) & 0x00ff0000;
                        pixelsRgba[h][v] += (sumG / count << 8) & 0x0000ff00;
                        pixelsRgba[h][v] += (sumB / count) & 0x000000ff;
					}
				}
			}
		}
	}

    public void reset(){
        pixelsRgba = new int[getNbH()][getNbV()];
        for(int i=0; i<pixelsRgba.length; i++){
            for(int j=0; j<pixelsRgba[0].length; j++){
                pixelsRgba[i][j] = 0xffffffff;
            }
        }
    }

	public void actionClearChange() {
		reset();
		this.imagePanel.repaint();
	}

	public int getNbV() {
		return this.nbPixelsVertical;
	}

	public static void main(String[] args) {
		new Pixelizer();
	}

	public void actionOpenFile() {
		JFileChooser fc = new JFileChooser("e:");
		int ret = fc.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			showImage(fc.getSelectedFile());
		}

	}

    public void actionSaveAsPNG(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG image file", "png"));
        if(fileChooser.showDialog(this, "Save")==JFileChooser.APPROVE_OPTION){
            File f = fileChooser.getSelectedFile();
            try {
                BufferedImage bImg = new BufferedImage(pixelsRgba.length, pixelsRgba[0].length, BufferedImage.TYPE_INT_ARGB_PRE);
                for (int i = 0; i < pixelsRgba.length; i++) {
                    for (int j = 0; j < pixelsRgba[i].length; j++) {
                        bImg.setRGB(i, j, toARGB(pixelsRgba[i][j]));
                    }
                }

                ImageIO.write(bImg, "png", f);
                JOptionPane.showMessageDialog(this, "File "+f.getName()+" saved!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to save "+f.getName()+": "+e.getMessage(), "Error occured", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

        }
    }

    private int toARGB(int rgba) {
        String ret = Integer.toHexString(rgba).toUpperCase();
        if(ret.length()<=6){
            return rgba | 0xff000000;
        } else {
            return Integer.parseInt(ret.substring(6)+ret.substring(0, 5), 16);
        }
    }

    public void actionSaveSourceCode() {
		StringBuilder code = new StringBuilder(
				"Integer[][] pixels = new Integer[][]{\n");
		for (int i = 0; i < pixelsRgba.length; i++) {
			if (i != 0) {
				code.append("\n,");
			}
			code.append("{");
			for (int j = 0; j < pixelsRgba[i].length; j++) {
				if (j != 0) {
					code.append(",");
				}
				code.append(toHex(pixelsRgba[i][j]));
			}
			code.append("}");
		}
		code.append("\n};");
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = new StringSelection(code.toString());
		clipboard.setContents(transferable, null);
		JOptionPane.showMessageDialog(this, "Source code copied");
	}

	private String toHex(Integer i) {
		String ret = Integer.toHexString(i);
		if (ret.length() == 6) {
			ret = "0x00" + ret;
		} else {
			ret = "0x" + ret;
		}
		return ret;
	}

	private void showImage(File selectedFile) {
		this.imagePanel.setImage(selectedFile);
	}

}
