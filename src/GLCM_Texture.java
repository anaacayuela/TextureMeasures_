//=====================================================
//Name:           GLCM_Texture
//Project:         Gray Level Correlation Matrix Texture Analyzer
//Version:         0.4
//
//Author:           Julio E. Cabrera
//Date:             06/10/05
//Comment:       Calculates texture features based in Gray Level Correlation Matrices
//	   Changes since 0.1 version: The normalization constant (R in Haralick's paper, pixelcounter here)
//	   now takes in account the fact that for each pair of pixel you take in account there are two entries to the 
//	   grey level co-ocurrence matrix
//	   Changes were made also in the Correlation parameter. Now this parameter is calculated according to Walker's paper

//=====================================================
// Changes by COSS
// Based on Haralick 1973

//===========imports===================================
import ij.*;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.*;
import ij.plugin.PlugIn;
import ij.text.*;
import ij.measure.ResultsTable;

//===========source====================================
public class GLCM_Texture {
	static boolean doIcalculateASM = true;
	static boolean doIcalculateContrast = true;
	static boolean doIcalculateCorrelation = true;
	static boolean doIcalculateIDM = true;
	static boolean doIcalculateEntropy = true;

	public void run(byte []pixels, int width, int height, String selectedStep,
			int step, double []f) {

		// The variable a holds the value of the pixel where the Image Processor
		// is sitting its attention
		// The variable b holds the value of the pixel which is the neighbor to
		// the pixel where the Image Processor is sitting its attention
		int a;
		int b;
		double pixelCounter = 0;

		// ====================================================================================================
		// This part computes the Gray Level Correlation Matrix based in the
		// step selected by the user

		int offset, i;
		double[][] glcm = new double[257][257];

		if (selectedStep.equals("0 degrees")) {
			for (int y = 0; y <height; y++) {
				offset = y * width;
				for (int x = 0; x < width-step; x++) {
					i = offset + x;

					a = 0xff & pixels[i];
					b = 0xff & pixels[(x + step)+y*width];
					glcm[a][b] += 1;
					glcm[b][a] += 1;
					pixelCounter += 2;
				}
			}
		}

		if (selectedStep.equals("90 degrees")) {
			for (int y = step; y <height; y++) {
				offset = y * width;
				for (int x = 0; x < width; x++) {
					i = offset + x;

					a = 0xff & pixels[i];
					b = 0xff & pixels[x+width*(y - step)];
					glcm[a][b] += 1;
					glcm[b][a] += 1;
					pixelCounter += 2;
				}
			}
		}

		if (selectedStep.equals("180 degrees")) {
			for (int y = 0; y <height; y++) {
				offset = y * width;
				for (int x = step; x < width; x++) {
					i = offset + x;

					a = 0xff & pixels[i];
					b = 0xff & pixels[(x - step)+y*width];
					glcm[a][b] += 1;
					glcm[b][a] += 1;
					pixelCounter += 2;
				}
			}
		}

		if (selectedStep.equals("270 degrees")) {
			for (int y = 0; y <height-step; y++) {
				offset = y * width;
				for (int x = 0; x < width; x++) {
					i = offset + x;

					a = 0xff & pixels[i];
					b = 0xff & pixels[x+(y + step)*width];
					glcm[a][b] += 1;
					glcm[b][a] += 1;
					pixelCounter += 2;
				}
			}
		}
		// =====================================================================================================
		// This part divides each member of the glcm matrix by the number of
		// pixels. The number of pixels was stored in the pixelCounter variable
		// The number of pixels is used as a normalizing constant
		for (a = 0; a < 257; a++) {
			for (b = 0; b < 257; b++) {
				glcm[a][b] = (glcm[a][b]) / (pixelCounter);
			}
		}

		// =====================================================================================================
		// This part calculates the angular second moment; the value is stored
		// in asm
		if (doIcalculateASM == true) {
			double asm = 0.0;
			for (a = 0; a < 257; a++) {
				for (b = 0; b < 257; b++) {
					asm = asm + (glcm[a][b] * glcm[a][b]);
				}
			}
			f[0]=asm;
		}

		// =====================================================================================================
		// This part calculates the contrast; the value is stored in contrast
		if (doIcalculateContrast == true) {
			double contrast = 0.0;
			for (a = 0; a < 257; a++) {
				for (b = 0; b < 257; b++) {
					contrast = contrast + (a - b) * (a - b) * (glcm[a][b]);
				}
			}
			f[1]=contrast;
		}

		// =====================================================================================================
		// This part calculates the correlation; the value is stored in
		// correlation
		// px [] and py [] are arrays to calculate the correlation
		// meanx and meany are variables to calculate the correlation
		// stdevx and stdevy are variables to calculate the correlation
		if (doIcalculateCorrelation == true) {
			// First step in the calculations will be to calculate px [] and py
			// []
			double correlation = 0.0;
			double px = 0;
			double py = 0;
			double meanx = 0.0;
			double meany = 0.0;
			double stdevx = 0.0;
			double stdevy = 0.0;

			for (a = 0; a < 257; a++) {
				for (b = 0; b < 257; b++) {
					px = px + a * glcm[a][b];
					py = py + b * glcm[a][b];
				}
			}

			// Now calculate the standard deviations
			for (a = 0; a < 257; a++) {
				for (b = 0; b < 257; b++) {
					stdevx = stdevx + (a - px) * (a - px) * glcm[a][b];
					stdevy = stdevy + (b - py) * (b - py) * glcm[a][b];
				}
			}

			// Now finally calculate the correlation parameter
			for (a = 0; a < 257; a++) {
				for (b = 0; b < 257; b++) {
					correlation = correlation
							+ ((a - px) * (b - py) * glcm[a][b] / (stdevx * stdevy));
				}
			}
			f[2]=correlation;
		}
		// ===============================================================================================
		// This part calculates the inverse difference moment
		if (doIcalculateIDM == true) {
			double IDM = 0.0;
			for (a = 0; a < 257; a++) {
				for (b = 0; b < 257; b++) {
					IDM = IDM + (glcm[a][b] / (1 + (a - b) * (a - b)));
				}
			}
			f[3]=IDM;
		}

		// ===============================================================================================
		// This part calculates the entropy
		if (doIcalculateEntropy == true) {
			double entropy = 0.0;
			for (a = 0; a < 257; a++) {
				for (b = 0; b < 257; b++) {
					if (glcm[a][b] == 0) {
					} else {
						entropy = entropy
								- (glcm[a][b] * (Math.log(glcm[a][b])));
					}
				}
			}
			f[4]=entropy;
		}
	}
}
