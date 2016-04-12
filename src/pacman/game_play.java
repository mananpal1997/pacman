package pacman;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class game_play extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public game_play() {
        
        initUI();
    }
    
    private void initUI() {
        
        add(new board());
        setTitle("PACMAN");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(395, 435);
        setLocationRelativeTo(null);
        setVisible(true);
        JOptionPane.showMessageDialog(rootPane, "INSTRUCTIONS :\n1. Press 'ESC' to reset the game.\n2. Press 'P' to pause(or resume).\n\nNOTE=> For clearing every level, you will\nget a bonus of 50 points.\n\n                ENJOY PLAYING");
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                game_play ex = new game_play();
                ex.setVisible(true);
            }
        });
    }
}