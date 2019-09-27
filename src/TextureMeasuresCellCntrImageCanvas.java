/*
 * CellCntrImageCanvas.java
 *
 * Created on November 22, 2005, 5:58 PM
 *
 */
/*
 *
 * @author Kurt De Vos � 2005
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
 *
 */

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.plugin.filter.RGBStackSplitter;
import ij.process.ImageProcessor;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ListIterator;
import java.util.Vector;
import java.io.*;

/**
 * 
 * @author Kurt De Vos
 */
public class TextureMeasuresCellCntrImageCanvas extends ImageCanvas {
	private Vector typeVector;
	private TextureMeasuresCellCntrMarkerVector currentMarkerVector;
	private TextureMeasures cc;
	private ImagePlus img;
	private boolean delmode = false;
	private boolean showNumbers = false;
	private boolean showAll = false;
	private Font font = new Font("SansSerif", Font.PLAIN, 10);
	private int radius = 10;

	/** Creates a new instance of CellCntrImageCanvas */
	public TextureMeasuresCellCntrImageCanvas(ImagePlus img, Vector typeVector, TextureMeasures cc, Overlay overlay) {
		super(img);
		this.img = img;
		this.typeVector = typeVector;
		this.cc = cc;
		if (overlay != null)
			this.setOverlay(overlay);
	}

	public void largerRadius() {
		radius++;
	}

	public void smallerRadius() {
		if (radius > 2)
			radius--;
	}

	public void mousePressed(MouseEvent e) {
		if (IJ.spaceBarDown() || Toolbar.getToolId() == Toolbar.MAGNIFIER
				|| Toolbar.getToolId() == Toolbar.HAND) {
			super.mousePressed(e);
			return;
		}

		if (currentMarkerVector == null) {
			IJ.error("Select a counter type first!");
			return;
		}

		int x = super.offScreenX(e.getX());
		int y = super.offScreenY(e.getY());
		if (!delmode) {
			TextureMeasuresCellCntrMarker m = new TextureMeasuresCellCntrMarker(x, y, img.getCurrentSlice());
			currentMarkerVector.addMarker(m);
		} else {
			TextureMeasuresCellCntrMarker m = currentMarkerVector.getMarkerFromPosition(
					new Point(x, y), img.getCurrentSlice());
			currentMarkerVector.remove(m);
		}
		repaint();
		cc.populateTxtFields();
	}

	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
	}

	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
	}

	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
	}

	public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);
		if (!IJ.spaceBarDown() | Toolbar.getToolId() != Toolbar.MAGNIFIER
				| Toolbar.getToolId() != Toolbar.HAND)
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);
	}

	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
	}

	private Point point;
	private Rectangle srcRect = new Rectangle(0, 0, 0, 0);

	public void paint(Graphics g) {
		super.paint(g);
		srcRect = getSrcRect();
		Roi roi = img.getRoi();
		double xM = 0;
		double yM = 0;

		/*
		 * double magnification = super.getMagnification();
		 * 
		 * try { if (imageUpdated) { imageUpdated = false; img.updateImage(); }
		 * Image image = img.getImage(); if (image!=null) g.drawImage(image, 0,
		 * 0, (int)(srcRect.width*magnification),
		 * (int)(srcRect.height*magnification), srcRect.x, srcRect.y,
		 * srcRect.x+srcRect.width, srcRect.y+srcRect.height, null); if (roi !=
		 * null) roi.draw(g); } catch(OutOfMemoryError e) {
		 * IJ.outOfMemory("Paint "+e.getMessage()); }
		 */

		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(1f));
		g2.setFont(font);

		ListIterator it = typeVector.listIterator();
		while (it.hasNext()) {
			TextureMeasuresCellCntrMarkerVector mv = (TextureMeasuresCellCntrMarkerVector) it.next();
			int typeID = mv.getType();
			g2.setColor(mv.getColor());
			ListIterator mit = mv.listIterator();
			while (mit.hasNext()) {
				TextureMeasuresCellCntrMarker m = (TextureMeasuresCellCntrMarker) mit.next();
				boolean sameSlice = m.getZ() == img.getCurrentSlice();
				if (sameSlice || showAll) {
					xM = ((m.getX() - srcRect.x) * magnification);
					yM = ((m.getY() - srcRect.y) * magnification);
					g2.drawRect((int) (xM - radius * magnification),
							(int) (yM - radius * magnification),
							(int) (2 * radius * magnification),
							(int) (2 * radius * magnification));
					if (showNumbers)
						g2.drawString(Integer.toString(typeID), (int) xM + 3,
								(int) yM - 3);
				}
			}
		}
	}

	public void removeLastMarker() {
		currentMarkerVector.removeLastMarker();
		repaint();
		cc.populateTxtFields();
	}

	public void measure() {
		try {
			// Produce raw data
			IJ.setColumnHeadings("Type\t ASM0_1\t Ct0_1\t Corr0_1\t IDM0_1\t Ent0_1\t ASM0_2\t Ct0_2\t Corr0_2\t IDM0_2\t Ent0_2\t ASM90_1\t Ct90_1\t Corr90_1\t IDM90_1\t Ent90_1\t ASM90_2\t Ct90_2\t Corr90_2\t IDM90_2\t Ent90_2\t ASM180_1\t Ct180_1\t Corr180_1\t IDM180_1\t Ent180_1\t ASM180_2\t Ct180_2\t Corr180_2\t IDM180_2\t Ent180_2\t ASM270_1\t Ct270_1\t Corr270_1\t IDM270_1\t Ent270_1\t ASM270_2\t Ct270_2\t Corr270_2\t IDM270_2\t Ent270_2");
			ImageProcessor ip = img.getProcessor();
			Calibration cal = img.getCalibration();
			int W = ip.getWidth();
			int H = ip.getHeight();
			int radius2 = radius * radius;

			FileOutputStream rawData = new FileOutputStream(
					"TextureMeasures.txt", (new File("TextureMeasures.txt"))
							.exists());
			PrintStream pRawData = new PrintStream(rawData);
			ListIterator it = typeVector.listIterator();
			GLCM_Texture textureAnalyzer = new GLCM_Texture();
			while (it.hasNext()) {
				TextureMeasuresCellCntrMarkerVector mv = (TextureMeasuresCellCntrMarkerVector) it.next();
				int typeID = mv.getType();
				String typeLabel = "";
				switch (typeID) {
				case 1:
					typeLabel = "Bg ";
					break;
				case 2:
					typeLabel = "Fg ";
					break;
				}
				ListIterator mit = mv.listIterator();
				int dimRegion = 2 * radius + 1;
				while (mit.hasNext()) {
					TextureMeasuresCellCntrMarker m = (TextureMeasuresCellCntrMarker) mit.next();
					int xM = m.getX();
					int yM = m.getY();
					byte[] region = new byte[dimRegion * dimRegion];
					double[] f = new double[40];
					double[] ff = new double[5];
					int idx = 0;
					textureAnalyzer.run(region, dimRegion, dimRegion,
							"0 degrees", 1, ff);
					for (int i = 0; i < 5; i++)
						f[idx++] = ff[i];
					textureAnalyzer.run(region, dimRegion, dimRegion,
							"0 degrees", 3, ff);
					for (int i = 0; i < 5; i++)
						f[idx++] = ff[i];
					textureAnalyzer.run(region, dimRegion, dimRegion,
							"90 degrees", 1, ff);
					for (int i = 0; i < 5; i++)
						f[idx++] = ff[i];
					textureAnalyzer.run(region, dimRegion, dimRegion,
							"90 degrees", 3, ff);
					for (int i = 0; i < 5; i++)
						f[idx++] = ff[i];
					textureAnalyzer.run(region, dimRegion, dimRegion,
							"180 degrees", 1, ff);
					for (int i = 0; i < 5; i++)
						f[idx++] = ff[i];
					textureAnalyzer.run(region, dimRegion, dimRegion,
							"180 degrees", 3, ff);
					for (int i = 0; i < 5; i++)
						f[idx++] = ff[i];
					textureAnalyzer.run(region, dimRegion, dimRegion,
							"270 degrees", 1, ff);
					for (int i = 0; i < 5; i++)
						f[idx++] = ff[i];
					textureAnalyzer.run(region, dimRegion, dimRegion,
							"270 degrees", 3, ff);
					for (int i = 0; i < 5; i++)
						f[idx++] = ff[i];

					// m.setValue(value);
					String output = typeLabel;
					for (int i = 0; i < 40; i++)
						output += "\t" + f[i];
					IJ.write(output);
					pRawData.println(output);
				}
			}
			pRawData.println(" ");
			rawData.close();

		} catch (FileNotFoundException e) {
			IJ.write("Cannot open raw data file for output");
			return;
		} catch (IOException e) {
			// Do nothing
		}
	}

	public Vector getTypeVector() {
		return typeVector;
	}

	public void setTypeVector(Vector typeVector) {
		this.typeVector = typeVector;
	}

	public TextureMeasuresCellCntrMarkerVector getCurrentMarkerVector() {
		return currentMarkerVector;
	}

	public void setCurrentMarkerVector(TextureMeasuresCellCntrMarkerVector currentMarkerVector) {
		this.currentMarkerVector = currentMarkerVector;
	}

	public boolean isDelmode() {
		return delmode;
	}

	public void setDelmode(boolean delmode) {
		this.delmode = delmode;
	}

	public boolean isShowNumbers() {
		return showNumbers;
	}

	public void setShowNumbers(boolean showNumbers) {
		this.showNumbers = showNumbers;
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}

}
