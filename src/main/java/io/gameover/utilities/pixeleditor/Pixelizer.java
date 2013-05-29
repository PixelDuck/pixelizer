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
import io.gameover.utilities.pixeleditor.utils.AnimatedGifEncoder;
import io.gameover.utilities.pixeleditor.utils.ColorUtils;
import io.gameover.utilities.pixeleditor.utils.Encoder;
import io.gameover.utilities.pixeleditor.utils.LayoutUtils;
import io.gameover.utilities.pixeleditor.utils.PngEncoder;
import io.gameover.utilities.pixeleditor.utils.Utilities;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main class.
 *
 * icons are taken from tango project.
 *
 * http://jimmac.musichall.cz/themes.php?skin=7
 */
public class Pixelizer extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final String ACTION_OPEN = "open";
    public static final String ACTION_SAVE_AS_PNG = "save_as_PNG";
    public static final String ACTION_SAVE_AS_GIF = "save_as_GIF";
    public static final String ACTION_CLEAR = "clear";
    public static final String ACTION_CLEAR_SELECTION = "clearSelection";

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

    private Point selectionPoint = new Point(0, 0);
    private boolean[][] selectionMask;
    private List<State> savedStates;
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
        selectionMask = new boolean[Frame.NB_PIXELS][Frame.NB_PIXELS];
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
        refresh();
    }

    private void refresh() {
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

    public boolean isSelected(int x, int y){
        return this.selectionMask[x][y];
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
            this.animation = new AnimationImagePanel(this, 5);
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

    public List<Frame> getPixelFrames() {
        return frames;
    }

    public void moveFrameRight(int frameIndex) {
        Frame frame = frames.get(frameIndex);
        frames.remove(frameIndex);
        frames.add(frameIndex+1, frame);
        this.currentFrameIndex = frameIndex+1;
        this.animationPanel.updateUI();
    }

    public void moveFrameLeft(int frameIndex) {
        Frame frame = frames.get(frameIndex);
        frames.remove(frameIndex);
        frames.add(frameIndex-1, frame);
        this.currentFrameIndex = frameIndex-1;
        this.animationPanel.updateUI();
    }

    public void insertFrameRight(int frameIndex) {
        insertNewFrame(frameIndex + 1, frameIndex);
    }

    public void insertFrameLeft(int frameIndex) {
        insertNewFrame(frameIndex, frameIndex);
    }

    public static class SelectFramePopClickListener extends MouseAdapter {
        private final Pixelizer parent;
        private final int frameIndex;

        public SelectFramePopClickListener(Pixelizer parent, int frameIndex) {
            this.frameIndex = frameIndex;
            this.parent = parent;
        }

        public void mousePressed(MouseEvent e){
            if (e.isPopupTrigger())
                doPop(e);
        }

        public void mouseReleased(MouseEvent e){
            if (e.isPopupTrigger())
                doPop(e);
        }

        private void doPop(MouseEvent e){
            AnimationFrameContextMenu menu = new AnimationFrameContextMenu(this.parent, frameIndex);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public static class SelectFrameActionListener implements ActionListener{
        private Pixelizer parent;
        public SelectFrameActionListener(Pixelizer parent){
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = Integer.parseInt(e.getActionCommand());
            this.parent.selectFrame(index);
        }
    }

    private void selectFrame(int index) {
        if(index>=0 && index<frames.size()){
            this.currentFrameIndex=index;
            clearSelection();
            this.savedStates.clear();
            currentStateIndex = 0;
            for(int i=0; i<this.frames.size(); i++){
                JToggleButton button = this.selectFrameButtons.get(i);
                if(i!=index && button.isSelected()){
                    button.setSelected(false);
                } else if(i==index && !button.isSelected()){
                    button.setSelected(true);
                }
            }
            refresh();
        }
    }

    private void clearSelection(){
        for(int i=0; i<selectionMask.length; i++){
            for(int j=0; j<selectionMask[0].length; j++){
                selectionMask[i][j] = false;
            }
        }
        selectionPoint = new Point(0, 0);
        repaint();
    }

    private void deleteSelection(){
        for(int i=0; i<selectionMask.length; i++){
            for(int j=0; j<selectionMask[0].length; j++){
                if(selectionMask[i][j]){
                    getCurrentFrame().setColor(i, j, Frame.NO_COLOR_AS_INT);
                }
            }
        }
        clearSelection();
        repaint();
    }

    private void selectAll(){
        for(int i=0; i<selectionMask.length; i++){
            for(int j=0; j<selectionMask[0].length; j++){
                selectionMask[i][j] = true;
            }
        }
        repaint();
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

    public void insertNewFrame(int index, int copyIndex){
        Frame f;
        if(copyIndex!=-1){
            f = this.frames.get(copyIndex).clone();
        } else {
            f = new Frame();
        }
        this.frames.add(index, f);
        this.currentFrameIndex = index;
        this.selectFramePanel.add(createSelectFrameButton(this.frames.size()), LayoutUtils.xyi(frames.size() % 4 + 1, index / 4 + 1, 0d, 0d, new Insets(1, 1, 1, 1)));
        refresh();
    }

    private JToggleButton createSelectFrameButton(int index) {
        JToggleButton button = new JToggleButton(""+index);
        button.setPreferredSize(new Dimension(20,20));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFont(button.getFont().deriveFont(9f));
        button.setActionCommand("" + (index - 1));
        button.addActionListener(
                getSelectFrameActionListener());
        button.addMouseListener(new SelectFramePopClickListener(this, index-1));
        this.selectFrameButtons.add(button);
        return button;
    }

    public void removeFrame(int index){
        if(this.frames.size()>1){
            if(index==this.frames.size()-1){
                currentFrameIndex--;
            }
            this.frames.remove(index);
            this.selectFramePanel.remove(index);
        } else {
            actionClearChange();
        }
        refresh();
    }

    public ImagePanel getImagePanel() {
        if(imagePanel==null){
            this.imagePanel = new ImagePanel(this);
            this.imagePanel.setIgnoreRepaint(true);
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
            } else {
                toolSelected.getButton().setSelected(true);
                toolSelected.getButton().updateUI();
            }
        }
    }

    private void initPanels() {
        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(2, 2, 2 ,2);

        mainPanel.add(getAnimationPanel(), LayoutUtils.xywi(1, 1, 3, 1.0d, 0.0d, insets));
        mainPanel.add(getToolsPanel(), LayoutUtils.xyi(1, 2, 0.0d, 0.0d, insets));
        mainPanel.add(getImagePanel(), LayoutUtils.xyi(2, 2, 1.0d, 1.0d, new Insets(5,5,5,5)));
        mainPanel.add(getColorPanel(), LayoutUtils.xyi(3, 2, 0.0d, 0.0d, insets));
        mainPanel.add(getTolerancePanel(), LayoutUtils.xyi(2, 3, 0.0d, 0.0d, insets));

        setContentPane(mainPanel);
    }

    private void restorePreviousState(){
        if(currentStateIndex>0){
            currentStateIndex--;
            this.frames.remove(currentFrameIndex);
            State state = savedStates.get(currentStateIndex);
            this.frames.add(currentFrameIndex, state.getFrame());
            this.selectionMask = state.getSelectionMask();
            repaint();

        }
    }

    private void restoreNextState(){
        if(currentStateIndex <savedStates.size()-1){
            currentStateIndex++;
            this.frames.remove(currentFrameIndex);
            State state = savedStates.get(currentStateIndex);
            this.frames.add(currentFrameIndex, state.getFrame());
            this.selectionMask = state.getSelectionMask();
            repaint();
        }
    }

    private void saveBeforeModification(){
        while(savedStates.size()> currentStateIndex){
            savedStates.remove(savedStates.size()-1);
        }
        savedStates.add(new State(getCurrentFrame().clone(), Utilities.copyArray(selectionMask)));
        this.currentStateIndex = savedStates.size();
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

            JMenuItem saveMenuAsGif = new JMenuItem("Save as GIF...");
            saveMenuAsGif.setActionCommand(ACTION_SAVE_AS_GIF);
            saveMenuAsGif.addActionListener(actionListener);
            menuOpen.add(saveMenuAsGif);

            this.appMenuBar.add(menuOpen);

            JMenu menuEdit = new JMenu("Edit");
            JMenuItem clearChangesMenu = new JMenuItem("Clear changes");
            clearChangesMenu.setActionCommand(ACTION_CLEAR);
            clearChangesMenu.addActionListener(actionListener);
            menuEdit.add(clearChangesMenu);
            JMenuItem clearSelectionMenu = new JMenuItem("Clear selection");
            clearSelectionMenu.setActionCommand(ACTION_CLEAR_SELECTION);
            clearSelectionMenu.addActionListener(actionListener);
            menuEdit.add(clearSelectionMenu);
            appMenuBar.add(menuEdit);
        }

        return appMenuBar;
    }


//	public int getImageWidth() {
//		return (PIXEL_SIZE + MARGIN) * Frame.NB_PIXELS + MARGIN;
//	}
//
//	public int getImageHeight() {
//		return (PIXEL_SIZE + MARGIN) * Frame.NB_PIXELS + MARGIN;
//	}



    private class InternalActionListener implements ActionListener {
		private Pixelizer parent;

		InternalActionListener(Pixelizer parent) {
			this.parent = parent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals(ACTION_OPEN)) {
				this.parent.openFile();
			} else if (e.getActionCommand().equals(ACTION_SAVE_AS_PNG)) {
                this.parent.actionSaveAsPNG();
            } else if (e.getActionCommand().equals(ACTION_SAVE_AS_GIF)) {
                this.parent.actionSaveAsAnimatedGIF();
			} else if (e.getActionCommand().equals(ACTION_CLEAR)) {
				this.parent.actionClearChange();
			} else if(e.getActionCommand().equals(ACTION_CLEAR_SELECTION)){
                this.parent.clearSelection();
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

	public void doFirstActionTool(int i, int j, boolean shiftPressed, boolean ctrlPressed, boolean altPressed) {
        requestFocus();
        if (Frame.NB_PIXELS > i && Frame.NB_PIXELS > j) {
            switch(toolSelected){
                case PEN:
                    clearSelection();
                    applyColor(i, j, this.colorChooser.getColor());
                    break;
                case CLEAR:
                    clearSelection();
                    applyColor(i, j, Frame.NO_COLOR_AS_INT);
                    break;
                case FILL:
                    clearSelection();
                    fillColor(i, j, this.colorChooser.getColor());
                    break;
                case MAGIC_WAND:
                    changeSelectionNearPoint(i, j, true);
                    break;
                case MOVE:
                    break;
                case SELECT:
                    this.selectionMask[i][j] = true;
                    repaint();
                    break;
                default:
                    break;
            }
        }
    }

    private void changeSelectionNearPoint(int x, int y, boolean select) {
        List<Point> points = getCurrentFrame().findPoint(x, y, getToleranceBar().getValue());
        for(Point p : points){
            selectionMask[p.x][p.y] = select;
        }
        repaint();
    }


    public void doSecondActionTool(int x, int y, boolean shiftPressed, boolean ctrlPressed, boolean altPressed) {
        requestFocus();
        switch(toolSelected){
            case PEN:
            case FILL:
            case CLEAR:
                updateColorChooser(x, y);
                break;
            case MAGIC_WAND:
                changeSelectionNearPoint(x, y, true);
                break;
            case MOVE:
                break;
            case SELECT:
                this.selectionMask[x][y] = false;
                repaint();
                break;
            default:
                break;
        }
    }

    public void doThirdActionTool(int x, int y, boolean shiftPressed, boolean ctrlPressed, boolean altPressed) {
        requestFocus();
        switch(toolSelected){
            case PEN:
                applyColor(x, y, Frame.NO_COLOR_AS_INT);
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
			int nbImgPixelPerHPixel = image.getWidth() / Frame.NB_PIXELS;
			int nbImgPixelPerVPixel = image.getHeight() / Frame.NB_PIXELS;
			for (int h = 0; h < Frame.NB_PIXELS; h++) {
				for (int v = 0; v < Frame.NB_PIXELS; v++) {
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
        clearSelection();
        getCurrentFrame().reset();
    }

    private void setCurrentPixel(int i, int j, int color){
        getCurrentFrame().setColor(i, j, color);
    }

	public void actionClearChange() {
        savedStates.clear();
        currentStateIndex=0;
		reset();
		this.imagePanel.repaint();
	}

	public static void main(String[] args) {
		new Pixelizer();
	}

	public void openFile() {
		JFileChooser fc = new JFileChooser();
		int ret = fc.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage image = ImageIO.read(fc.getSelectedFile());
                if(image.getHeight()>Frame.NB_PIXELS || image.getWidth()%Frame.NB_PIXELS!=0 || image.getHeight()!=Frame.NB_PIXELS){
                    int retOption = JOptionPane.showConfirmDialog(this, "Image seems not a pixel image (height > " + Frame.NB_PIXELS+ "px or length not a multiple of "+Frame.NB_PIXELS+"). Would you like to convert it to pixel?");
                    if(retOption == JOptionPane.OK_OPTION){
                        convertToPixelImage(image);
                    }
                } else {
                    openImage(image);
                }
                savedStates.clear();
                currentStateIndex = 0;
                clearSelection();
                refresh();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
		}
	}

    private void openImage(BufferedImage image) {
        int nb = image.getWidth() / Frame.NB_PIXELS;
        if(image.getWidth()%Frame.NB_PIXELS!=0){
            throw new IllegalArgumentException("image width is not a multiple of "+Frame.NB_PIXELS);
        }

        this.frames = new ArrayList<>(nb);
        this.currentFrameIndex = 0;
        for(int f = 0; f < nb; f++){
            Frame frame = new Frame();
            for (int x = 0; x < Frame.NB_PIXELS; x++) {
                for (int y = 0; y < Frame.NB_PIXELS; y++) {
                    int ix = x + f * Frame.NB_PIXELS;
                    int iy = y;
                    int c = image.getRGB(ix, iy);
                    frame.setColor(x, y, c);
                }
            }
            this.frames.add(frame);
        }
        fillPanelWithButton(nb);
    }

    public void actionSaveAsPNG(){
        saveImage("PNG file", "png", new PngEncoder());
    }

    public void actionSaveAsAnimatedGIF(){
        saveImage("GIF file", "gif", new AnimatedGifEncoder());
    }

    private void saveImage(String description, String extension, Encoder encoder){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(description, extension));
        if(fileChooser.showDialog(this, "Save")==JFileChooser.APPROVE_OPTION){
            File f = fileChooser.getSelectedFile();
            try {
                encoder.saveImage(f, frames);
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
            System.out.println(e);
            if(e.getKeyCode()>=KeyEvent.VK_1 && e.getKeyCode()<=KeyEvent.VK_9 && (e.getModifiers() & KeyEvent.ALT_MASK) != 0){
                int frame = e.getKeyCode()-KeyEvent.VK_1;
                this.parent.selectFrame(frame);
            } else if(e.getKeyCode()==KeyEvent.VK_Z && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0){
                this.parent.undo();
            } else if(e.getKeyCode()==KeyEvent.VK_Y && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0){
                this.parent.redo();
            } else if(e.getKeyCode()==KeyEvent.VK_DELETE){
                this.parent.deleteSelection();
            } else if(e.getKeyCode()==KeyEvent.VK_A && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0){
                this.parent.selectAll();
            } else {
                for(Tool t : Tool.values()){
                    if(t.getKeyStroke()==e.getKeyCode()){
                        this.parent.selectTool(t);
                    }
                }
            }
        }
    }

    private void redo() {
        restoreNextState();
    }

    private void undo() {
        restorePreviousState();
    }

    private void selectTool(Tool t) {
        this.toolSelected.getButton().setSelected(false);
        this.toolSelected = t;
        t.getButton().setSelected(true);
        refresh();
    }

}
