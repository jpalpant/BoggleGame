import java.util.ArrayList;
import java.util.List;



public class BoardFirstAutoPlayer extends AbstractAutoPlayer {
	
	private int minLength;
    
    @Override
    public void findAllValidWords(BoggleBoard board, ILexicon lex, int min) {
	    clear();
	    minLength = min;
		
		for(int r = 0; r < board.size(); r++) {
			for(int c = 0; c < board.size(); c++) {
				recursiveWordSearch(board, lex, new ArrayList<BoardCell>(), new BoardCell(r, c));
			}
		}
	}
	
	public void recursiveWordSearch(BoggleBoard board, ILexicon lex, List<BoardCell> previouslyTried, BoardCell nextTry) {
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
		
		LexStatus isword = lex.wordStatus(builtString);
		
		//Add the word to the list of found words if it is in fact a word in the lexicon
		if(isword == LexStatus.WORD && builtString.length() >= minLength) {
			//System.out.println("(" + nextTry.col + ", " + nextTry.row + ") completes a word!");
			add(builtString.toString());
		}
		
		//Continue the search if you have a prefix or a word, because the word could be a prefix
		if(isword == LexStatus.PREFIX || isword == LexStatus.WORD) {
			//System.out.println("\n(" + nextTry.col + ", " + nextTry.row + ") makes a prefix");
			List<BoardCell> neighbors = enumerateNeighbors(nextTry, board);
			neighbors.removeAll(previouslyTried);
			
			for(BoardCell neighbor : neighbors) {
				//System.out.println("Recursively searching (" + neighbor.col + ", " + neighbor.row + ")");
				recursiveWordSearch(board, lex, previouslyTried, neighbor);
				previouslyTried.remove(neighbor);
			}
			
		}
		else {
			//System.out.println(" and this word is neither a prefix nor a word");
		}
	}
	
	public List<BoardCell> enumerateNeighbors(BoardCell b, BoggleBoard board) {
		List<BoardCell> result = new ArrayList<BoardCell>();
		int boardsize = board.size();
		
		for(int rdiff=-1; rdiff<=1; rdiff++) {
			for(int cdiff=-1; cdiff<=1; cdiff++) {
				if((rdiff != 0 || cdiff != 0) && (b.row + rdiff>=0) && (b.col + cdiff >= 0) && (b.col + cdiff < boardsize) && (b.row + rdiff < boardsize)) result.add(new BoardCell(b.row + rdiff, b.col + cdiff));
			}
		}
		
		return result;
	}
    
    
}
