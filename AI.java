import java.util.ArrayList;
import java.util.List;


public class AI {

	int ROWS = TicTacToe.ROWS;
	int COLS  = TicTacToe.COLS;

	Token[][] board;
	Token token;
	Token oppToken;
	
	//AI Constructor
	public AI(Token[][] game)  {
		board = game;
	}

	//set token used by AI
	public void setToken(Token t) {
		this.token = t;
		if(this.token == Token.X) {
			this.oppToken = Token.O;
		} else {
			this.oppToken = Token.X;
		}
	}

	//ai's turn to make move
	int [] move() {
		//depth, token, alpha, beta
		int[] result = alphabeta(2, token, Integer.MIN_VALUE, Integer.MAX_VALUE);
		
		//return chosen move
		return new int[] {result[1], result[2]};
	}
	
	//minmax algo with alphabeta pruning
	int[] alphabeta(int depth, Token player, int alpha, int beta) {

		List<int[]>children;
		int score;
		//row and col initialized as -1
		int row=-1;
		int col = -1;
		int[] vector = new int[3];

		//depth is 0 or the node is terminal
		if(depth == 0 || Actions().isEmpty()) {
			score = calculateScore();
			vector[0] = score;
			vector[1] = row;
			vector[2] = col;
			return vector;
		}

		//all possible moves (non-empty)
		children = Actions();

		for(int[] child : children){
			
			//ai's turn
			if(player.equals(this.token)){
				//compare alpha to utility value of move done
				vector = alphabeta(depth-1,this.oppToken,alpha,beta);
				if(vector[0] > alpha) {
					alpha = vector[0];
					row = child[0];
					col = child[1];
				}
			}else{ //user's turn
				//get alphabeta of depth-1				
				vector = alphabeta(depth-1,this.token,alpha,beta);
				
				//compare utility of move done to beta's value
				if(vector[0] < beta) {
					beta = vector[0];
					row = child[0];
					row = child[1];
				}
			}
			//undo move done
			board[child[0]][child[1]] = Token.EMPTY;
			// for the cutoff
			if(beta <= alpha)
				break;
		}

		//score is alpha if it is the ai's move if not score is beta
		//return score and chosen move's coordinate
		return new int[] {(player == this.token) ? alpha : beta, row, col};
	}

	//return all possible moves for current board's state
	private List<int[]> Actions() {
		List<int[]> moves = new ArrayList<int[]>(); // allocate List

		// Search for empty cells and add to the List (moves)
		for (int row = 0; row < ROWS; ++row) {
			for (int col = 0; col < COLS; ++col) {
				if (this.board[row][col] == Token.EMPTY) {
					moves.add(new int[] {row, col});
				}
			}
		}
		return moves;
	}
	
	//calculate scores per row
	private int calculateRows() {
		int score = 0;
		for(int row=0; row < 3; row++)
			score += evaluateLine(row, 0, row, 1, row, 2);
		return score;
	}
	
	//calculate scores per column
	private int calculateCols() {
		int score = 0;
		for(int col=0; col < 3; col++)
			score += evaluateLine(0, col, 1, col, 2, col);
		return score;
	}
	
	//calculate scores per diagonal
	private int calculateCross() {
		return (evaluateLine(0, 0, 1, 1, 2, 2) + evaluateLine(0, 2, 1, 1, 2, 0));
	}

	/*
		SCORING:
	  1 line with ai's token = 1 point, with user's token = -1 point
	  1 line with 2 consecutive ai tokens = 10 points, with 2 consecutive user's token = -10 points
	  1 line with 3 consecutive ai tokens = 100 points, with 3 consecutive user's token = -100 points
	  other scenarios gives 0 points
	 */
	private int calculateScore() {
		//return ai score for the current board state
		return (calculateRows() + calculateCols() + calculateCross());
	}

	//checks all cells where you can win, namely: 3 rows, 3 columns and 2 diagonals
	private int evaluateLine(int row1, int col1, int row2, int col2, int row3, int col3) {
		int score = 0;

		//check first cell
		if (this.board[row1][col1] == this.token) {
			score = 1;
		} else if (this.board[row1][col1] == oppToken) {
			score = -1;
		}

		//check 2 consecutive cells
		if (this.board[row2][col2] == this.token) {
			if (score == 1) {
				score = 10;
			} else if (score == -1) {
				return 0;
			} else {
				score = 1;
			}
		} else if (this.board[row2][col2] == oppToken) {
			if (score == -1) {
				score = -10;
			} else if (score == 1) {
				return 0;
			} else {
				score = -1;
			}
		}

		//check 3 consecutive cells
		if (this.board[row3][col3] == this.token) {
			if(score > 0) {
				score *= 10;
			} else if (score < 0) {
				return 0;
			} else {
				score = 1;
			}
		} else if (this.board[row3][col3] == oppToken) {
			/*if (score == -1) {
				score = -10;
			} else if (score == -10) {
			*/	score = -100;
			if(score < 0) {
				score *= 10;
			} else if (score > 1) {
				return 0;
			} else {
				score = -1;
			}
		}
		//return score for the current line
		return score;
	}
}
