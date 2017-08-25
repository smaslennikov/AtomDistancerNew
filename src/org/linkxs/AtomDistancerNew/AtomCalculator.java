package org.linkxs.AtomDistancerNew;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class AtomCalculator {
	public Interface			swinger;

	private File				pdbFile, lolFile, uplFile, outFile;

	private Scanner				pdbStream, lolStream, uplStream;
	private PrintWriter			outStream;

	private String[][][]		atoms;
	private String[][]			lowerLims;
	private String[][]			upperLims;
	private ArrayList<String>	output;

	private int					pdbModels;

	public static void main(String[] args) {
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--pdb")) {
					pdbFile = args[i+1];
					i++;
				}
			}
		}
		new AtomCalculator();
	}

	public AtomCalculator() {
		output = new ArrayList<String>();

		swinger = new Interface(this);
		swinger.setVisible(true);
	}

	public void reStream() {
		try {
			this.pdbStream = new Scanner(new FileInputStream(pdbFile));
			this.lolStream = new Scanner(new FileInputStream(lolFile));
			this.uplStream = new Scanner(new FileInputStream(uplFile));
			this.outStream = new PrintWriter(outFile);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,
					"Error occured opening files. Try choosing them again.");
			swinger.setVisible(true);
			e.printStackTrace();
		}
	}

	public void runCalc(File pdbFile, File lolFile, File uplFile, File outFile) {
		this.pdbFile = pdbFile;
		this.lolFile = lolFile;
		this.uplFile = uplFile;
		this.outFile = outFile;

		this.reStream();
		this.calcSizes();
	}

	public void calcSizes() {
		String inp;
		int pdbAtoms = 0;
		int pdbAtomsFin = 0;

		int lowerLims = 0;
		int upperLims = 0;

		pdbModels = 0;

		while (pdbStream.hasNext()) {
			inp = pdbStream.next();
			if (inp.equals("MODEL")) {
				pdbModels++;
			} else if (inp.equals("ATOM")) {
				pdbAtoms++;
			} else if (inp.equals("TER")) {
				pdbAtomsFin = pdbAtoms;
				pdbAtoms = 0;
			}
			inp = pdbStream.nextLine();
		}

		System.out.println(">   " + pdbFile.toString() + " has " + pdbModels
				+ " models and " + pdbAtomsFin + " atoms per model listed.");

		while (lolStream.hasNextLine()) {
			if (!lolStream.nextLine().substring(0, 1).equals("#")) {
				lowerLims++;
			}
		}

		System.out.println(">   " + lolFile.toString() + " has " + lowerLims
				+ " lower limit distances for atoms. ");

		while (uplStream.hasNext()) {
			if (!uplStream.nextLine().substring(0, 1).equals("#")) {
				upperLims++;
			}
		}

		System.out.println(">   " + uplFile.toString() + " has " + upperLims
				+ " upper limit distances for atoms. ");

		this.atoms = new String[pdbModels][pdbAtomsFin][6];
		this.lowerLims = new String[lowerLims][7];
		this.upperLims = new String[upperLims][7];

		this.reStream();
		this.fillArrays();
	}

	public void fillArrays() {
		String inp;
		int currentModel = 0;
		int i = 0;

		while (this.pdbStream.hasNext()) {
			inp = this.pdbStream.next();
			if (inp.equals("MODEL")) {
				currentModel = this.pdbStream.nextInt() - 1;
				i = 0;
			} else if (inp.equals("ATOM")) {
				this.atoms[currentModel][i][2] = this.pdbStream.next();
				this.atoms[currentModel][i][2] = this.pdbStream.next();
				if (this.atoms[currentModel][i][2].equals("H"))
					this.atoms[currentModel][i][2] = "HN";
				this.atoms[currentModel][i][1] = this.pdbStream.next();
				this.atoms[currentModel][i][0] = this.pdbStream.next();
				this.atoms[currentModel][i][3] = this.pdbStream.next();
				this.atoms[currentModel][i][4] = this.pdbStream.next();
				this.atoms[currentModel][i][5] = this.pdbStream.next();
				i++;
			}

			inp = this.pdbStream.nextLine();
		}

		i = 0;
		while (this.lolStream.hasNext()) {
			inp = this.lolStream.next();

			if (!inp.substring(0, 1).equals("#")) {
				this.lowerLims[i][0] = inp;
				this.lowerLims[i][1] = this.lolStream.next();
				this.lowerLims[i][2] = this.lolStream.next();
				this.lowerLims[i][3] = this.lolStream.next();
				this.lowerLims[i][4] = this.lolStream.next();
				this.lowerLims[i][5] = this.lolStream.next();
				this.lowerLims[i][6] = this.lolStream.next();
				i++;
			}
			inp = this.lolStream.nextLine();
		}

		i = 0;
		while (this.uplStream.hasNext()) {
			inp = this.uplStream.next();

			if (!inp.substring(0, 1).equals("#")) {
				this.upperLims[i][0] = inp;
				this.upperLims[i][1] = this.uplStream.next();
				this.upperLims[i][2] = this.uplStream.next();
				this.upperLims[i][3] = this.uplStream.next();
				this.upperLims[i][4] = this.uplStream.next();
				this.upperLims[i][5] = this.uplStream.next();
				this.upperLims[i][6] = this.uplStream.next();
				i++;
			}
			inp = this.uplStream.nextLine();
		}

		this.reStream();
		this.runLims();
	}

	public double calcDist(int i, int res1Num, String res1, String atom1,
			int res2Num, String res2, String atom2) {
		double x1 = 0;
		double y1 = 0;
		double z1 = 0;
		double x2 = 0;
		double y2 = 0;
		double z2 = 0;
		double dist = 0.0;

		for (int j = 0; j < atoms[0].length; j++) { // running through the list
			// of atoms
			if (atoms[i][j][0] != null) {
				if (res1Num == Integer.parseInt(atoms[i][j][0])) {
					if (!atoms[i][j][1].equals(res1))
						System.out
								.println("!!! Residue number doesn't match "
										+ "the name in the pdb file. Will attempt to continue. ");
					else { // if match,
						if (atoms[i][j][2].equals(atom1)) {
							x1 = Double.parseDouble(atoms[i][j][3]);
							y1 = Double.parseDouble(atoms[i][j][4]);
							z1 = Double.parseDouble(atoms[i][j][5]);
							// System.out.println(x1 + " " + y1 + " " + z1);
						}
					}
				} else if (res2Num == Integer.parseInt(atoms[i][j][0])) {
					if (!atoms[i][j][1].equals(res2))
						System.out
								.println("!!! Residue number doesn't match "
										+ "the name in the pdb file. Will attempt to continue. ");
					else {
						if (atoms[i][j][2].equals(atom2)) {
							x2 = Double.parseDouble(atoms[i][j][3]);
							y2 = Double.parseDouble(atoms[i][j][4]);
							z2 = Double.parseDouble(atoms[i][j][5]);
						}
					}
				}
			}
		}
		if ((x1 == 0 && y1 == 0 && z1 == 0) || (x2 == 0 && y2 == 0 && z2 == 0)) {
			return 0;
		}
		dist = (Math.sqrt(Math.abs(Math.pow((x1 - x2), 2)
				+ Math.pow((y1 - y2), 2) + Math.pow((z1 - z2), 2))));

		return dist;
	}

	public void runLims() {
		double[] distances = new double[pdbModels + 1];
		String inp, res1 = null, res2 = null, atom1 = null, atom2 = null, lolimd = null, uplimd = null;
		int res1num = 0, res2num = 0;
		double totalDist = 0, avg = 0, stDevTemp = 0, stDev = 0;
		boolean found = false;

		while (lolStream.hasNext()) {
			inp = lolStream.next();

			if (!inp.substring(0, 1).equals("#")) {
				res1num = Integer.parseInt(inp);
				res1 = this.lolStream.next();
				atom1 = this.lolStream.next();
				res2num = this.lolStream.nextInt();
				res2 = this.lolStream.next();
				atom2 = this.lolStream.next();
				lolimd = this.lolStream.next();
				for (int j = 0; j < pdbModels; j++) {
					distances[j] = calcDist(j, res1num, res1, atom1, res2num,
							res2, atom2);
					totalDist += distances[j];
				}
				avg = totalDist / pdbModels;
				for (int j = 0; j < pdbModels; j++) {
					stDevTemp += Math.pow((distances[j] - avg), 2);
				}
				stDev = (Math.sqrt((stDevTemp) / (pdbModels - 1)));
				search: for (int j = 0; j < upperLims.length; j++) {
					if (Integer.parseInt(upperLims[j][0]) == res1num) {
						if (Integer.parseInt(upperLims[j][3]) == res2num) {
							if (upperLims[j][2].equals(atom1)
									&& upperLims[j][5].equals(atom2)) {
								this.addOutput(res1num, res1, atom1, res2num,
										res2, atom2, lolimd, upperLims[j][6],
										avg, stDev);
								found = true;
								break search;
							}
						}
					}
				}
			}
			if (!found)
				this.addOutput(res1num, res1, atom1, res2num, res2, atom2,
						lolimd, "N/A", avg, stDev);
			inp = lolStream.nextLine();
			found = false;
			stDev = 0.0;
			stDevTemp = 0.0;
			avg = 0.0;
			totalDist = 0.0;
		}

		found = false;
		stDev = 0.0;
		stDevTemp = 0.0;
		avg = 0.0;
		totalDist = 0.0;

		while (this.uplStream.hasNext()) {
			inp = this.uplStream.next();
			if (!inp.substring(0, 1).equals("#")) {
				res1num = Integer.parseInt(inp);
				res1 = this.uplStream.next();
				atom1 = this.uplStream.next();
				res2num = this.uplStream.nextInt();
				res2 = this.uplStream.next();
				atom2 = this.uplStream.next();
				uplimd = this.uplStream.next();
				for (int j = 0; j < pdbModels; j++) {
					distances[j] = calcDist(j, res1num, res1, atom1, res2num,
							res2, atom2);
					totalDist += distances[j];
				}
				avg = totalDist / pdbModels;
				for (int j = 0; j < pdbModels; j++) {
					stDevTemp += Math.pow((distances[j] - avg), 2);
				}
				stDev = (Math.sqrt((stDevTemp) / (pdbModels + 1)));
				search2: for (int j = 0; j < lowerLims.length; j++) {
					if (Integer.parseInt(lowerLims[j][0]) == res1num) {
						if (Integer.parseInt(lowerLims[j][3]) == res2num) {
							if (lowerLims[j][2].equals(atom1)
									&& lowerLims[j][5].equals(atom2)) {
								this.addOutput(res1num, res1, atom1, res2num,
										res2, atom2, lowerLims[j][6], uplimd,
										avg, stDev);
								found = true;
								break search2;
							}
						}
					}
				}
			}
			if (!found)
				this.addOutput(res1num, res1, atom1, res2num, res2, atom2,
						"N/A", uplimd, avg, stDev);
			inp = this.uplStream.nextLine();
			found = false;
			stDev = 0.0;
			stDevTemp = 0.0;
			avg = 0.0;
			totalDist = 0.0;
			uplimd = "";
		}

		this.writeOut();

		this.pdbStream.close();
		this.lolStream.close();
		this.uplStream.close();

		System.out.println("blah");
	}

	public void addOutput(int r1n, String r1, String a1, int r2n, String r2,
			String a2, String lolim, String uplim, double avg, double stDev) {
		NumberFormat df = new DecimalFormat("#########.##");
		output.add(r1n + " " + r1 + " " + a1 + " " + r2n + " " + r2 + " " + a2
				+ " " + lolim + " " + uplim + " " + df.format(avg) + " "
				+ df.format(stDev));
	}

	public void writeOut() {
		for (int i = 0; i < output.size(); i++) {
			this.outStream.write(output.get(i) + "\n");
		}
		outStream.close();

		JOptionPane.showMessageDialog(swinger, "Wrote output to " + outFile);

		this.swinger.dispose();
	}
}
