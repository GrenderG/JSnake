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

import net.moralesblog.models.FruitType;

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
    /* Delay aplicado al timer, la pantalla se actualizará cada 50 ms */
    private final int DELAY = 50;
    
    /* Colores de fondo y cuadrícula */
    private Color backgroundColor = new Color(202, 140, 99);
    private Color gridColor = new Color(214, 150, 107);
    
    private Color snakeBodyColor = new Color(99, 160, 135);
    private Color snakeBorderColor = new Color(79, 127, 108);
    
    /* Se crean los arrays que contienen las coordenadas del juego */
    private final int snake_x[] = new int[ALL_DOTS];
    private final int snake_y[] = new int[ALL_DOTS];
    
    /* Puntiaci�n y tiempo transcurrido, se necesita acceder de forma estática desde ScorePanel */
    private static int score;
    private static double time;
    
    /* N�mero de partes de la serpiente */   
    private int bodyParts;
    /* Variable auxiliar al que se le suma las partes del cuerpo
     * obtenidas para luego sumarlas al orginal */
    private int bodyPartsCumul = 0;
    
    /* Coordenadas donde ha spawneado la fruta */
    private int fruit_x;
    private int fruit_y;
    
    /* Booleanos que controlan qué dirección está tomando la serpiente */
    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    /* Controla si el movimiento es posible o no */
    private boolean canMove = true;
    
    /* Controlan el estado del juego en cada momento */
    private boolean isNewGame = true;
    private boolean inGame = false;
    private static boolean isPaused = false;
    
    /* Ruta y clip del efecto de sonido a reproducir cuando la serpiente se come una fruta */
	private URL fruitEatenPath = GamePanel.class.getResource("/sounds/fruit_eaten.wav");
	private AudioClip fruitEaten = Applet.newAudioClip(fruitEatenPath);
	/* Ruta y clip del efecto de sonido a reproducir cuando la serpiente no se come una
	 * fruta grande después de 5.5 s */
	private URL missedGreaterFruitPath = GamePanel.class.getResource("/sounds/greater_fruit_missed.wav");
	private AudioClip missedFruit = Applet.newAudioClip(missedGreaterFruitPath);
	/* Ruta y clip del efecto de sonido a reproducir cuando la serpiente se come una
	 * fruta grande */
	private URL greaterFruitEatenPath = GamePanel.class.getResource("/sounds/greater_fruit_eaten.wav");
	private AudioClip greaterFruitEaten = Applet.newAudioClip(greaterFruitEatenPath);
	/* Ruta y clip del efecto de sonido a reproducir cuando la serpiente no se come una
	 * fruta mala después de 4 s */
	private URL missedBadFruitPath = GamePanel.class.getResource("/sounds/bad_fruit_missed.wav");
	private AudioClip missedBadFruit = Applet.newAudioClip(missedBadFruitPath);
	/* Ruta y clip del efecto de sonido a reproducir cuando la serpiente se come una
	 * fruta mala */
	private URL badFruitEatenPath = GamePanel.class.getResource("/sounds/bad_fruit_eaten.wav");
	private AudioClip badFruitEaten = Applet.newAudioClip(badFruitEatenPath);
	/* Ruta y clip del efecto de sonido a reproducir cuando la serpiente se come una
	 * fruta LSD */
	private URL LSDFruitEatenPath = GamePanel.class.getResource("/sounds/lsd_fruit_eaten.wav");
	private AudioClip LSDFruitEaten = Applet.newAudioClip(LSDFruitEatenPath);
	/* Ruta y clip del efecto de sonido a reproducir cuando la serpiente no se come una
	 * fruta mala después de 3 s */
	private URL missedLSDFruitPath = GamePanel.class.getResource("/sounds/lsd_fruit_missed.wav");
	private AudioClip missedLSDFruit = Applet.newAudioClip(missedLSDFruitPath);
	
	/* Timer que controla las veces que se actualiza la pantalla.
	 * Este objeto es muy importante. Hay que implementar la interfaz ActionListener */
    private Timer timer;
    /* Contador usado para saber cuanto tiempo ha estado algo activo (como por 
     * ejemplo la fruta grande) */
    private double auxCont = 0.0;
    
    /* Guarda de qué tipo es la fruta actual */
	private FruitType fruitType = null;
	/* Comprueba si se puede spawnear la fruta o no */
	private boolean canSpawnFruit = true;
	
	/* Variables usadas para hacer posible el
	 * efecto del LSD durante 8 s */
	private byte LSDColorSetter = 0;
	private boolean isLSDModeOn = false;
	private double LSDAuxCount = 0.0;
	
	/* Constructor para GamePanel */
	
    public GamePanel() {

        this.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent e) {
	            int key = e.getKeyCode();
	            
	            if (key == KeyEvent.VK_Q){
	            	if (isPaused)
	            		System.exit(0);
	            }
	            
	            /* Si pulsamos escape el juego se pausa, si lo volvemos a pulsar se reanuda */
	            
	            if (key == KeyEvent.VK_ESCAPE && (inGame)){
	            	if (!isPaused && (!isNewGame)){
	        			InfoPanel.setMSG(InfoPanel.PAUSE_INFO);
	            		timer.stop();
	            	}else if ((!isNewGame) && (isPaused)){
	            		timer.start();
	            	}
	            	
	            	if (!isNewGame)
	            		isPaused = !isPaused;
	            	
	            }
	            
	            /* Si hemos muerto (!inGame) podremos pulsar espacio para resetear el juego */
	            
	            if (key == KeyEvent.VK_SPACE && ((!inGame) || (isNewGame))){
	            
	            	if (isNewGame)
	            		isNewGame = false;
	            	
            		initGame();
	            	
	            }
	            
	            /* Se controla la direcci�n de la serpiente */
	            
	            if ((key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) && (!rightDirection) && canMove) {
	            	canMove = false;
	                leftDirection = true;
	                upDirection = false;
	                downDirection = false;
	            }

	            if ((key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) && (!leftDirection) && canMove) {
	            	canMove = false;
	                rightDirection = true;
	                upDirection = false;
	                downDirection = false;
	            }

	            if ((key == KeyEvent.VK_W || key == KeyEvent.VK_UP) && (!downDirection) && canMove) {
	            	canMove = false;
	                upDirection = true;
	                rightDirection = false;
	                leftDirection = false;
	            }

	            if ((key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) && (!upDirection) && canMove) {
	            	canMove = false;
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
        
        this.setBackground(backgroundColor);
        this.setFocusable(true);
        
        this.setPreferredSize(new Dimension(GP_WIDTH, GP_HEIGHT));
        
        /* Se inicializa el juego */
        
    }
    
    /* Método que inicializa el juego */
    
    private void initGame() {
    	
    	/* Inicializamos las variables a los valores por defecto al principio de la partida */
    	inGame = true;
        bodyParts = 3;
        score = 0;
        time = 0.0;
        auxCont = 0.0;
        LSDAuxCount = 0.0;
        
        isLSDModeOn = false;
        backgroundColor = new Color(202, 140, 99);
        this.setBackground(backgroundColor);
        gridColor = new Color(214, 150, 107);
        
        snakeBodyColor = new Color(99, 160, 135);
        snakeBorderColor = new Color(79, 127, 108);
        
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
        
        /* Se llama al m�todo que generará la primera fruta */        
        locateFruit();
        
        /* Indicamos el delay al timer, en nuestro caso 50ms (cada 50ms llamará al método "actionPerformed"), y se le pasa 
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
        
        Font big = new Font("Helvetica", Font.BOLD, 14);
        Font small = new Font("Helvetica", Font.BOLD, 12);
        /* Se obtienen las medidas de las fuentes, para poder centrarlas correctamente después */
        FontMetrics metrB = getFontMetrics(big);
        FontMetrics metrS = getFontMetrics(small);

        if (isNewGame){
        	
            String msg = "Welcome to JSnake";
            String subMsg = "Press SPACE to start";

            g2d.setColor(Color.white);
            /* Se centran en la pantalla */
            g2d.setFont(big);
            g2d.drawString(msg, (GP_WIDTH - metrB.stringWidth(msg)) / 2, GP_HEIGHT / 2);
            g2d.setFont(small);
            g2d.drawString(subMsg, (GP_WIDTH - metrS.stringWidth(subMsg)) / 2, GP_HEIGHT / 2 + 16);
        
        } else if (inGame) {
        	
        	/* Crea la cuadricula en el mapa, 50 es el número de columnas / filas,
        	 * los parámetros de drawLine son el punto donde empezar y el punto
        	 * donde acabar */
        	
        	g2d.setColor(gridColor);
    		g2d.drawRect(0, 0, GP_WIDTH, GP_HEIGHT);
    		for(int x = 0; x < 50; x++) {
    			for(int y = 0; y < 50; y++) {
    				g2d.drawLine(x * DOT_SIZE, 0, x * DOT_SIZE, GP_HEIGHT);
    				g2d.drawLine(0, y * DOT_SIZE, GP_WIDTH, y * DOT_SIZE);
    			}
    		}
        	
        	/* Si ha spawneado una fruta especial, se renderiza, 
        	 * está formada por varios círculos superpuestos */
        	if (fruitType == FruitType.GreaterFruit){
        		
        		g2d.setColor(Color.BLACK);
        		g2d.fillOval(fruit_x, fruit_y, DOT_SIZE, DOT_SIZE);
            	g2d.setColor(Color.YELLOW); 
            	g2d.fillOval(fruit_x + 1, fruit_y + 1, 8, 8);
            	g2d.setColor(Color.RED); 
            	g2d.fillOval(fruit_x + 2, fruit_y + 2, 6, 6);
            	g2d.setColor(Color.CYAN); 
            	g2d.fillOval(fruit_x + 3, fruit_y + 3, 4, 4);
            	
            /* Esto es la fruta mala */	
        	}else if (fruitType == FruitType.BadFruit){
        		
        		g2d.setColor(Color.BLACK);
        		g2d.fillOval(fruit_x, fruit_y, DOT_SIZE, DOT_SIZE);
            	g2d.setColor(Color.RED); 
            	g2d.fillOval(fruit_x + 1, fruit_y + 1, 8, 8);
            	g2d.setColor(new Color(229, 110, 110)); 
            	g2d.fillOval(fruit_x + 3, fruit_y + 3, 4, 4);
        		
            	/* Esto es la fruta LSD */
        	}else if (fruitType == FruitType.LSDFruit){
        		g2d.setColor(Color.BLACK);
        		g2d.fillOval(fruit_x, fruit_y, DOT_SIZE, DOT_SIZE);
            	g2d.setColor(Color.MAGENTA); 
            	g2d.fillOval(fruit_x + 1, fruit_y + 1, 8, 8);
            	g2d.setColor(Color.GREEN); 
            	g2d.fillOval(fruit_x + 3, fruit_y + 3, 4, 4);
                /* Esto es la fruta normal */
        	}else{
        		
        		g2d.setColor(Color.BLACK);
        		g2d.fillOval(fruit_x, fruit_y, DOT_SIZE, DOT_SIZE);
            	g2d.setColor(Color.RED); 
            	g2d.fillOval(fruit_x + 1, fruit_y + 1, 8, 8);
            	
        	}
        	/*
        	 *         	
        	 */
        	/* En este bucle se renderiza la serpiente*/       	
            for (int i = 0; i < bodyParts; i++) {
            	
            	/* Color del borde de la serpiente */

				g2d.setColor(snakeBorderColor);
				g2d.fillRect(snake_x[i], snake_y[i], DOT_SIZE,
						DOT_SIZE);

				/* Cabeza de la serpiente */
				if (i == 0) {
					g2d.setColor(snakeBorderColor);
				/* Resto del cuerpo */
				} else {
					g2d.setColor(snakeBodyColor);
				}
				
				g2d.fillRect(snake_x[i] + 1, snake_y[i] + 1, 8, 8);

			}
        
			/* Si no se encuentra inGame se procede a terminar la partida actual */
		} else {

			String msg = "Game Over";
			String subMsg = "Press SPACE to continue";

			g2d.setColor(Color.WHITE);
			/* Se centran en la pantalla */
			g2d.setFont(big);
			g2d.drawString(msg, (GP_WIDTH - metrB.stringWidth(msg)) / 2,
					GP_HEIGHT / 2);
			g2d.setFont(small);
			g2d.drawString(subMsg, (GP_WIDTH - metrS.stringWidth(subMsg)) / 2,
					GP_HEIGHT / 2 + 16);
		}
	}
    
    /* Método que comprueba si se ha colisionado con una fruta */
    
    private void checkFruit() {
    	
    	/* Si las coordenadas de la cabeza coinciden con las del spawn
    	 * de la fruta, ha colisionado */
        if ((snake_x[0] == fruit_x) && (snake_y[0] == fruit_y)) {
        	
        	/* Si ha spawneado la fruta especial, se procede a "despawnearla" 
        	 * y en vez de aumentar 1 el tama�o / puntuaci�n se aumenta x2 */
        	if (fruitType == FruitType.GreaterFruit){
        		InfoPanel.setMSG(InfoPanel.GREATER_FRUIT_MSG);
        		greaterFruitEaten.play();
        		auxCont = 0.0;
        		bodyPartsCumul += 3;
        		score += 3;
        	/* Fruta mala */	
        	}else if (fruitType == FruitType.BadFruit){
        		InfoPanel.setMSG(InfoPanel.BAD_FRUIT_MSG);
        		auxCont = 0.0;
        		badFruitEaten.play();
        		bodyPartsCumul -= 2;
        		score -= 2;
        	/* Fruta LSD */	
        	}else if (fruitType == FruitType.LSDFruit){
        		InfoPanel.setMSG(InfoPanel.LSD_FRUIT_MSG);
        		auxCont = 0.0;
        		LSDFruitEaten.play();
        		isLSDModeOn = true;
        		bodyPartsCumul += 5;
        		score += 5;
        	/* Fruta normal */	
        	}else{
        		InfoPanel.setMSG(InfoPanel.NORMAL_FRUIT_MSG);
                fruitEaten.play();
        		bodyPartsCumul ++;
        		score++;
        	}
        	
        	/* Se vuelve a spawnear una fruta */
        	if (canSpawnFruit)
        		locateFruit();
        }
    }
    
    /* Método extremadamente importante, se encarga de mover la serpiente */
    
    private void move() {
    	
    	/* Este bucle hace que se mueva todo el cuerpo, en caso contrario 
    	 * solo se movería la cabeza, el el cuerpo se mueve una posición
    	 * en la cadena. (El segundo se mueve donde estaba el primero, el 
    	 * tercero donde el segundo...) */
        for (int i = bodyParts; i > 0; i--) {
        	/* Si no se actualizan las dos, cuando la serpiente
        	 * está doblada no se renderizará bien */
            snake_x[i] = snake_x[(i - 1)];
            snake_y[i] = snake_y[(i - 1)];

        }
        
        /* Estos condicionales según que dirección está tomando restan o suman píxeles (10,
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
        	 * Además se comprueba que la cabeza no está coincidiendo con alguna parte de
        	 * su propio cuerpo. */
            if ((i > 4) && (snake_x[0] == snake_x[i]) && (snake_y[0] == snake_y[i])) {
                inGame = false;
            }
        }
        
        /* Estos condicionales comprueban que la cabeza no está sobrepasando los marcos */
        
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
    	
    	double spawnRateOp = Math.random() * 10;
    	
		/* Spawnea el tipo de fruta */
		
		if (spawnRateOp > 5.2)
			fruitType = FruitType.GreaterFruit;
		else if (spawnRateOp < 5.2 && spawnRateOp > 4.2 && (bodyParts >= 5 && score >= 2))
			fruitType = FruitType.BadFruit;
		else if (spawnRateOp < 4.0 && spawnRateOp > 3.2 && !isLSDModeOn)
			fruitType = FruitType.LSDFruit;
		else
			fruitType = FruitType.NormalFruit;
		
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
				canSpawnFruit = false;
			}
		}
		
		canSpawnFruit = true;

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
	
	
	/* Método que ejecuta el timer cada 50ms (delay especificado),
	 * este método es el "bucle" principal */
	@Override
    public void actionPerformed(ActionEvent e) {

        if (inGame) {
        	
        	if (fruitType == FruitType.GreaterFruit){
        		auxCont += 0.05;
        		if (auxCont > 5.5){
        			InfoPanel.setMSG(InfoPanel.MISSED_GREATER_FRUIT);
        			missedFruit.play();
        			auxCont = 0.0;
        			locateFruit();
        		}
        	}else if (fruitType == FruitType.BadFruit){
        		auxCont += 0.05;
        		if (auxCont > 4.0){
        			InfoPanel.setMSG(InfoPanel.MISSED_BAD_FRUIT);
        			missedBadFruit.play();
        			auxCont = 0.0;
        			score += 2;
        			bodyParts += 2;
        			locateFruit();
        		}
        	}else if (fruitType == FruitType.LSDFruit){
        		auxCont += 0.05;
        		if (auxCont > 3.0){
        			missedLSDFruit.play();
        			InfoPanel.setMSG(InfoPanel.MISSED_LSD_FRUIT);
        			auxCont = 0.0;
        			locateFruit();
        		}
        	}
        	
			if (isLSDModeOn) {
				
				/* Si pasan 8 s todo vuelve a la normalidad */
				if (LSDAuxCount > 8){
        			InfoPanel.setMSG(InfoPanel.LSD_FRUIT_LEAVING_MSG);
					isLSDModeOn = false;
					LSDAuxCount = 0.0;
					
			        backgroundColor = new Color(202, 140, 99);
			        this.setBackground(backgroundColor);
			        gridColor = new Color(214, 150, 107);
			        
			        snakeBodyColor = new Color(99, 160, 135);
			        snakeBorderColor = new Color(79, 127, 108);
			        
			        /* Aquí se realiza el parpadeo cada 50ms */
				}else{
					if (LSDColorSetter == 0) {
						LSDColorSetter++;
						this.setBackground(Color.GREEN);
						this.gridColor = Color.MAGENTA;
						this.snakeBodyColor = Color.MAGENTA;
						this.snakeBorderColor = Color.GREEN;
					} else {
						LSDColorSetter--;
	            		this.setBackground(Color.MAGENTA);
	            		this.gridColor = Color.GREEN;
	            		this.snakeBodyColor = Color.GREEN;
	            		this.snakeBorderColor = Color.MAGENTA;
					}
					
					LSDAuxCount += 0.05;
				}				

			}
        	
        	time += 0.05;
            move();
            /* Una vez se ha "renderizado" el movimiento, ya es posible
             * volver a mover la serpiente */
            canMove = true;
            checkFruit();
            checkCollision();
            
            /* Se restringe la suma de partes a la serpiente a una cada
             * 50 ms */
            if (bodyPartsCumul > 0){
            	bodyParts ++;
            	bodyPartsCumul--;
            }

        }

        repaint();
    }

}