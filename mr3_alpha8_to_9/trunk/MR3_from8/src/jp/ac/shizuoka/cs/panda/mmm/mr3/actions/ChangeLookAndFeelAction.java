/*
 * Created on 2003/09/29
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class ChangeLookAndFeelAction extends MR3AbstractAction {

	private static final String METAL = Translator.getString("Component.View.LookAndFeel.Metal.Text");
	private static final String WINDOWS = Translator.getString("Component.View.LookAndFeel.Windows.Text");
	private static final String MOTIF = Translator.getString("Component.View.LookAndFeel.Motif.Text");

	public ChangeLookAndFeelAction(MR3 mr3, String title) {
		super(mr3, title);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if (getName().equals(METAL)) {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			} else if (getName().equals(WINDOWS)) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} else if (getName().equals(MOTIF)) {	
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");			
			}
			SwingUtilities.updateComponentTreeUI(mr3.getContentPane());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
