import java.util.ArrayList;
import java.util.List;


public class GoodWordOnBoardFinder implements IWordOnBoardFinder {
	@Override
	public List<BoardCell> cellsForWord(BoggleBoard board, String word) {
		List<BoardCell> locations = new ArrayList<BoardCell>();
		
		
		for(int r = 0; r < board.size(); r++) {
			for(int c = 0; c < board.size(); c++) {
				locations=recursiveFindWord(board, word, new ArrayList<BoardCell>(), new BoardCell(r, c));
				if(!locations.isEmpty()) return locations;
			}
		}
		return new ArrayList<BoardCell>();
	}
	
	public List<BoardCell> recursiveFindWord(BoggleBoard board, String word, List<BoardCell> previouslyTried, BoardCell nextTry) {
		StringBuilder builtString = new StringBuilder();
		
		for(BoardCell bc : previouslyTried) {
			builtString.append(board.getFace(bc.row, bc.col));
		}
		
		builtString.append(board.getFace(nextTry.row, nextTry.col));
		
		//System.out.print("The word " + builtString.toString() + " was built with the cells: ");
		
		previouslyTried.add(nextTry);
		
		for(BoardCell tried : previouslyTried) {
			//System.out.print("(" + tried.col + ", " + tried.row + "), ");
		}
		
		if(word.equals(builtString.toString())) {
			//System.out.println("(" + nextTry.col + ", " + nextTry.row + ") completes the word!");
			return previouslyTried;
		}
		else if(word.startsWith(builtString.toString())) {
			//System.out.println("\n(" + nextTry.col + ", " + nextTry.row + ") adds to the prefix of the word");
			List<BoardCell> neighbors = enumerateNeighbors(nextTry, board);
			neighbors.removeAll(previouslyTried);
			
			for(BoardCell neighbor : neighbors) {
				//System.out.println("Recursively trying (" + neighbor.col + ", " + neighbor.row + ")");
				List<BoardCell> recursiveList = recursiveFindWord(board, word, previouslyTried, neighbor);
				if(!recursiveList.isEmpty()) return recursiveList;
				previouslyTried.remove(neighbor);
			}
			
		}
		else {
			//System.out.println(" and this word is neither " + word + " nor a prefix of it.");
		}
		
		return new ArrayList<BoardCell>();
	}
	
	
	/**
	 * The function generates a safe list of the BoardCells around a given cell - it will not produce and out-of-bounds cells
	 * but it also does not check to see if the cells have already been used before
	 * @param b - the BoardCell around which we must find neighbors
	 * @param board - the board which will be used to bound the search
	 * @returns a list of the (up to eight) neighboring cells
	 */
	public List<BoardCell> enumerateNeighbors(BoardCell b, BoggleBoard board) {
		List<BoardCell> result = new ArrayList<BoardCell>();
		int boardsize = board.size();
		
		//Enumerates all of the possible locations around b, not including b and making sure no locations go off the board
		for(int rdiff=-1; rdiff<=1; rdiff++) {
			for(int cdiff=-1; cdiff<=1; cdiff++) {
				if((rdiff != 0 || cdiff != 0) && (b.row + rdiff>=0) && (b.col + cdiff >= 0) && (b.col + cdiff < boardsize) && (b.row + rdiff < boardsize)) result.add(new BoardCell(b.row + rdiff, b.col + cdiff));
			}
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		GoodWordOnBoardFinder bf = new GoodWordOnBoardFinder();
		String[] faces = {"qu", "e", "e", "n", "o", "z", "j", "k", "t", "i", "o", "p", "e", "k", "l", "p"};
		BoggleBoard bg = new BoggleBoard(faces);
				
		List<BoardCell> cellList = bf.cellsForWord(bg, "quote");
		
		//System.out.println("Done searching!");
		for(BoardCell bc : cellList) {
			System.out.println("(" + bc.col + ", " + bc.row + ")");
		}
	}

}
