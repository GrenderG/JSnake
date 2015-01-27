package net.moralesblog;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/* JFrame, ventana principal del juego */

public class SnakeGame extends JFrame {

	private static final long serialVersionUID = 1L;
    private static final ImageIcon icon = new ImageIcon(SnakeGame.class.getResource("/img/icon.png"));

	public SnakeGame() {
		
		/* Este layout nos permite posicionarlo en referencia a los puntos cardinales */
		this.setLayout(new BorderLayout());
		
        this.add(new ScorePanel(), BorderLayout.NORTH);
        this.add(new GamePanel(), BorderLayout.CENTER);
        this.add(new InfoPanel(), BorderLayout.SOUTH);
        
        this.setResizable(false);
        
        this.setTitle("JSnake");
        this.setIconImage(icon.getImage());
		this.pack();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
    }
    

    public static void main(String[] args) {
    	if (args[0] != null && args[0].equals("SUPERLSDMODE"))
    		GamePanel.superLSDMode = true;
    	
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {                
                JFrame ex = new SnakeGame();
                ex.setVisible(true);                
            }
        });
    }
}
