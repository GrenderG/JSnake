package net.moralesblog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/* Panel de puntuaciones */

public class ScorePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static int SP_WIDTH = 500;
	private static int SP_HEIGHT = 30;
	
	public ScorePanel(){
		
        this.setBackground(new Color(79, 127, 63));
        this.setFocusable(false);
        this.setBorder(new LineBorder(new Color(100, 160, 80), 3));
        this.setPreferredSize(new Dimension(SP_WIDTH, SP_HEIGHT));
		
	}
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		/* Suavizado de bordes */
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        		RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Helvetica", Font.BOLD, 14));
		g2d.drawString("Score: " + GamePanel.getScore(), 40, 20);
		g2d.drawString("Time: " + (int) GamePanel.getTime()+" s", 400, 20);
		
		pauseMsg(g2d);
		repaint();
		
	}
	
	/* Si el juego está pausado, muestra este mensaje, hay dos para crear
	 * un efecto de sombreado */
	
    private void pauseMsg (Graphics2D g2d){
    	
    	if(GamePanel.isPaused()){
    		g2d.setColor(Color.BLACK);
    		g2d.setFont(new Font("Helvetica", Font.BOLD, 16));
    		g2d.drawString("P A U S E", 210, 20);
    		g2d.setColor(Color.RED);
    		g2d.setFont(new Font("Helvetica", Font.BOLD, 16));
    		g2d.drawString("P A U S E", 210 - 1, 20 - 1);
    	}else{
    		g2d.setColor(new Color(244, 240, 139));
    		g2d.setFont(new Font("Helvetica", Font.BOLD, 14));
    		g2d.drawString("PRESS ESCAPE TO PAUSE", 160, 20);
    	}

		
    }
	
}
