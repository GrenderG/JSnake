package net.moralesblog;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.Timer;

/* Panel de juego */

public class GamePanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	/* Tamaño de la pantalla */
	private final int GP_WIDTH = 500;
    private final int GP_HEIGHT = 500;
    /* Tamaño de los sprites principales */
    private final int DOT_SIZE = 10;
    /* Puntos totales (500*500) / (10 * 10) -> DOT_SIZE */
    private final int ALL_DOTS = 2500;
    /* Posición máxima a tener en cuenta para la aleatoriedad del spawn de frutas (Si fuera 50 habría
     * una posibildiad de que salieran fuera, va de 0 a 49 */
    private final int RAND_POS = 49;
    /* Delay aplicado al timer, la pantalla se actualizará cada 80 ms */
    private final int DELAY = 80;
    
    /* Se crean los arrays que contienen las coordenadas del juego */
    private final int snake_x[] = new int[ALL_DOTS];
    private final int snake_y[] = new int[ALL_DOTS];
    
    /* Puntiación y tiempo transcurrido, se necesita acceder de forma estática desde ScorePanel */
    private static int score;
    private static double time;
    
    /* Número de partes de la serpiente */   
    private int bodyParts;
    
    /* Coordenadas donde ha spawneado la fruta */
    private int fruit_x;
    private int fruit_y;
    
    /* Booleanos que controlan qué dirección está tomando la serpiente */
    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    
    /* Controlan el estado del juego en cada momento */
    private boolean inGame = true;
    private static boolean isPaused = false;
    
    /* Ruta y clip del efecto de sonido a reproducir cuando la serpiente se come una fruta */
	private URL fruitEatenPath = GamePanel.class.getResource("/sounds/fruit_eaten.wav");
	private AudioClip fruitEaten = Applet.newAudioClip(fruitEatenPath);
    
	/* Timer que controla las veces que se actualiza la pantalla.
	 * Este objeto es muy importante. Hay que implementar la interfaz ActionListener */
    private Timer timer;
    
    /* Controla si la fruta especial que aumenta 3 el tamaño ha spawneado */
	private boolean greaterFruitSpawned;
	
	/* Constructor para GamePanel */
	
    public GamePanel() {

        this.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent e) {
	            int key = e.getKeyCode();

	            /* Si pulsamos escape el juego se pausa, si lo volvemos a pulsar se reanuda */
	            
	            if (key == KeyEvent.VK_ESCAPE && (inGame)){
	            	if (!isPaused){
	            		timer.stop();
	            	}else{
	            		timer.start();
	            	}
	            	
	            	isPaused = !isPaused;
	            	
	            }
	            
	            /* Si hemos muerto (!inGame) podremos pulsar espacio para resetear el juego */
	            
	            if (key == KeyEvent.VK_SPACE && (!inGame)){
	            	initGame();
	            }
	            
	            /* Se controla la dirección de la serpiente, si se pulsan dos teclas en menos de 80 ms
	             * puede generar problemas. */
	            
	            if ((key == KeyEvent.VK_A) && (!rightDirection)) {
	                leftDirection = true;
	                upDirection = false;
	                downDirection = false;
	            }

	            if ((key == KeyEvent.VK_D) && (!leftDirection)) {
	                rightDirection = true;
	                upDirection = false;
	                downDirection = false;
	            }

	            if ((key == KeyEvent.VK_W) && (!downDirection)) {
	                upDirection = true;
	                rightDirection = false;
	                leftDirection = false;
	            }

	            if ((key == KeyEvent.VK_S) && (!upDirection)) {
	                downDirection = true;
	                rightDirection = false;
	                leftDirection = false;
	            }
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				
			}

			@Override
			public void keyTyped(KeyEvent e) {
				
			}
        	
        });
        
        this.setBackground(new Color(202, 140, 99));
        this.setFocusable(true);
        
        this.setPreferredSize(new Dimension(GP_WIDTH, GP_HEIGHT));
        
        /* Se inicializa el juego */
        
        initGame();
    }
    
    /* Método que inicializa el juego */
    
    private void initGame() {
    	
    	/* Inicializamos las variables a los valores por defecto al principio de la partida */
    	inGame = true;
        bodyParts = 3;
        score = 0;
        time = 0.0;
        
        rightDirection = true;
        leftDirection = false;
        upDirection = false;
        downDirection = false;
        
        
        /* Este bucle "spawnea" la serpiente, empezará en [0, 0] y los siguientes en 
         * [1, 0] y [2, 0] */
        
        for (int i = 0; i < bodyParts; i++) {
            snake_x[i] = i * 10;
            snake_y[i] = 0;
        }
        
        /* Se llama al método que generará la primera fruta */        
        locateFruit();
        
        /* Indicamos el delay al timer, en nuestro caso 80ms (cada 80ms llamará al método "actionPerformed"), y se le pasa 
         * el delay y this puesto que necesitamos la interfaz implementada (ActionListener) que la tiene la clase GamePanel */
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        /* Aplicamos suavizado a los renders */
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        		RenderingHints.VALUE_ANTIALIAS_ON);
        
        /* Condición que renderiza las frutas*/
        
        if (inGame) {
        	
        	/* Crea la cuadricula en el mapa, 50 es el número de columnas / filas,
        	 * los parámetros de drawLine son el punto donde empezar y el punto
        	 * donde acabar */
        	
        	g2d.setColor(new Color(214, 150, 107));
    		g2d.drawRect(0, 0, GP_WIDTH, GP_HEIGHT);
    		for(int x = 0; x < 50; x++) {
    			for(int y = 0; y < 50; y++) {
    				g2d.drawLine(x * DOT_SIZE, 0, x * DOT_SIZE, GP_HEIGHT);
    				g2d.drawLine(0, y * DOT_SIZE, GP_WIDTH, y * DOT_SIZE);
    			}
    		}
        	
        	/* Si ha spawneado una fruta especial, se renderiza, 
        	 * está formada por varios círculos superpuestos */
        	if (greaterFruitSpawned){
        		
        		g2d.setColor(Color.BLACK);
        		g2d.fillOval(fruit_x, fruit_y, DOT_SIZE, DOT_SIZE);
            	g2d.setColor(Color.YELLOW); 
            	g2d.fillOval(fruit_x + 1, fruit_y + 1, 8, 8);
            	g2d.setColor(Color.RED); 
            	g2d.fillOval(fruit_x + 2, fruit_y + 2, 6, 6);
            	g2d.setColor(Color.CYAN); 
            	g2d.fillOval(fruit_x + 3, fruit_y + 3, 4, 4);
            
            /* Esto es la fruta normal */
        	}else{
        		
        		g2d.setColor(Color.BLACK);
        		g2d.fillOval(fruit_x, fruit_y, DOT_SIZE, DOT_SIZE);
            	g2d.setColor(Color.RED); 
            	g2d.fillOval(fruit_x + 1, fruit_y + 1, 8, 8);
            	
        	}
        	
        	/* En este bucle se renderiza la serpiente*/       	
            for (int i = 0; i < bodyParts; i++) {
            	
            	/* Color del borde de la serpiente */

				g2d.setColor(new Color(79, 127, 108));
				g2d.fillRect(snake_x[i], snake_y[i], DOT_SIZE,
						DOT_SIZE);

				/* Cabeza de la serpiente */
				if (i == 0) {
					g2d.setColor(new Color(79, 127, 108));
				/* Resto del cuerpo */
				} else {
					g2d.setColor(new Color(99, 160, 135));
				}
				
				g2d.fillRect(snake_x[i] + 1, snake_y[i] + 1, 8, 8);

			}
        
        /* Si no se encuentra inGame se procede a terminar la partida actual */    
        } else {

            gameOver(g2d);
        }  
    }
    
    /* Método que termina la partida */    
    
    private void gameOver(Graphics2D g2d) {
        
        String msg = "Game Over";
        String subMsg = "Press SPACE to continue";
        Font big = new Font("Helvetica", Font.BOLD, 14);
        Font small = new Font("Helvetica", Font.BOLD, 12);
        /* Se obtienen las medidas de las fuentes, para poder centrarlas correctamente después */
        FontMetrics metrB = getFontMetrics(big);
        FontMetrics metrS = getFontMetrics(small);

        g2d.setColor(Color.white);
        /* Se centran en la pantalla */
        g2d.setFont(big);
        g2d.drawString(msg, (GP_WIDTH - metrB.stringWidth(msg)) / 2, GP_HEIGHT / 2);
        g2d.setFont(small);
        g2d.drawString(subMsg, (GP_WIDTH - metrS.stringWidth(subMsg)) / 2, GP_HEIGHT / 2 + 16);
    }
    
    /* Método que comprueba si se ha colisionado con una fruta */
    
    private void checkFruit() {
    	
    	/* Si las coordenadas de la cabeza coinciden con las del spawn
    	 * de la fruta, ha colisionado */
        if ((snake_x[0] == fruit_x) && (snake_y[0] == fruit_y)) {
        	
        	/* Si ha spawneado la fruta especial, se procede a "despawnearla" 
        	 * y en vez de aumentar 1 el tamaño / puntuación se aumenta x2 */
        	if (greaterFruitSpawned){
        		greaterFruitSpawned = false;
        		bodyParts += 2;
        		score += 3;
        	/* Fruta normal */	
        	}else{
        		bodyParts++;
        		score++;
        	}
        	
        	/* Se reproduce el sonido de comer una fruta */
            fruitEaten.play();
        	/* Se vuelve a spawnear una fruta */
            locateFruit();
        }
    }
    
    /* Método extemadamente importante, se encarga de mover la serpiente */
    
    private void move() {
    	
    	/* Este bucle hace que se mueva todo el cuerpo, en caso contrario 
    	 * solo se movería la cabeza, el el cuerpo se mueve una posición
    	 * en la cadena. (El segundo se mueve donde estaba el primero, el 
    	 * tercero donde el segundo...) */
        for (int i = bodyParts; i > 0; i--) {
        	/* Si no se actualizan las dos, cuando la serpiente
        	 * esté doblada no se renderizará bien */
            snake_x[i] = snake_x[(i - 1)];
            snake_y[i] = snake_y[(i - 1)];

        }
        
        /* Estos condicionales según que dirección esté tomando restan o suman píxeles (10,
         * tamaño del sprite) a la serpiente en el eje correspondiente */
        
        if (leftDirection) {
            snake_x[0] -= DOT_SIZE;
        }

        if (rightDirection) {
            snake_x[0] += DOT_SIZE;
        }

        if (upDirection) {
            snake_y[0] -= DOT_SIZE;
        }

        if (downDirection) {
            snake_y[0] += DOT_SIZE;
        }
    }
    
    /* Este método comprueba si la serpiente ha colisionado con ella misma o con el marco */
    
    private void checkCollision() {

        for (int i = bodyParts; i > 0; i--) {
        	
        	/* Solo entrará encaso de que haya comido alguna fruta (tamaño mayor a 4) 
        	 * Además se comprueba que la cabeza no esté coincidiendo con alguna parte de
        	 * su propio cuerpo. */
            if ((i > 4) && (snake_x[0] == snake_x[i]) && (snake_y[0] == snake_y[i])) {
                inGame = false;
            }
        }
        
        /* Estos condicionales comprueban que la cabeza no esté sobrepasando los marcos */
        
        if (snake_y[0] >= GP_HEIGHT) {
            inGame = false;
        }

        if (snake_y[0] < 0) {
            inGame = false;
        }

        if (snake_x[0] >= GP_WIDTH) {
            inGame = false;
        }

        if (snake_x[0] < 0) {
            inGame = false;
        }
        
        if(!inGame) {
            timer.stop();
        }
    }
    
    /* Método que spawnea una fruta en una posición aleatoria */
    
    private void locateFruit() {

		greaterFruitSpawned = false;
		
		/* Random para spawnear frutas especiales con poco % */
		
		if ((int) (Math.random() * 10) > 6)
			greaterFruitSpawned = true;
		
		/* Genera las coordenadas de la fruta forma aleatoria */
		
		int r = (int) (Math.random() * RAND_POS);
		fruit_x = ((r * DOT_SIZE));

		r = (int) (Math.random() * RAND_POS);
		fruit_y = ((r * DOT_SIZE));
		
		/* Comprueba que la fruta no ha spawneado en un lugar donde se encuentra el cuerpo de la serpiente, 
		 * en caso de hacerlo se llama a si mismo de nuevo */		
		for (int i = 0; i < bodyParts; i++) {
			if (snake_x[i] == fruit_x && snake_y[i] == fruit_y) {
				locateFruit();
			}
		}

    }
    
    public static int getScore(){
    	return score;
    }
    
    public static double getTime(){
    	return time;
    }
    
    public static boolean isPaused() {
		return isPaused;
	}

	public static void setPaused(boolean isPaused) {
		GamePanel.isPaused = isPaused;
	}
	
	
	/* Método que ejecuta el timer cada 80ms (delay especificado),
	 * este método es el "bucle" principal */
	@Override
    public void actionPerformed(ActionEvent e) {

        if (inGame) {
        	
        	time += 0.08;
            move();
            checkFruit();
            checkCollision();
            
        }

        repaint();
    }

}