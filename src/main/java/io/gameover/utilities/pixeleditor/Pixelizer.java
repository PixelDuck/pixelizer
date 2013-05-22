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

import io.gameover.utilities.pixeleditor.colorchooser.ColorChooserPanel;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
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
    public static final int NO_COLOR_AS_INT = 0x00000000;
    private static final Color NO_COLOR = new Color(NO_COLOR_AS_INT);
    public static final String ACTION_OPEN = "open";
    public static final String ACTION_SAVE_AS_PNG = "save_as_PNG";
    public static final String ACTION_CLEAR = "clear";

    private ImagePanel imagePanel;
	private JMenuBar appMenuBar;
    private ColorChooserPanel colorChooser;
    private JPanel toolsPanel;
    private JPanel tolerancePanel;
    private JPanel animationPanel;
    private JPanel colorPanel;
    private Tool toolSelected = Tool.PEN;
    private JProgressBar toleranceBar;
    private JPanel selectFramePanel;

    private List<Frame> frames;
    private int currentFrameIndex;
    private boolean[][] selectionMask;
    private List<Frame> savedStates;
    private int currentStateIndex;
    private List<JToggleButton> selectFrameButtons;
    private ActionListener selectFrameActionListener;
    private JPanel animation;


    /**
     * Constructor.
     */
    public Pixelizer() {
        setFocusable(true);
        requestFocus();
        frames = new ArrayList<>();
        frames.add(new Frame());
        currentFrameIndex = 0;
        selectionMask = new boolean[PIXEL_SIZE][PIXEL_SIZE];
        reset();
        applyLookAndFeel();
        setTitle("Pixelizer");

        savedStates = new ArrayList<>();
        currentStateIndex = 0;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setJMenuBar(getAppMenuBar());

        initPanels();

        addKeyListener(new InternalKeyListener(this));
        setSize(new Dimension(800, 700));
        centerFrame();
        setVisible(true);
        ((JPanel)getContentPane()).updateUI();
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

    public JPanel getSelectFramePanel() {
        if(selectFramePanel==null){
            selectFramePanel = new JPanel(new GridBagLayout());
            this.selectFrameButtons = new ArrayList<>();
            fillPanelWithButton(1);
        }
        return selectFramePanel;
    }

    public ActionListener getSelectFrameActionListener() {
        if(selectFrameActionListener==null){
            selectFrameActionListener = new SelectFrameActionListener(this);
        }
        return selectFrameActionListener;
    }

    public JPanel getAnimation() {
        if(this.animation==null){
            this.animation = new AnimationImagePanel(this, 12);
        }
        return animation;
    }

    public JProgressBar getToleranceBar() {
        if(toleranceBar==null){
            toleranceBar = new JProgressBar();
            toleranceBar.setPreferredSize(new Dimension(200, 25));
            toleranceBar.setValue(10);
            toleranceBar.setMaximum(100);
            toleranceBar.setStringPainted(true);
            toleranceBar.addMouseListener(new MouseAdapter() {
                public boolean mouseEntered = false;

                @Override
                public void mouseReleased(MouseEvent e) {
                    if(mouseEntered){
                        JProgressBar pb = (JProgressBar)e.getComponent();
                        pb.setValue((int) ((((double)e.getX())/pb.getSize().getWidth())*100d));
                        pb.updateUI();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    this.mouseEntered = true;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    this.mouseEntered = false;
                }
            });
        }
        return toleranceBar;
    }

    public int getNbPixels() {
        return NB_PIXELS;
    }

    public int getPixelSize() {
        return PIXEL_SIZE;
    }

    public int getMargin() {
        return MARGIN;
    }

    public List<Frame> getPixelFrames() {
        return frames;
    }

    public static class SelectFrameActionListener implements ActionListener{
        private Pixelizer parent;
        public SelectFrameActionListener(Pixelizer parent){
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = Integer.parseInt(e.getActionCommand());
            for(int i=0; i<this.parent.frames.size(); i++){
                JToggleButton button = this.parent.selectFrameButtons.get(i);
                if(i!=index && button.isSelected()){
                    button.setSelected(false);
                }
            }
            this.parent.selectFrame(index);
        }
    }

    private void selectFrame(int index) {
        this.currentFrameIndex=index;
        this.savedStates.clear();
        repaint();
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

        public void fillColor(int x, int y, int newColor, int tolerance) {
            List<Point> points = findPoint(x, y, getColor(x, y), tolerance);
            for(Point p : points){
                setColor(p.x, p.y, newColor);
            }
        }

        public List<Point> findPoint(int x, int y, int color, int tolerance) {
            Set<Point> scanned = new HashSet<>();
            List<Point> ret = new ArrayList<>();
            findPointAux(x, y, color, tolerance, ret, scanned);
            return ret;
        }

        private void findPointAux(int x, int y, int color, int tolerance, List<Point> ret, Set<Point> scanned) {
            Point p = new Point(x, y);
            if(!scanned.contains(p)){
                scanned.add(p);
                if(x>=0 && x<argb.length
                        && y>=0 && y<argb[0].length
                        && isColorClosed(getColor(x,y), color, tolerance)){
                    ret.add(p);
                    findPointAux(x-1, y, color, tolerance, ret, scanned);
                    findPointAux(x+1, y, color, tolerance, ret, scanned);
                    findPointAux(x, y-1, color, tolerance, ret, scanned);
                    findPointAux(x, y+1, color, tolerance, ret, scanned);
                }
            }
        }

        private boolean isColorClosed(int color1, int color2, int tolerance) {
            int t = 256*tolerance/100;
            int[] argb1 = ColorUtils.extractARGB(color1);
            int[] argb2 = ColorUtils.extractARGB(color2);
            boolean ok = true;
            for(int i=0; i<4;i++){
                ok &= ((argb1[i]-t) < argb2[i] && argb2[i]< (argb1[i]+t));
            }
            return ok;
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
                gbc.insets = new Insets(2, 2, 2, 2);
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
            colorChooser = new ColorChooserPanel();
            this.colorPanel.add(colorChooser, BorderLayout.CENTER);

        }
        return colorPanel;
    }

    public JPanel getTolerancePanel() {
        if (this.tolerancePanel == null) {
            this.tolerancePanel = new JPanel(new GridBagLayout());
            Insets i = new Insets(2,2,2,2);
            this.tolerancePanel.add(new JLabel("Tolerance"), LayoutUtils.xyi(1, 1, 0d, 0d, i));
            JButton clearToleranceBTN = new JButton("-");
            clearToleranceBTN.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    getToleranceBar().setValue(0);
                }
            });
            this.tolerancePanel.add(clearToleranceBTN, LayoutUtils.xyi(2, 1, 0d, 0d, i));
            this.tolerancePanel.add(getToleranceBar(), LayoutUtils.xyi(3, 1, 0d, 0d, i));
            JButton maxToleranceBTN = new JButton("+");
            maxToleranceBTN.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    getToleranceBar().setValue(100);
                }
            });
            this.tolerancePanel.add(maxToleranceBTN, LayoutUtils.xyi(4, 1, 0d, 0d, i));
        }
        return tolerancePanel;
    }

    public JPanel getAnimationPanel() {
        if (this.animationPanel == null) {
            this.animationPanel = new JPanel(new BorderLayout());
            this.animationPanel.add(getAnimation(), BorderLayout.CENTER);
            this.animationPanel.add(getSelectFramePanel(), BorderLayout.WEST);
        }
        return animationPanel;
    }

    private void fillPanelWithButton(int nbFrames){
        this.selectFramePanel.removeAll();
        this.selectFrameButtons.clear();
        for(int i=0; i<nbFrames; i++){
            addSelectFrameButtonToPanel(i, createSelectFrameButton(i + 1));
        }
        selectFrameButtons.get(0).setSelected(true);
        this.selectFramePanel.updateUI();
    }

    public void addSelectFrameButtonToPanel(int index, JToggleButton btn){
        this.selectFramePanel.add(btn, LayoutUtils.xyi(index % 4 + 1, index / 4 + 1, 0d, 0d, new Insets(1, 1, 1, 1)));
    }

    private void addNewFrame(int index, int copyIndex){
        Frame f = null;
        if(copyIndex!=-1){
            f = this.frames.get(copyIndex).clone();
        } else {
            f = new Frame();
        }
        this.frames.add(index, f);
        this.currentFrameIndex = index;
        this.selectFramePanel.add(createSelectFrameButton(this.frames.size()));
        repaint();
    }

    private JToggleButton createSelectFrameButton(int index) {
        JToggleButton button = new JToggleButton(""+index);
        button.setPreferredSize(new Dimension(20,20));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFont(button.getFont().deriveFont(9f));
        button.setActionCommand("" + (index-1));
        button.addActionListener(
                getSelectFrameActionListener());
        this.selectFrameButtons.add(button);
        return button;
    }

    private void removeFrame(int index){
        if(this.frames.size()>1){
            if(this.currentFrameIndex == index){
                if(this.currentFrameIndex==this.frames.size()-1){
                    this.currentFrameIndex--;
                }
            }
            this.frames.remove(index);
            repaint();
        }
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
        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(2, 2, 2 ,2);

        mainPanel.add(getAnimationPanel(), LayoutUtils.xywi(1, 1, 3, 1.0d, 0.0d, insets));
        mainPanel.add(getToolsPanel(), LayoutUtils.xyi(1, 2, 0.0d, 0.0d, insets));
        mainPanel.add(new JScrollPane(getImagePanel()), LayoutUtils.xyi(2, 2, 1.0d, 1.0d, insets));
        mainPanel.add(getColorPanel(), LayoutUtils.xyi(3, 2, 0.0d, 0.0d, insets));
        mainPanel.add(getTolerancePanel(), LayoutUtils.xyi(2, 3, 0.0d, 0.0d, insets));

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


	public int getImageWidth() {
		return (PIXEL_SIZE + MARGIN) * NB_PIXELS + MARGIN;
	}

	public int getImageHeight() {
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
            boolean shiftPressed = (e.getModifiers() & KeyEvent.SHIFT_MASK) != 0;
            boolean ctrlPressed = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0;
            boolean altPressed = (e.getModifiers() & KeyEvent.ALT_MASK) != 0;
            if(SwingUtilities.isLeftMouseButton(e)){
                this.parent.doFirstActionTool(xy[0], xy[1], shiftPressed, ctrlPressed, altPressed);
            } else if(SwingUtilities.isRightMouseButton(e)){
                this.parent.doSecondActionTool(xy[0], xy[1], shiftPressed, ctrlPressed, altPressed);
            } else if(SwingUtilities.isMiddleMouseButton(e)){
                this.parent.doThirdActionTool(xy[0], xy[1], shiftPressed, ctrlPressed, altPressed);
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

	public void updateColorChooser(int i, int j) {
		updateColorChooser(i, j, getCurrentFrame().getColor(i, j));
	}

	public void updateColorChooser(int i, int j, int p) {
        int[] argb = ColorUtils.extractARGB(p);
	    this.colorChooser.setColor(argb);
	}

	public void doFirstActionTool(int x, int y, boolean shiftPressed, boolean ctrlPressed, boolean altPressed) {
        requestFocus();
        if (NB_PIXELS > x && NB_PIXELS > y) {
            switch(toolSelected){
                case PEN:
                    applyColor(x, y, this.colorChooser.getColor());
                    break;
                case CLEAR:
                    applyColor(x, y, NO_COLOR);
                    break;
                case FILL:
                    fillColor(x, y, this.colorChooser.getColor());
                    break;
                case MAGIC_WAND:

                case MOVE:
                    break;
                case SELECT:
                    this.selectionMask[x][y] = true;
                    break;
                default:
                    break;
            }
        }
    }


    private void doSecondActionTool(int x, int y, boolean shiftPressed, boolean ctrlPressed, boolean altPressed) {
        requestFocus();
        switch(toolSelected){
            case PEN:
            case FILL:
                updateColorChooser(x, y);
                break;
            case CLEAR:
            case MAGIC_WAND:
            case MOVE:
                break;
            case SELECT:
                this.selectionMask[x][y] = false;
                break;
            default:
                break;
        }
    }

    private void doThirdActionTool(int x, int y, boolean shiftPressed, boolean ctrlPressed, boolean altPressed) {
        requestFocus();
        switch(toolSelected){
            case PEN:
                applyColor(x, y, NO_COLOR);
                break;
            case FILL:
            case CLEAR:
            case MAGIC_WAND:
            case MOVE:
            case SELECT:
                //TODO
                break;
            default:
                break;
        }
    }

    public void applyColor(int x, int y, Color c){
        applyColor(x, y, ColorUtils.convertToColorAsInt(c));
	}

    public void applyColor(int x, int y, int c){
        if(getCurrentFrame().getColor(x, y)!=c){
            saveBeforeModification();
            getCurrentFrame().setColor(x, y, c);
            this.imagePanel.repaint();
        }
    }

    public void fillColor(int x, int y, int c){
        if(getCurrentFrame().getColor(x, y)!=c){
            saveBeforeModification();
            getCurrentFrame().fillColor(x, y, c, getToleranceBar().getValue());
            this.imagePanel.repaint();
        }
    }

    public Frame getCurrentFrame() {
        if(currentFrameIndex>=0 && currentFrameIndex<frames.size()){
            return frames.get(currentFrameIndex);
        } else
            return null;
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
                            int[] argb = ColorUtils.extractARGB(p);
                            sumA += argb[0];
							sumR += argb[1];
							sumG += argb[2];
							sumB += argb[3];
							count++;
						}
					}
                    int c = ColorUtils.convertToColorAsInt(
                            (int) (sumA / count),
                            (int) (sumR / count),
                            (int) (sumG / count),
                            (int) (sumB / count));
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
            try {
                BufferedImage image = ImageIO.read(fc.getSelectedFile());
                if(image.getHeight()>NB_PIXELS || image.getWidth()%NB_PIXELS!=0 || image.getHeight()!=NB_PIXELS){
                    int retOption = JOptionPane.showConfirmDialog(this, "Image seems not a pixel image (height > " + NB_PIXELS+ "px or length not a multiple of "+NB_PIXELS+"). Would you like to convert it to pixel?");
                    if(retOption == JOptionPane.OK_OPTION){
                        convertToPixelImage(image);
                    }
                } else {
                    int nb = image.getWidth()/NB_PIXELS;
                    this.frames = new ArrayList<>(nb);
                    this.currentFrameIndex = 0;
                    for(int c=0; c<nb; c++){
                        Frame p = new Frame();
                        this.frames.add(p);
                        for (int x = 0; x < NB_PIXELS; x++) {
                            for (int y = 0; y < NB_PIXELS; y++) {
                                p.setColor(x, y,  image.getRGB(x + c*NB_PIXELS, y));
                            }
                        }
                    }
                    fillPanelWithButton(nb);
                }
                repaint();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
		}
	}

    public void actionSaveAsPNG(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG image file", "png"));
        if(fileChooser.showDialog(this, "Save")==JFileChooser.APPROVE_OPTION){
            File f = fileChooser.getSelectedFile();
            try {
                BufferedImage bImg = new BufferedImage(NB_PIXELS*this.frames.size(), NB_PIXELS, BufferedImage.TYPE_INT_ARGB);
                for (int c = 0; c < frames.size(); c++) {
                    for (int i = 0; i < NB_PIXELS; i++) {
                        for (int j = 0; j < NB_PIXELS; j++) {
                            bImg.setRGB(i+c*NB_PIXELS, j, frames.get(c).getColor(i, j));
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
