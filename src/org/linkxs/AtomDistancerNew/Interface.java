package org.linkxs.AtomDistancerNew;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Interface extends JFrame implements ActionListener {
	private static final long		serialVersionUID	= 2559076138785162703L;
	public static final Dimension	INITIAL_SCREEN_SIZE	= Toolkit
																.getDefaultToolkit()
																.getScreenSize();	;
	private int						WIDTH				= 400;
	private int						HEIGHT				= 180;
	
	private AtomCalculator			ac;
	
	private JButton					setPdb;
	private JButton					setLol;
	private JButton					setUpl;
	private JButton					setOut;
	private JButton					compute;
	
	private JLabel					pdbFileLabel;
	private JLabel					lolFileLabel;
	private JLabel					uplFileLabel;
	private JLabel					outFileLabel;
	
	private File					pdbFile;
	private File					lolFile;
	private File					uplFile;
	private File					outFile;
	
	public Interface(AtomCalculator ac) {
		super("Atom Distancer");
		super.setBounds((int) INITIAL_SCREEN_SIZE.getWidth() / 2,
				(int) INITIAL_SCREEN_SIZE.getHeight() / 2, this.WIDTH,
				this.HEIGHT);
		super.setResizable(false);
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		super.setLayout(null);
		
		this.ac = ac;
		
		this.setPdb = new JButton("Set .pdb file");
		this.setLol = new JButton("Set .lol file");
		this.setUpl = new JButton("Set .upl file");
		this.setOut = new JButton("Set output file");
		this.compute = new JButton("Compute!");
		
		this.setPdb.setBounds(240, 5, 150, 20);
		this.setLol.setBounds(240, 30, 150, 20);
		this.setUpl.setBounds(240, 55, 150, 20);
		this.setOut.setBounds(240, 80, 150, 20);
		this.compute.setBounds(this.WIDTH / 3, 115, 140, 30);
		
		this.setPdb.addActionListener(this);
		this.setLol.addActionListener(this);
		this.setUpl.addActionListener(this);
		this.setOut.addActionListener(this);
		this.compute.addActionListener(this);
		
		this.pdbFileLabel = new JLabel(".pdb file not set");
		this.lolFileLabel = new JLabel(".lol file not set");
		this.uplFileLabel = new JLabel(".upl file not set");
		this.outFileLabel = new JLabel("Output file not set");
		
		this.pdbFileLabel.setBounds(5, 5, 300, 20);
		this.lolFileLabel.setBounds(5, 30, 300, 20);
		this.uplFileLabel.setBounds(5, 55, 300, 20);
		this.outFileLabel.setBounds(5, 80, 300, 20);
		
		super.add(setPdb);
		super.add(setLol);
		super.add(setUpl);
		super.add(setOut);
		super.add(compute);
		
		super.add(pdbFileLabel);
		super.add(lolFileLabel);
		super.add(uplFileLabel);
		super.add(outFileLabel);
		
		super.setVisible(false);
	}
	
	public void saveFile() {
		boolean success = false;
		int response;
		
		JFileChooser fileChooser = new JFileChooser();
		
		while (!success) {
			fileChooser.showSaveDialog(this);
			
			if (fileChooser.getSelectedFile() == null) {
				break;
			} else if (fileChooser.getSelectedFile().exists()) {
				response = JOptionPane.showConfirmDialog(this,
						"File exists! Overwrite?");
				if (response == JOptionPane.CANCEL_OPTION) {
					break;
				} else if (response == JOptionPane.YES_OPTION) {
					success = true;
				}
			} else {
				success = true;
			}
		}
		
		if (success) {
			outFile = fileChooser.getSelectedFile();
			this.outFileLabel.setText(fileChooser.getSelectedFile().toString());
		}
	}
	
	public void openFile(String extension) {
		boolean success = false;
		JFileChooser fileChooser = new JFileChooser();
		
		while (!success) {
			fileChooser.showOpenDialog(this);
			
			if (fileChooser.getSelectedFile() == null) {
				break;
			} else if (!fileChooser.getSelectedFile().exists()) {
				JOptionPane.showMessageDialog(this,
						"File doesn't exist. Please try again");
			} else if (!this.accept(fileChooser.getSelectedFile(), extension)) {
				fileChooser.setSelectedFile(null);
				JOptionPane.showMessageDialog(this,
						"Wrong extension. Please try again");
			} else {
				success = true;
			}
		}
		
		if (success) {
			if (extension.equals("pdb")) {
				pdbFile = fileChooser.getSelectedFile();
				this.pdbFileLabel.setText(fileChooser.getSelectedFile()
						.toString());
			} else if (extension.equals("lol")) {
				lolFile = fileChooser.getSelectedFile();
				this.lolFileLabel.setText(fileChooser.getSelectedFile()
						.toString());
			} else if (extension.equals("upl")) {
				uplFile = fileChooser.getSelectedFile();
				this.uplFileLabel.setText(fileChooser.getSelectedFile()
						.toString());
			}
		}
	}
	
	public boolean accept(File f, String extension) {
		if (f.isDirectory()) {
			return true;
		}
		
		String fExtension = this.getExtension(f);
		if (fExtension != null) {
			if (fExtension.equals(extension)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(setPdb)) {
			this.openFile("pdb");
		} else if (e.getSource().equals(setLol)) {
			this.openFile("lol");
		} else if (e.getSource().equals(setUpl)) {
			this.openFile("upl");
		} else if (e.getSource().equals(setOut)) {
			this.saveFile();
		} else if (e.getSource().equals(compute)) {
			ac.runCalc(pdbFile, lolFile, uplFile, outFile);
			super.setVisible(false);
		}
	}
	
	public void dispose() {
		System.exit(0);
	}
}
