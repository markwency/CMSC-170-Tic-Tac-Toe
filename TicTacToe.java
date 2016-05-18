import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class TicTacToe extends JFrame{
	
	public static final int ROWS = 3;  // ROWS by COLS cells
	public static final int COLS = 3;

	//for drawing the user interface
	public int CELL_SIZE = 150; 
	public int CANVAS_WIDTH = CELL_SIZE * COLS;  
	public int CANVAS_HEIGHT = CELL_SIZE * ROWS;
	public int GRID_WIDTH = 8;                   
	public int GRID_WIDTH_HALF = GRID_WIDTH / 2; 
	public int CELL_PADDING = CELL_SIZE / 6;
	public int SYMBOL_SIZE = CELL_SIZE - CELL_PADDING * 2; 
	public int SYMBOL_STROKE_WIDTH = 8; 

	Token[][] board = new Token[ROWS][COLS];
	Board canvas;
	Token player;
	State gameState;
	JLabel status = new JLabel("");
	JPanel topPanel = new JPanel();
	JButton start = new JButton("New Game");
	//bot initialized as AI
	AI bot = new AI(board);
	static boolean isValid = true;

	Boolean playerTurn;
	
	//tictactoe constructor
	public TicTacToe() {
		canvas = new Board(this);
		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		
		topPanel.setSize(new Dimension(100, 100));
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//call startGame
				startGame();
			}
		});
		
		
		start.setBackground(Color.CYAN);
		topPanel.add(start);
		topPanel.setBackground(Color.WHITE);
		
		startGame();
		canvas.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = e.getY() / CELL_SIZE;
				int col = e.getX() / CELL_SIZE;

				if(gameState == State.PLAYING) {
					
					//check if user turn to make a move
					if(playerTurn) {
						
						//check if executed move is valid
						if(row >= 0 && row < ROWS && col >= 0 && col < COLS && board[row][col] == Token.EMPTY) {
							board[row][col] = player;
							if(checkGame()) 
								gameState = State.LOSE;
							changePlayer();
						
							//set flag so it is ai's turn to move
							playerTurn = false;
							isValid = true;
						} else {
							isValid = false;
							JOptionPane.showMessageDialog(null,
									"Chosen move is invalid.",
									"Oh no!",
									JOptionPane.ERROR_MESSAGE);
						}
					} 
					
					//ai's turn to make a move
					if(gameState == State.PLAYING && isValid) {		
						AIturn();
						if(checkGame()) {
							gameState = State.WIN;
						}
						changePlayer();

						//set flag so it is player's turn to move
						playerTurn = true;
					}
				}
				repaint();

				
				if(gameState == State.DRAW) {
					JOptionPane.showMessageDialog(canvas,
							"DRAW!",
							"TIC-TAC-TOE",
							JOptionPane.INFORMATION_MESSAGE);
				} else if(gameState == State.LOSE) {
					JOptionPane.showMessageDialog(canvas,
							"You Win!",
							"TIC-TAC-TOE",
							JOptionPane.INFORMATION_MESSAGE);
				} else if(gameState == State.WIN) {
					JOptionPane.showMessageDialog(canvas,
							"You Lose!",
							"TIC-TAC-TOE",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		status = new JLabel("Click anywhere to Start!");
		status.setBorder(BorderFactory.createEmptyBorder(2,5,4,5));
		status.setBackground(Color.GREEN);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(topPanel, BorderLayout.NORTH);
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(status, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setTitle("TIC-TAC-TOE");
		setVisible(true);
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension window = new Dimension(getSize().width, getSize().height);
		int x = screen.width - window.width;
		int y = screen.height - window.height;
		setLocation(x/2, y/2);
		setResizable(false);
	}

	//initialize game
	protected void startGame() {

		//initialize all row and col as empty, from Token.java
		for(int row = 0; row < ROWS; row++) {
			for(int col = 0; col < COLS; col++) {
				board[row][col] = Token.EMPTY;
			}
		}
		repaint();

		//State.PLAYING from State.java
		gameState = State.PLAYING;
		//initalize player as X token, from Token.java
		player = Token.X;
		//for while loop
		int n = -1;
		
		//until n is changed
		while(n == -1) {
			n = JOptionPane.showOptionDialog(
					this, "Who goes first?", "TIC-TAC-TOE",
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.QUESTION_MESSAGE,
					null,
					new String[]{"I want to go first", "AI goes first"},
					"I want to go first");
			if(n == JOptionPane.CLOSED_OPTION) {
				JOptionPane.showMessageDialog(this,
						"Please choose",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		//if yes
		playerTurn = true;
		//if NO_OPTION, AI goes first
		if(n == JOptionPane.NO_OPTION) {
			AIturn();
			changePlayer();
			repaint();
		}
	}

	//sequence of commands to determine AI's move
	public void AIturn() {
		//set bot token as player(initalized as token X), call setToken from AI.java
		bot.setToken(player);
		int[] move = bot.move();
		board[move[0]][move[1]] = player;
	}

	//changes current token used in making a move
	public void changePlayer() {
		if(player == Token.X) {
			player = Token.O;
		} else {
			player = Token.X;
		}
	}

	//checks update for game state
	public boolean checkGame() {
		checkDraw();
		return checkWin(player);
	}

	//check if there is already a winner
	public boolean checkWin(Token player) {

		//checks rows
		for(int i=0;i<3;i++) {
			if(player== board[i][0] && player== board[i][1] && player == board[i][2])
				return true;
		}

		//checks cols
		for(int i=0;i<3;i++) {
			if(player == board[0][i] && player== board[1][i] && player== board[2][i])
				return true;
		}

		//checks first diagonal
		if(player == board[0][0] && player == board[1][1] && player == board[2][2])
			return true;

		//checks second diagonal
		if(player== board[0][2] && player == board[1][1] && player== board[2][0])
			return true;

		return false;
	}

	//checks for draw
	public void checkDraw() {

		for(int x=0;x<3;x++) 
			for(int y=0;y<3;y++) {
				if(board[x][y] == Token.EMPTY)
					return;
			}

		gameState = State.DRAW;
	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				new TicTacToe();
			}
		});
	}
}
