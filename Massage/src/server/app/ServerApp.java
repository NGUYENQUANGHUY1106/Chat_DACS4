package server.app;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import server.view.ServerAdminGUI;

public class ServerApp {
	public static final int TYPE_AVATAR_UPDATED = 120;

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			ServerAdminGUI frame = new ServerAdminGUI();
			frame.setVisible(true);
		});
	}
}
