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

public class Pixelizer extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final int PIXEL_SIZE = 20;
	private static final int MARGIN = 1;

	private int nbPixelsHorizontal = 20;
	private int nbPixelsVertical = 20;
	private ImagePanel imagePanel;
	private JMenuBar menuBar;
	private boolean pixelView = true;
	private int[][] imagePixelsRgba;
	private Integer[][] pixelsRgba;
	private Integer[][] alphaMaskRgba;
	private boolean showImage = true;
	private boolean showChange = true;
	private ColorChooser colorChooser;
	private boolean alphaMask = false;

	public Pixelizer() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("Image viewer");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.menuBar = new JMenuBar();

		JMenu menuOpen = new JMenu("File");
		JMenuItem openMenu = new JMenuItem("Open...");
		openMenu.setActionCommand("open");
        InternalActionListener actionListener = new InternalActionListener(this);
        openMenu.addActionListener(actionListener);
		menuOpen.add(openMenu);
		JMenuItem saveMenuAsCode = new JMenuItem("Save as code...");
		saveMenuAsCode.setActionCommand("save_as_code");
		saveMenuAsCode.addActionListener(actionListener);
		menuOpen.add(saveMenuAsCode);
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

		JMenu menuDisplay = new JMenu("View");
		JCheckBoxMenuItem showImage = new JCheckBoxMenuItem("Show image");
		showImage.setSelected(this.showImage);
		showImage.setActionCommand("showImage");
		showImage.addActionListener(actionListener);
		menuDisplay.add(showImage);
		JCheckBoxMenuItem showChange = new JCheckBoxMenuItem("Show change");
		showChange.setSelected(this.showChange);
		showChange.setActionCommand("showChange");
		showChange.addActionListener(actionListener);
		menuDisplay.add(showChange);
		JCheckBoxMenuItem selectAlpha = new JCheckBoxMenuItem("Select alpha");
		selectAlpha.setSelected(this.alphaMask);
		selectAlpha.setActionCommand("selectAlpha");
		selectAlpha.addActionListener(actionListener);
		menuDisplay.add(selectAlpha);
		menuBar.add(menuDisplay);
		setJMenuBar(menuBar);

		JPanel mainpanel = new JPanel();
		mainpanel.setLayout(new BorderLayout());
		this.imagePanel = new ImagePanel(this);
		this.imagePanel
				.addComponentListener(new InternalComponentListener(this));
		InternalMouseListener mouseListener = new InternalMouseListener(this);
		this.imagePanel.addMouseListener(mouseListener);
		this.imagePanel.addMouseMotionListener(mouseListener);
		mainpanel.add(imagePanel, BorderLayout.CENTER);
		setContentPane(mainpanel);

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

	private class InternalComponentListener implements ComponentListener {
		private Pixelizer parent;

		public InternalComponentListener(Pixelizer parent) {
			this.parent = parent;

		}

		@Override
		public void componentResized(ComponentEvent e) {
			int h = this.parent.nbPixelsHorizontal;
			int v = this.parent.nbPixelsVertical;
			this.parent.nbPixelsHorizontal = (e.getComponent().getWidth() - MARGIN)
					/ (PIXEL_SIZE + MARGIN);
			this.parent.nbPixelsVertical = (e.getComponent().getHeight() - MARGIN)
					/ (PIXEL_SIZE + MARGIN);
			if (h != this.parent.nbPixelsHorizontal
					|| v != this.parent.nbPixelsVertical) {
				pixelsRgba = new Integer[getNbH()][getNbV()];
				alphaMaskRgba = new Integer[getNbH()][getNbV()];
				convertCurentImageToPixels();
			}
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub

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
				case MouseEvent.BUTTON2:
					this.parent.changePixelView();
					break;
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
			} else if (e.getActionCommand().equals("showImage")) {
				this.parent.actionShowImage();
			} else if (e.getActionCommand().equals("showChange")) {
				this.parent.actionShowChange();
			} else if (e.getActionCommand().equals("selectAlpha")) {
				this.parent.actionShowAlpha();
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

			if (this.parent.pixelView) {
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
						int c = 0xffffffff;
						if (this.parent.showImage && imagePixelsRgba != null
								&& imagePixelsRgba[i][j] >= 0) {
							c = imagePixelsRgba[i][j];
						}
						if (this.parent.showChange && pixelsRgba != null
								&& pixelsRgba[i][j] != null) {
							c = pixelsRgba[i][j];
						}
						if (c != 0xffffffff) {
							g2d.setPaint(new Color(c));
							g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN),
									MARGIN + j * (PIXEL_SIZE + MARGIN),
									PIXEL_SIZE, PIXEL_SIZE);
						}
						if (alphaMask) {
							g2d.setPaint(Color.GRAY);
							g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN),
									MARGIN + j * (PIXEL_SIZE + MARGIN),
									PIXEL_SIZE / 2, PIXEL_SIZE / 2);
							g2d.fillRect(MARGIN + i * (PIXEL_SIZE + MARGIN)
									+ PIXEL_SIZE / 2, MARGIN + j
									* (PIXEL_SIZE + MARGIN) + PIXEL_SIZE / 2,
									PIXEL_SIZE / 2, PIXEL_SIZE / 2);
						}
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

	public void actionShowAlpha() {
		if (alphaMask) {
			alphaMask = false;
			this.alphaMaskRgba = null;
		} else {
			alphaMask = true;
			alphaMaskRgba = new Integer[getNbH()][getNbV()];
		}
		repaint();
	}

	public void actionShowImage() {
		this.showImage = !showImage;
		this.imagePanel.repaint();
	}

	public void actionShowChange() {
		this.showChange = !showChange;
		this.imagePanel.repaint();
	}

	public void updateColorChooser(int i, int j) {
		if (this.pixelsRgba[i][j] != null)
			updateColorChooser(i, j, this.pixelsRgba[i][j]);
		else if (imagePixelsRgba != null && this.imagePixelsRgba.length > i
				&& imagePixelsRgba[i].length > j)
			updateColorChooser(i, j, this.imagePixelsRgba[i][j]);
		else
			updateColorChooser(i, j, 0x00000000);
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
			imagePixelsRgba = new int[getNbH()][getNbV()];
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
					imagePixelsRgba[h][v] = 0x00000000;
					if (count != 0) {
						imagePixelsRgba[h][v] += (sumR / count << 16) & 0x00ff0000;
						imagePixelsRgba[h][v] += (sumG / count << 8) & 0x0000ff00;
						imagePixelsRgba[h][v] += (sumB / count) & 0x000000ff;
					}
				}
			}
		}
	}

	public void changePixelView() {
		changePixelView(!this.pixelView);
	}

	public void changePixelView(boolean b) {
		this.pixelView = b;
		this.imagePanel.repaint();
	}

	public void actionClearChange() {
		pixelsRgba = new Integer[getNbH()][getNbV()];
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
                        bImg.setRGB(i, j, toARGB(toInt(pixelsRgba[i][j], imagePixelsRgba[i][j])));
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
            return rgba | 0xFF000000;
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
				code.append(toHex(pixelsRgba[i][j], imagePixelsRgba[i][j]));
			}
			code.append("}");
		}
		code.append("\n};");
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = new StringSelection(code.toString());
		clipboard.setContents(transferable, null);
		JOptionPane.showMessageDialog(this, "Source code copied");
	}

	private String toHex(Integer i1, Integer i2) {
		int i = toInt(i1, i2);
		String ret = Integer.toHexString(i);
		if (ret.length() == 6) {
			ret = "0x00" + ret;
		} else {
			ret = "0x" + ret;
		}
		return ret;
	}

    private int toInt(Integer i1, Integer i2) {
        return i1 != null ? i1 : i2;
    }

	private void showImage(File selectedFile) {
		this.imagePanel.setImage(selectedFile);
	}

}
