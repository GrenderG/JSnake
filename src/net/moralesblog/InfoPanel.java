package net.moralesblog;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

/* Esta clase es un panel en el cual se muestra información en
 * tiempo real relativa a los eventos sucedidos dentro del juego */

public class InfoPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static int IP_WIDTH = 500;
	private static int IP_HEIGHT = 30;
	
	private static String msg = "";
	
	/* Mensajes predeterminados */
	
	public static final String MISSED_GREATER_FRUIT = "GREATER FRUIT MISSED";
	public static final String NORMAL_FRUIT_MSG = "+ 1 LENGTH / POINT";
	public static final String GREATER_FRUIT_MSG = "+ 3 LENGTH / POINTS";
	public static final String PAUSE_INFO = "PRESS Q TO EXIT GAME";
	public static final String MISSED_BAD_FRUIT = "+ 2 LENGTH / POINTS";
	public static final String BAD_FRUIT_MSG = "- 2 LENGTH / POINTS";
	
	private static Timer msgTimer;
	/* Variable utilizada para desvanecer el texto */
	private static double fontAlphaCount = 10.0;
	private static boolean stateChanged = false;
	
	public InfoPanel (){
		
        this.setBackground(new Color(79, 127, 63));
        this.setFocusable(false);
        this.setBorder(new LineBorder(new Color(100, 160, 80), 3));
        this.setPreferredSize(new Dimension(IP_WIDTH, IP_HEIGHT));
        
        /* El panel se actualiza cada 150 ms */
        
        msgTimer = new Timer (150, this);

	}
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		/* Suavizado de bordes */
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        		RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setColor(new Color(244, 240, 139));
		
		/* Solo se procede a actualizar en caso de que un nuevo mensaje haya sido
		 * mandado a mostrar, para que no se repita el ciclo de desvanecimiento */
		
		if (stateChanged){
			/* Mediante la siguiente linea se consigue aplicar el valor del
			 * canal alfa */
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
			        (float)fontAlphaCount * 0.1f));
			Font infoPanelFont = new Font("Helvetica", Font.BOLD, 20);
			g2d.setFont(infoPanelFont);
			FontMetrics metrB = getFontMetrics(infoPanelFont);
	        g2d.drawString(msg, (IP_WIDTH - metrB.stringWidth(msg)) / 2, 22);
		}
			
		repaint();
		
	}
	
	/* Método que aplica un mensaje a ser mostrado */
	
	public static void setMSG(String message){
		msg = message;
		stateChanged = true;
		fontAlphaCount = 10.0;
		msgTimer.start();
	}
	
	/* Método que llama el timer cada 150 ms */
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		if (!GamePanel.isPaused()){
			if (msg.equals(PAUSE_INFO))
				msg = "";
			/* Valor alpha a restar cada 150 ms */
			fontAlphaCount -= 0.6;
			/* En caso de haberse desvanecido se vuelve al valor 
			 * alfa por defecto preparado para el siguiente mensaje
			 * a mostrar */
			if (fontAlphaCount <= 0){
				stateChanged = false;
				msgTimer.stop();
				fontAlphaCount = 10.0;
			}

		}else if (GamePanel.isPaused()){
			fontAlphaCount = 10.0;
		}
		
	}
	
}
