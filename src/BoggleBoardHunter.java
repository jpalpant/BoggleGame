import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.ProgressMonitorInputStream;


public class BoggleBoardHunter {
	private BoggleBoard myBoard;
	private Random myRandom;

	private IAutoPlayer myPlayer;
	private ILexicon myLex;

	private static int MIN_SIZE = 3;

	BoggleBoardHunter() {
		//Make a randomizer
		myRandom = new Random(System.currentTimeMillis());

		newRandomBoard();

		//Make a new player, preferably an efficient one
		myPlayer = new BoardFirstAutoPlayer();

		//Initialize your lexicon
		myLex = new BinarySearchLexicon();

		InputStream is = myLex.getClass().getResourceAsStream("/ospd3.txt");   
		ProgressMonitorInputStream pmis = StoppableReader.getMonitorableStream(is, "reading..."); 
		Scanner s = new Scanner(pmis);
		ArrayList<String> list = new ArrayList<String>();
		while (s.hasNext()){
			list.add(s.next().toLowerCase());
		}

		myLex.load(list);

	}

	private void newRandomBoard() {
		StandardBoardMaker sbm = new StandardBoardMaker();
		myBoard = sbm.makeBoardWithDice(4);			
		//System.out.println(myBoard.getDice());
	}

	private BoggleBoard rotateCube(BoggleBoard board) {
		String[] oldBoard = board.straightString().split("(?!^)");

		int cube = myRandom.nextInt(16);
		//System.out.println("Rotating cube " + cube + " of\n" + board.getDice());
		String newLetter = board.getDice().get(cube).getRandomFace();

		oldBoard[cube] = newLetter;
		BoggleBoard returnable = new BoggleBoard(oldBoard);
		returnable.setDice(board.getDice());
		return returnable;
		
	}

	private BoggleBoard swapCubes(BoggleBoard board) {
		String[] oldBoard = board.straightString().split("(?!^)");

		int cube1 = myRandom.nextInt(16);
		int cube2 = myRandom.nextInt(16);

		String temp = oldBoard[cube1];
		oldBoard[cube1] = oldBoard[cube2];
		oldBoard[cube2] = temp;

		BoggleBoard returnable = new BoggleBoard(oldBoard);
		List<Cube> newDice = board.getDice();
		//System.out.println("Swapping cubes " + cube1 + " and " + cube2 + " of\n" + newDice);
		Collections.swap(newDice, cube1, cube2);
		returnable.setDice(newDice);
		
		return returnable;
	}

	private BoggleBoard makeRandomChange(BoggleBoard board) {
		boolean swap = myRandom.nextBoolean();

		if(swap) return swapCubes(board);
		else return rotateCube(board);
	}
/**
 * 
 * @param ntimes the number of high-density Boggle boards and scores to return in the map
 * @returns a map of scores and the high-density Boggle boards that created those scores
 */
	private String depthHunt(int search_depth, int fail_limit) {
		TreeMap<Integer, BoggleBoard> localBoards = new TreeMap<Integer, BoggleBoard>();
		int failcount = 0;
		int acceptedNum = 0;
		long scoredNum = 0;
		int bestScore = 0;
		double start = System.currentTimeMillis();
		
		newRandomBoard();

		myPlayer.findAllValidWords(myBoard, myLex, MIN_SIZE);
		bestScore = myPlayer.getScore();

		while(failcount < fail_limit)  {
			localBoards.clear();
			acceptedNum++;
			BoggleBoard potential = makeRandomChange(myBoard);
			for(int changes = 1; changes <= search_depth; changes++) {
				//System.out.println("The board with " + changes + " changes is \n" + potential);
				//Store the board at every step out to the max number of steps allowed
				myPlayer.findAllValidWords(potential, myLex, MIN_SIZE);
				int newScore = myPlayer.getScore();
				localBoards.put(newScore, potential);
				potential = makeRandomChange(potential);
				scoredNum++;
			}
			potential = localBoards.get(localBoards.lastKey());								//Pick the board that had the best score of them all, see if it beat best score
			myPlayer.findAllValidWords(potential, myLex, MIN_SIZE);
			scoredNum++;
			int newScore = myPlayer.getScore();

			if(newScore > bestScore) { 	//If the board has been improved
				failcount = 0;			//Reset the number of fails
				bestScore = newScore;	//Update the best score
				myBoard = potential;	//Keep the new board
				//System.out.println(acceptedNum + "\t" + myBoard.straightString() + "\t" + bestScore);

			}
			else {
				failcount++;
				//System.out.println("\t" + k + "\t" + potential.straightString() + "\t" + newScore);
				//System.out.println("Failed " + failcount + " times");
			}
		}

		System.out.print(search_depth + "\t" + fail_limit + "\t" + acceptedNum + "\t" + scoredNum + "\t" + myBoard.straightString() + "\t" + bestScore + "\t" + (System.currentTimeMillis()-start)/1000 + "\n");
		return search_depth + "\t" + fail_limit + "\t" + acceptedNum + "\t" + scoredNum + "\t" + myBoard.straightString() + "\t" + bestScore + "\t" + (System.currentTimeMillis()-start)/1000 + "\n";

	}
	
	
	private String annealingHunt(double start_temp, double temp_k, int stall_limit) {
		int bestScore = 0;
		double temp = start_temp;
		double start = System.currentTimeMillis();
		int last_board_num = 0;
		int accepted_count = 0;

		
		newRandomBoard();
		for(int iteration = 0; iteration <= last_board_num + stall_limit; iteration++) {
			myPlayer.findAllValidWords(myBoard, myLex, MIN_SIZE);
			bestScore = myPlayer.getScore();
			BoggleBoard potential = makeRandomChange(myBoard);

			//Possibly make more than one change
			while(myRandom.nextDouble() < 0.66) {
				//System.out.println("Making another change!");
				potential = makeRandomChange(potential);
			}

			myPlayer.findAllValidWords(potential, myLex, MIN_SIZE);
			int nextScore = myPlayer.getScore();

			if(acceptBoard(temp, bestScore, nextScore)) {			
				//Outright accept the new, shaken board if it has improved!  If it hasn't improved, probabilistically accept it
				//System.out.println(iteration + ":\t" + String.format("%3.2e", temp) + "\t" + potential.straightString() + "\t" + nextScore);
				bestScore = nextScore;
				myBoard = potential;
				temp = start_temp * Math.exp(-1*temp_k*iteration);
				last_board_num = iteration;
				accepted_count++;
			}

		}
		System.out.print(start_temp + "\t" + stall_limit + "\t" + accepted_count + "\t" + (last_board_num+stall_limit) + "\t" + myBoard.straightString() + "\t" + bestScore + "\t" + (System.currentTimeMillis()-start)/1000 + "\n");
		return start_temp + "\t" + stall_limit + "\t" + accepted_count + "\t" + (last_board_num+stall_limit) + "\t" + myBoard.straightString() + "\t" + bestScore + "\t" + (System.currentTimeMillis()-start)/1000 + "\n";
		
	}
	
	private boolean acceptBoard(double temp, int bestScore, int nextScore) {
		if(nextScore > bestScore) return true;
		if(nextScore == bestScore) return false;
		else {
			return (myRandom.nextDouble() < Math.exp((nextScore - bestScore)/temp));
		}
	}


	public static void main(String[] args) {
		BoggleBoardHunter bbh = new BoggleBoardHunter();
		Map<Integer, BoggleBoard> bestDepthBoards = new TreeMap<Integer, BoggleBoard>();
		Map<Integer, BoggleBoard> bestAnnealingBoards = new TreeMap<Integer, BoggleBoard>();
		Map<Integer, BoggleBoard> bestCombBoards = new TreeMap<Integer, BoggleBoard>();
		
		StringBuilder fileString = new StringBuilder(30*30*100);
	
		
		fileString.append("Param1\tParam2\tBoards Accepted\tBoards Scored\tBoard\tScore\tTime\n");
		for(int depth = 1; depth < 30; depth++) {
			for(int failLimit = 10; failLimit < 300; failLimit += 10) {
				fileString.append(bbh.depthHunt(depth, failLimit));
			}
		}
		
		FileWriter depthFile;
		try {
			depthFile = new FileWriter("depth4.txt");
			depthFile.write(fileString.toString());
			depthFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		fileString = new StringBuilder(30*30*100);
		
		fileString.append("Param1\tParam2\tBoards Accepted\tBoards Scored\tBoard\tScore\tTime\n");
		for(int start_temp = 10; start_temp < 300; start_temp += 10) {
			for(int failLimit = 20; failLimit < 600; failLimit += 20) {
				fileString.append(bbh.annealingHunt(start_temp, 0.001, failLimit));
			}
		}
		FileWriter annealingFile;
		try {
			annealingFile = new FileWriter("anneal4.txt");
			annealingFile.write(fileString.toString());
			annealingFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
