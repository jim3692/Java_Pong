package knp.YoutubeDemos.Pong;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.JFrame;

public class Game extends Canvas implements Runnable, KeyListener {

	private static final double billion = Math.pow(10, 9);

	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 700;
	public static final int HEIGHT = 500;
	public static final int SCALE = 1;
	public static final String NAME = "Pong";

	private JFrame frame;
	
	HumanPaddle p1;
	AIPaddle p2;
	Ball b1;
	Thread thread;
	
	boolean gameStarted;
	boolean gameOver;
	
	int score;

	public boolean running = false;
	public int tickCount = 0;
	
	private BufferedImage image;
	Graphics gfx;
	
	int fps = 0;
	//private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	
	public Game() {
		setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));

		frame = new JFrame(NAME);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		frame.add(this, BorderLayout.CENTER);
		frame.pack();

		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public synchronized void start() {
		running = true;
		
		initGame();
		
		image  = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		gfx = image.getGraphics();
		
		thread = new Thread(this);
		thread.start();
	}
	
	public void initGame() {
		score = 0;
		gameStarted = false;
		gameOver = false;
		this.addKeyListener(this);
		p1 = new HumanPaddle(1);
		b1 = new Ball();
		p2 = new AIPaddle(2, b1);
	}

	public synchronized void stop() {
		running = false;
	}

	public void run() {
		long lastTime = System.nanoTime();
		double nsPerTick = billion / 60D;

		int ticks = 0;
		int frames = 0;

		long lastTimer = System.currentTimeMillis();
		double delta = 0;

		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;
			
			boolean shouldRender = false;
			
			while (delta >= 1) {
				ticks++;
				tick();
				delta -= 1;
				shouldRender = true;
			}
			
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (shouldRender) {
				frames++;
				render();
			}
			
			if (System.currentTimeMillis() - lastTimer >= 1000) {
				lastTimer += 1000;
				fps = frames;
				System.out.println("Frames per second: " + frames);
				System.out.println("Ticks per second: " + ticks);
				System.out.println("");
				frames = 0;
				ticks = 0;
			}
		}
	}

	public void tick() {
		tickCount++;
		if (gameStarted) {
			p1.move();
			b1.move();
			p2.move();
			if(b1.checkPaddleCollision(p1, p2)) score++;
		}
	}

	public void render() {
		BufferStrategy bStrategy = getBufferStrategy();
		if (bStrategy == null) {
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bStrategy.getDrawGraphics();
		
		gfx.setColor(Color.BLACK);
		gfx.fillRect(0, 0, WIDTH, HEIGHT);
		
		if (b1.getX() < -10 || b1.getX() > 710) {
			gfx.setColor(Color.RED);
			gfx.drawString("Game Over", 350, 250);
			gameOver = true;
			gameStarted = false;
		} else {
			p1.draw(gfx);
			b1.draw(gfx);
			p2.draw(gfx);
		}
		
		if (!gameStarted) {
			gfx.setColor(Color.WHITE);
			gfx.drawString("Tennis", 340, 100);
			gfx.drawString("Press Enter to Begin...", 310, 130);
		}
		
		gfx.setColor(Color.RED);
		gfx.drawString("Score: " + score, 25, 25);
		gfx.drawString("FPS: " + fps, 25, 40);
		
		g.drawImage(image, 0, 0, null);
		g.dispose();
		bStrategy.show();
	}

	public static void main(String[] args) {
		new Game().start();
	}

	public void keyTyped(KeyEvent e) {
		return;
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			p1.setUpAccel(true);
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			p1.setDownAccel(true);
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (gameOver) 
				initGame();
			
			gameStarted = true;
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			p1.setUpAccel(false);
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			p1.setDownAccel(false);
		}
		
	}

}
