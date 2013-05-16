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
    public static final int NO_COLOR_AS_INT = 0x00ffffff;
    private static final Color NO_COLOR = new Color(NO_COLOR_AS_INT);
    public static final String ACTION_OPEN = "open";
    public static final String ACTION_SAVE_AS_PNG = "save_as_PNG";
    public static final String ACTION_CLEAR = "clear";

    private ImagePanel imagePanel;
	private JMenuBar appMenuBar;
	private JColorChooser colorChooser;
    private JPanel toolsPanel;
    private JPanel tolerancePanel;
    private JPanel animationPanel;
    private JPanel colorPanel;
    private Tool toolSelected = Tool.PEN;
    private JProgressBar toleranceBar;

    private List<Frame> frames;
    private int currentFrameIndex;
    private boolean[][] selectionMask;
    private List<Frame> savedStates;
    private int currentStateIndex;


    /**
     * Constructor.
     */
    public Pixelizer() {
        frames = new ArrayList<>();
        frames.add(new Frame());
        currentFrameIndex = 0;
        selectionMask = new boolean[PIXEL_SIZE][PIXEL_SIZE];
        reset();
        applyLookAndFeel();
        setTitle("Pixelizer");
        addKeyListener(new InternalKeyListener(this));

        savedStates = new ArrayList<>();
        currentStateIndex = 0;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setJMenuBar(getAppMenuBar());

        initPanels();

        setSize(new Dimension(800, 700));
        centerFrame();
        setVisible(true);

    }

    private void centerFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width) / 2,
                (screenSize.height - getSize().height) / 2);
    }

    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Frame {
        private int[][] argb;

        public Frame(){
            reset();
        }

        public void reset() {
            argb = new int[NB_PIXELS][NB_PIXELS];
            for(int i=0; i< argb.length; i++){
                for(int j=0; j< argb[0].length; j++){
                    argb[i][j] = NO_COLOR_AS_INT;
                }
            }
        }

        public int getColor(int i, int j){
            return argb[i][j];
        }

        public void setColor(int i, int j, int color){
            argb[i][j] = color;
        }

        public Frame clone(){
            Frame p = new Frame();
            p.argb = copyArray(argb);
            return p;
        }
    }

    private void clearSelection(){
        for(int i=0; i<selectionMask.length; i++){
            for(int j=0; j<selectionMask[0].length; j++){
                selectionMask[i][j] = false;
            }
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
            this.tolerancePanel.add(new JLabel("Tolerance"), gbc);
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


    public static class ToolActionListener implements ActionListener{
        private final Pixelizer parent;

        public ToolActionListener(Pixelizer parent) {
            this.parent = parent;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            Tool toolSelected = Tool.valueOf(e.getActionCommand());
            if(toolSelected!=this.parent.toolSelected){
                if(toolSelected!=Tool.MAGIC_WAND&&toolSelected!=Tool.SELECT){
                    parent.clearSelection();
                }
                this.parent.toolSelected.getButton().setSelected(false);
                toolSelected.getButton().setSelected(true);
                this.parent.toolSelected = toolSelected;
            }
        }
    }

    private void initPanels() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(getImagePanel(), BorderLayout.CENTER);
        centerPanel.add(getTolerancePanel(), BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(getToolsPanel(), BorderLayout.WEST);
        mainPanel.add(getColorPanel(), BorderLayout.NORTH);
        mainPanel.add(getAnimationPanel(), BorderLayout.EAST);


        setContentPane(mainPanel);
    }

    private void restorePreviousState(){
        if(currentStateIndex >0){
            currentStateIndex--;
            this.frames.remove(currentFrameIndex);
            this.frames.add(currentFrameIndex, savedStates.get(currentStateIndex));
            repaint();

        }
    }

    private void restoreNextState(){
        if(currentStateIndex <savedStates.size()-1){
            currentStateIndex++;
            this.frames.remove(currentFrameIndex);
            this.frames.add(currentFrameIndex, savedStates.get(currentStateIndex));
            repaint();
        }
    }

    private void saveBeforeModification(){
        while(savedStates.size()> currentStateIndex){
            savedStates.remove(savedStates.size()-1);
        }
        savedStates.add(getCurrentFrame().clone());
        this.currentStateIndex = savedStates.size();
    }

    private static int[][] copyArray(int[][] src){
        int[][] ret = new int[src.length][src[0].length];
        for (int i = 0; i < ret.length; i++) {
            System.arraycopy(src[i], 0, ret[i], 0, src[0].length);
        }
        return ret;
    }

    private JMenuBar getAppMenuBar() {
        if(this.appMenuBar ==null){
            this.appMenuBar = new JMenuBar();

            JMenu menuOpen = new JMenu("File");
            JMenuItem openMenu = new JMenuItem("Open...");
            openMenu.setActionCommand(ACTION_OPEN);
            InternalActionListener actionListener = new InternalActionListener(this);
            openMenu.addActionListener(actionListener);
            menuOpen.add(openMenu);
            JMenuItem saveMenuAsPng = new JMenuItem("Save as PNG...");
            saveMenuAsPng.setActionCommand(ACTION_SAVE_AS_PNG);
            saveMenuAsPng.addActionListener(actionListener);
            menuOpen.add(saveMenuAsPng);
            this.appMenuBar.add(menuOpen);

            JMenu menuEdit = new JMenu("Edit");
            JMenuItem clearChangesMenu = new JMenuItem("Clear changes");
            clearChangesMenu.setActionCommand(ACTION_CLEAR);
            clearChangesMenu.addActionListener(actionListener);
            menuEdit.add(clearChangesMenu);
            appMenuBar.add(menuEdit);
        }

        return appMenuBar;
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
                doMouse(e);
			}
		}

        private void doMouse(MouseEvent e) {
            int[] xy = findPixelIndices(e.getX(), e.getY());
            switch (e.getButton()) {
                default:
                case MouseEvent.BUTTON1:
                    this.parent.applyFirstActionTool(xy[0], xy[1], (e.getModifiers() & KeyEvent.SHIFT_MASK) != 0, (e.getModifiers() & KeyEvent.CTRL_MASK) != 0);
                    break;
                case MouseEvent.BUTTON3:
                    this.parent.applySecondActionTool(xy[0], xy[1], (e.getModifiers() & KeyEvent.SHIFT_MASK) != 0, (e.getModifiers() & KeyEvent.CTRL_MASK) != 0);
                    break;
            }
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
				doMouse(e);
			}
		}

		private int[] findPixelIndices(int x, int y) {
			return new int[] {x / (PIXEL_SIZE + MARGIN),
                    y / (PIXEL_SIZE + MARGIN)};
		}
	}

    private void applySecondActionTool(int x, int y, boolean shiftPressed, boolean ctrlPressed) {
        switch(toolSelected){
            case PEN:
                updateColorChooser(x, y);
                break;
            default:
                break;
        }
    }

    private class InternalActionListener implements ActionListener {
		private Pixelizer parent;

		InternalActionListener(Pixelizer parent) {
			this.parent = parent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals(ACTION_OPEN)) {
				this.parent.actionOpenFile();
			} else if (e.getActionCommand().equals(ACTION_SAVE_AS_PNG)) {
                this.parent.actionSaveAsPNG();
			} else if (e.getActionCommand().equals(ACTION_CLEAR)) {
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
                if(image.getHeight()>NB_PIXELS || image.getWidth()%NB_PIXELS!=0){
                    int ret = JOptionPane.showConfirmDialog(this, "Image seems not a pixel image (height > " + NB_PIXELS+ "px or length not a multiple of "+NB_PIXELS+"). Would you like to convert it to pixel?");
                    if(ret == JOptionPane.OK_OPTION){
                        this.parent.convertCurentImageToPixels();
                    }
                } else {
                    int nb = image.getHeight()/NB_PIXELS;
                    this.parent.frames = new ArrayList<>(nb);
                    this.parent.currentFrameIndex =0;
                    for(int c=0; c<nb; c++){
                        Frame p = new Frame();
                        this.parent.frames.add(p);
                        for (int h = 0; h < NB_PIXELS; h++) {
                            for (int v = 0; v < NB_PIXELS; v++) {
                                p.setColor(h, v,  image.getRGB(h+c*NB_PIXELS, v));
                            }
                        }
                    }
                }
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

            for (int i = 0; i < NB_PIXELS; i++) {
                g2d.setPaint(Color.black);
                g2d.fillRect((i + 1) * (PIXEL_SIZE + MARGIN), 0, MARGIN, h);
                for (int j = 0; j < NB_PIXELS; j++) {
                    int c = getCurrentFrame().getColor(i, j);
                    g2d.setPaint(Utils.overlayWithColor(Color.GRAY, c));
                    g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN), MARGIN
                            + j * (PIXEL_SIZE + MARGIN), PIXEL_SIZE / 2,
                            PIXEL_SIZE / 2);
                    g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN)
                            + PIXEL_SIZE / 2, MARGIN + j
                            * (PIXEL_SIZE + MARGIN) + PIXEL_SIZE / 2,
                            PIXEL_SIZE / 2, PIXEL_SIZE / 2);
                    g2d.setPaint(Utils.overlayWithColor(Color.LIGHT_GRAY, c));
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
			g2d.dispose();
			Toolkit.getDefaultToolkit().sync();
		}

    }

	public void updateColorChooser(int i, int j) {
		updateColorChooser(i, j, getCurrentFrame().getColor(i, j));
	}

	public void updateColorChooser(int i, int j, int p) {
        int[] argb = Utils.extractARGB(p);
	    this.colorChooser.setColor(new Color(argb[1], argb[2], argb[3], argb[0]));
	}

	public void applyFirstActionTool(int x, int y, boolean shiftPressed, boolean ctrlPressed) {
        if (NB_PIXELS > x && NB_PIXELS > y) {
            switch(toolSelected){
                case PEN:
                    applyColor(x, y, this.colorChooser.getColor());
                    break;
                case CLEAR:
                    applyColor(x, y, NO_COLOR);
                    break;
                default:
                    break;
            }
        }
    }

    public void applyColor(int x, int y, Color c){
        int p = Utils.convertToColorAsInt(c);
        if(getCurrentFrame().getColor(x, y)!=p){
            saveBeforeModification();
            getCurrentFrame().setColor(x, y, p);
            this.imagePanel.repaint();
        }
	}

    private Frame getCurrentFrame() {
        return frames.get(currentFrameIndex);
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
                    long sumA=0, sumR=0, sumG=0, sumB=0;
					for (int x = h * nbImgPixelPerHPixel; x < (h + 1)
							* nbImgPixelPerHPixel; x++) {
						for (int y = v * nbImgPixelPerVPixel; y < (v + 1)
								* nbImgPixelPerVPixel; y++) {
 							int p = image.getRGB(x, y);
                            int[] argb = Utils.extractARGB(p);
                            sumA += argb[0];
							sumR += argb[1];
							sumG += argb[2];
							sumB += argb[3];
							count++;
						}
					}
                    int c = Utils.convertToColorAsInt(
                            (int)(sumA / count),
                            (int)(sumR / count),
                            (int)(sumG / count),
                            (int)(sumB / count));
                    getCurrentFrame().setColor(h, v, c);
				}
			}
		}
	}

    public void reset(){
        getCurrentFrame().reset();
    }

    private void setCurrentPixel(int i, int j, int color){
        getCurrentFrame().setColor(i, j, color);
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
		JFileChooser fc = new JFileChooser();
		int ret = fc.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
            this.imagePanel.setImage(fc.getSelectedFile());
		}
	}

    public void actionSaveAsPNG(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG image file", "png"));
        if(fileChooser.showDialog(this, "Save")==JFileChooser.APPROVE_OPTION){
            File f = fileChooser.getSelectedFile();
            try {
                BufferedImage bImg = new BufferedImage(NB_PIXELS, NB_PIXELS*this.frames.size(), BufferedImage.TYPE_INT_ARGB);
                for (int c = 0; c < frames.size(); c++) {
                    for (int i = 0; i < NB_PIXELS; i++) {
                        for (int j = 0; j < NB_PIXELS; j++) {
                            bImg.setRGB(i, j, frames.get(c).getColor(i, j));
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

    private class InternalKeyListener extends KeyAdapter{
        private final Pixelizer parent;

        public InternalKeyListener(Pixelizer parent) {
            this.parent = parent;
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
