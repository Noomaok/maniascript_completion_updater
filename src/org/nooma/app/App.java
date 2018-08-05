package org.nooma.app;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class App {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Generator g = new Generator();
		
		JFrame frame = new JFrame("Generator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 75);
		frame.setLocationRelativeTo(null);
		frame.setContentPane(g);
		frame.setVisible(true);
		frame.setResizable(false);
		
		if(g.generate()) {
			frame.requestFocus();
			JOptionPane.showMessageDialog(frame, "The generation was succesfull.", "Finished", JOptionPane.INFORMATION_MESSAGE);
		} else {
			frame.requestFocus();
			JOptionPane.showMessageDialog(frame, "The generation has failed", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		System.exit(0);
	}
}
