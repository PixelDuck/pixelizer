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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main class.
 */
public class Pixelizer extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int PIXEL_SIZE = 10;
	private static final int MARGIN = 1;

	private static final int NB_PIXELS = 32;

	private ImagePanel imagePanel;
	private JMenuBar menuBar;
	private List<Pixels> pixelsRgba = new ArrayList<>();
    private int currentIndex = 0;
    private boolean[][] selectionMask;
    private List<Pixels> savedStates;
	private JColorChooser colorChooser;
    private int currentSaveIndex = 0;
    private JPanel toolsPanel;
    private JPanel tolerancePanel;
    private JPanel animationPanel;
    private JPanel colorPanel;
    private Tool toolSelected = Tool.PEN;
    private JProgressBar toleranceBar;

    private static class Pixels{
        private int[][] pixelsRgba;

        Pixels(){
            reset();
        }

        public void reset() {
            pixelsRgba = new int[NB_PIXELS][NB_PIXELS];
            for(int i=0; i<pixelsRgba.length; i++){
                for(int j=0; j<pixelsRgba[0].length; j++){
                    pixelsRgba[i][j] = 0xffffffff;
                }
            }
        }

        public int getColor(int i, int j){
            return pixelsRgba[i][j];
        }

        public void setColor(int i, int j, int color){
            pixelsRgba[i][j] = color;
        }

        public Pixels clone(){
            Pixels p = new Pixels();
            p.pixelsRgba = copyArray(pixelsRgba);
            return p;
        }
    }

    public JPanel getToolsPanel() {
        if(this.toolsPanel==null){
            this.toolsPanel = new JPanel(new GridBagLayout());
            ActionListener toolsActionListener = new ToolActionListener(this);
            int i=0;
            for(Tool t : Tool.values()){
                JToggleButton button = t.getButton();
                button.addActionListener(toolsActionListener);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = i++;
                toolsPanel.add(button, gbc);
            }
            toolSelected.getButton().setSelected(true);
        }
        return toolsPanel;
    }

    public JPanel getColorPanel() {
        if(this.colorPanel==null){
            this.colorPanel = new JPanel(new BorderLayout());
            colorChooser = new JColorChooser();
            colorChooser.setPreviewPanel(new JPanel());
            this.colorPanel.add(colorChooser, BorderLayout.CENTER);

        }
        return colorPanel;
    }

    public JPanel getTolerancePanel() {
        if(this.tolerancePanel==null){
            this.tolerancePanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 1;
            this.tolerancePanel.add(new JLabel("tolerance"), gbc);
            gbc.gridx = 2;
            toleranceBar = new JProgressBar();
            toleranceBar.setSize(new Dimension(200, 25));
            toleranceBar.setValue(50);
            toleranceBar.setMaximum(100);
            this.tolerancePanel.add(toleranceBar, gbc);
        }
        return tolerancePanel;
    }

    public JPanel getAnimationPanel() {
        if(this.animationPanel==null){
            this.animationPanel = new JPanel(new BorderLayout());
        }
        return animationPanel;
    }

    public ImagePanel getImagePanel() {
        if(imagePanel==null){
            this.imagePanel = new ImagePanel(this);
            this.imagePanel.setIgnoreRepaint(true);
            InternalMouseListener mouseListener = new InternalMouseListener(this);
            this.imagePanel.addMouseListener(mouseListener);
            this.imagePanel.addMouseMotionListener(mouseListener);
        }
        return imagePanel;
    }

    /**
     * Constructor.
     */
	public Pixelizer() {
        pixelsRgba.add(new Pixels());
        reset();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("Pixelizer");
        addKeyListener(new InternalKeyListener(this));

        savedStates = new ArrayList<>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createMenuBar();
		setJMenuBar(menuBar);

        createPanels();

		setSize(new Dimension(800, 700));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width) / 2,
				(screenSize.height - getSize().height) / 2);

		setVisible(true);

	}

    private static class ToolActionListener implements ActionListener{
        private final Pixelizer parent;

        ToolActionListener(Pixelizer parent) {
            this.parent = parent;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            Tool toolSelected = Tool.valueOf(e.getActionCommand());
            if(toolSelected!=this.parent.toolSelected){
                this.parent.toolSelected.getButton().setSelected(false);
                toolSelected.getButton().setSelected(true);
                this.parent.toolSelected = toolSelected;
            }
        }
    }

    private void createPanels() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(getImagePanel(), BorderLayout.CENTER);
        centerPanel.add(getTolerancePanel(), BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(getToolsPanel(), BorderLayout.WEST);
        mainPanel.add(getColorPanel(), BorderLayout.NORTH);


        setContentPane(mainPanel);
    }

    private void restorePreviousState(){
        if(currentSaveIndex>0){
            currentSaveIndex--;
            this.pixelsRgba.remove(currentIndex);
            this.pixelsRgba.add(currentIndex, savedStates.get(currentSaveIndex));
            repaint();

        }
    }

    private void restoreNextState(){
        if(currentSaveIndex<savedStates.size()-1){
            currentSaveIndex++;
            this.pixelsRgba.remove(currentIndex);
            this.pixelsRgba.add(currentIndex, savedStates.get(currentSaveIndex));
            repaint();
        }
    }

    private void saveBeforeModification(){
        while(savedStates.size()>currentSaveIndex){
            savedStates.remove(savedStates.size()-1);
        }
        savedStates.add(getCurrentPixels().clone());
        this.currentSaveIndex = savedStates.size();
    }

    private static int[][] copyArray(int[][] src){
        int[][] ret = new int[src.length][src[0].length];
        for (int i = 0; i < ret.length; i++) {
            System.arraycopy(src[i], 0, ret[i], 0, src[0].length);
        }
        return ret;
    }

    private void createMenuBar() {
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
    }


	private int getImageWidth() {
		return (PIXEL_SIZE + MARGIN) * NB_PIXELS + MARGIN;
	}

	private int getImageHeight() {
		return (PIXEL_SIZE + MARGIN) * NB_PIXELS + MARGIN;
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
			this.parent.applyToolOnPixel(xy[0], xy[1]);
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
			} else if (e.getActionCommand().equals("save_as_PNG")) {
                this.parent.actionSaveAsPNG();
			} else if (e.getActionCommand().equals("clear")) {
				this.parent.actionClearChange();
			}
		}
	}

	public class ImagePanel extends JPanel {
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
                if(image.getHeight()>PIXEL_SIZE || image.getWidth()%PIXEL_SIZE!=0){
                    int ret = JOptionPane.showConfirmDialog(this, "Image seems not a pixel image (height > " + PIXEL_SIZE + "px). Would you like to convert it to pixel?");
                    if(ret == JOptionPane.OK_OPTION){
                        this.parent.convertCurentImageToPixels();
                        repaint();
                    }
                } else {

                }
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

            for (int i = 0; i < NB_PIXELS; i++) {
                g2d.setPaint(Color.black);
                g2d.fillRect((i + 1) * (PIXEL_SIZE + MARGIN), 0, MARGIN, h);
                for (int j = 0; j < NB_PIXELS; j++) {
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
            for (int i = 0; i < NB_PIXELS; i++) {
                for (int j = 0; j < NB_PIXELS; j++) {
                    //int c = 0xffffffff;
                    int c = getCurrentPixels().getColor(i, j);
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

	public void updateColorChooser(int i, int j) {
		updateColorChooser(i, j, getCurrentPixels().getColor(i, j));
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

	public void applyToolOnPixel(int x, int y) {
        if (NB_PIXELS > x && NB_PIXELS > y) {
            switch(toolSelected){
                case PEN:
                    applyColor(x, y, this.colorChooser.getColor());
                    break;
                case CLEAR:
                    applyColor(x, y, new Color(0xffffffff));
                    break;
                default:
                    break;
            }
        }
    }

    public void applyColor(int x, int y, Color c){
        int px = 0xffffffff;
        if (c != null) {
            px = 0x00000000;
            px += c.getAlpha() << 24 & 0xff000000;
            px += c.getRed() << 16 & 0x00ff0000;
            px += c.getGreen() << 8 & 0x0000ff00;
            px += c.getBlue() & 0x000000ff;
        }
        if(getCurrentPixels().getColor(x, y)!=px){
            saveBeforeModification();
            getCurrentPixels().setColor(x, y, px);
            this.imagePanel.repaint();
        }
	}

    private Pixels getCurrentPixels() {
        return pixelsRgba.get(currentIndex);
    }

    public void convertCurentImageToPixels() {
        savedStates.clear();
		convertToPixelImage(this.imagePanel.image);
	}

	public void convertToPixelImage(BufferedImage image) {
		if (image != null) {
            actionClearChange();
			int nbImgPixelPerHPixel = image.getWidth() / NB_PIXELS;
			int nbImgPixelPerVPixel = image.getHeight() / NB_PIXELS;
			for (int h = 0; h < NB_PIXELS; h++) {
				for (int v = 0; v < NB_PIXELS; v++) {
					int count = 0;
                    long sumA = 0;
					long sumR = 0;
					long sumG = 0;
					long sumB = 0;
					for (int x = h * nbImgPixelPerHPixel; x < (h + 1)
							* nbImgPixelPerHPixel; x++) {
						for (int y = v * nbImgPixelPerVPixel; y < (v + 1)
								* nbImgPixelPerVPixel; y++) {
							int p = image.getRGB(x, y);
                            int a = (p & 0xff000000) >> 24;
                            sumA += a;
							int r = (p & 0x00ff0000) >> 16;
							sumR += r;
							int g = (p & 0x0000ff00) >> 8;
							sumG += g;
							int b = p & 0x000000ff;
							sumB += b;
							count++;
						}
					}
                    int c = 0x00000000;
					if (count != 0) {
                        c += (sumA / count << 24) & 0xff000000;
                        c += (sumR / count << 16) & 0x00ff0000;
                        c += (sumG / count << 8) & 0x0000ff00;
                        c += (sumB / count) & 0x000000ff;
					}
                    getCurrentPixels().setColor(h, v, c);
				}
			}
		}
	}

    public void reset(){
        getCurrentPixels().reset();
    }

    private void setCurrentPixel(int i, int j, int color){
        getCurrentPixels().setColor(i, j, color);
    }

	public void actionClearChange() {
        savedStates.clear();
		reset();
		this.imagePanel.repaint();
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
                BufferedImage bImg = new BufferedImage(NB_PIXELS, NB_PIXELS*this.pixelsRgba.size(), BufferedImage.TYPE_INT_ARGB_PRE);
                for (int c = 0; c < pixelsRgba.size(); c++) {
                    for (int i = 0; i < NB_PIXELS; i++) {
                        for (int j = 0; j < NB_PIXELS; j++) {
                            bImg.setRGB(i, j, toARGB(pixelsRgba.get(c).getColor(i, j)));
                        }
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

    private class InternalKeyListener implements KeyListener {
        private final Pixelizer parent;

        public InternalKeyListener(Pixelizer parent) {
            this.parent = parent;
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode()==KeyEvent.VK_Z && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0){
                this.parent.restorePreviousState();
            } else if(e.getKeyCode()==KeyEvent.VK_Y && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0){
                this.parent.restoreNextState();
            }
        }
    }
}
