import java.io.*;
import java.util.*;

public class agent {
    class Pair{
        int i = 0, j = 0;
        public Pair(int i, int j){
            this.i = i;
            this.j = j;
        }
    }
    class Board {
        int width;
        String mode;
        char play;
        int depth;
        int[][] values;
        char[][] state;
        HashMap<Character, Integer> score = new HashMap<>();
        ArrayList<Pair> unoccupied;
        boolean[] used;
        int available;
        public Board(int width, String mode, char play, int depth){
            this.width = width;
            this.mode = mode;
            this.play = play;
            this.depth = depth;
            this.values = new int[width][width];
            this.state = new char[width][width];
            score.put('O', 0);
            score.put('X', 0);
            unoccupied = new ArrayList<>();
        }
        private int utility(){
            char other = (play == 'X')?'O':'X';
            return score.get(play) - score.get(other);
        }
        private void calculateScore(){
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    int v = values[i][j];
                    char curt = state[i][j];
                    if (curt != '.') {
                        score.put(curt, score.get(curt) + v);
                    } else {
                        Pair pos = new Pair(i, j);
                        unoccupied.add(pos);
                    }
                }
            }
            used = new boolean[unoccupied.size()];
            available = unoccupied.size();
        }
    }
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        agent agent = new agent();
        agent.getBoard();
        agent.exec();
        agent.printOutput();

        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime)+"ms");
    }
    Board board;
    Pair maxMove;
    String action;
    private void getBoard(){
        board = getInitState();
    }
    private void exec(){
        switch (board.mode){
            case "MINIMAX":
                minmax_decision();
                break;
            case "ALPHABETA":
                alphabeta_search();
                break;
            default:
                System.out.println("end");
        }
    }
    private void minmax_decision(){
        int maxValue = Integer.MIN_VALUE, v;
        char curt = board.play, other = (board.play == 'X')?'O':'X';
        action = "Stake";

        for (int p = 0; p < board.used.length; p++){
            Pair curtMove = putChess(p, curt);
            v = minValue(other, 1);
            if (v > maxValue){
                maxMove = curtMove;
                maxValue = v;
            }
            takeChess(p, curt);
        }


        for (int p = 0; p < board.used.length; p++){
            Pair pos = board.unoccupied.get(p);

            ArrayList<Pair> changed = new ArrayList<>();
            int raid_value = raidValue(pos.i, pos.j, changed, curt);
            if (raid_value == 0)
                continue;

            putChess(p, curt);
            raidScore(raid_value, curt, other);

            v = minValue(other, 1);
            if (v > maxValue){
                maxMove = pos;
                maxValue = v;
                action = "Raid";
            }

            for (Pair position: changed)
                board.state[position.i][position.j] = other;

            deRaidScore(raid_value, curt, other);
            takeChess(p, curt);
        }
    }
    private void alphabeta_search(){
        int maxV = Integer.MIN_VALUE, v = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;
        char curt = board.play, other = (board.play == 'X')?'O':'X';
        action = "Stake";

        for (int p = 0; p < board.used.length; p++) {
            Pair pos = putChess(p, curt);

            v = Math.max(v, minValue(other, 1, alpha, beta));
            alpha = Math.max(alpha, v);
            if (v > maxV){
                maxV = v;
                maxMove = pos;
            }
            takeChess(p, curt);
        }

        for (int p = 0; p < board.used.length; p++) {
            Pair pos = board.unoccupied.get(p);

            ArrayList<Pair> changed = new ArrayList<>();
            int raid_value = raidValue(pos.i, pos.j, changed, curt);
            if (raid_value == 0)
                continue;

            putChess(p, curt);
            raidScore(raid_value, curt, other);

            v = Math.max(v, minValue(other, 1, alpha, beta));
            alpha = Math.max(alpha, v);
            if (v > maxV){
                action = "Raid";
                maxV = v;
                maxMove = pos;
            }

            for (Pair position: changed)
                board.state[position.i][position.j] = other;

            deRaidScore(raid_value, curt, other);
            takeChess(p, curt);
        }
    }

    private int maxValue(char curt, int depth, int alpha, int beta){
        return ALPHABETA_action(curt, depth, alpha, beta, false);
    }
    private int minValue(char curt, int depth, int alpha, int beta){
        return ALPHABETA_action(curt, depth, alpha, beta, true);
    }
    private int ALPHABETA_action(char curt, int depth, int alpha, int beta, boolean isMin){
        if (depth == board.depth || board.available == 0)
            return board.utility();

        int v = isMin? Integer.MAX_VALUE: Integer.MIN_VALUE;
        char other = (curt == 'X')?'O':'X';

        for (int p = 0; p < board.used.length; p++) {
            if (board.used[p])
                continue;
            Pair pos = putChess(p, curt);

            ArrayList<Pair> changed = new ArrayList<>();
            int raid_value = raidValue(pos.i, pos.j, changed, curt);

            if (raid_value == 0){
                if (isMin)
                    v = Math.min(v, maxValue(other, depth+1, alpha, beta));
                else
                    v = Math.max(v, minValue(other, depth+1, alpha, beta));
            } else {
                raidScore(raid_value, curt, other);
                if (isMin)
                    v = Math.min(v, maxValue(other, depth+1, alpha, beta));
                else
                    v = Math.max(v, minValue(other, depth+1, alpha, beta));
                for (Pair position: changed)
                    board.state[position.i][position.j] = other;
                deRaidScore(raid_value, curt, other);
            }
            takeChess(p, curt);
            if (isMin) {
                if (v <= alpha) return v;
                beta = Math.min(beta, v);
            } else {
                if (v >= beta) return v;
                alpha = Math.max(alpha, v);
            }
        }
        return v;
    }

    private int minValue(char curt, int depth){
        return MINMAX_action(curt, depth, true);
    }
    private int maxValue(char curt, int depth){
        return MINMAX_action(curt, depth, false);
    }
    private int MINMAX_action(char curt, int depth, boolean isMin){
        if (depth == board.depth || board.available == 0)
            return board.utility();

        int v = isMin? Integer.MAX_VALUE: Integer.MIN_VALUE;
        char other = (curt == 'X')?'O':'X';

        for (int p = 0; p < board.used.length; p++){
            if (board.used[p])
                continue;

            Pair pos = putChess(p, curt);
            ArrayList<Pair> changed = new ArrayList<>();
            int raid_value = raidValue(pos.i, pos.j, changed, curt);

            if (raid_value == 0){
                if (isMin)
                    v = Math.min(v, maxValue(other, depth+1));
                else
                    v = Math.max(v, minValue(other, depth+1));
            } else {
                raidScore(raid_value, curt, other);
                if (isMin)
                    v = Math.min(v, maxValue(other, depth+1));
                else
                    v = Math.max(v, minValue(other, depth+1));
                for (Pair position: changed)
                    board.state[position.i][position.j] = other;
                deRaidScore(raid_value, curt, other);
            }
            takeChess(p, curt);
        }
        return v;
    }

    private Board getInitState() {
        File file = new File("input.txt");
        try {
            Scanner scanner = new Scanner(file);
            int n = Integer.valueOf(scanner.nextLine().trim());
            String mode = scanner.nextLine().trim();
            char play = scanner.nextLine().charAt(0);
            int depth = scanner.nextInt();

            Board board = new Board(n, mode, play, depth);
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    board.values[i][j] = scanner.nextInt();

            for (int i = 0; i < n; i++){
                String line = scanner.nextLine().trim();
                if (line.length() != n){
                    i--;
                    continue;
                }
                for (int j = 0; j < n; j++)
                    board.state[i][j] = line.charAt(j);
            }
            board.calculateScore();
            return board;
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }
    private void printOutput(){
        try {
            PrintWriter out = new PrintWriter("output.txt");
            StringBuilder builder = new StringBuilder();
            int i = maxMove.i, j = maxMove.j;
            int n = board.width;
            char column =(char)('A' + j);
            int row = i+1;
            builder.append(column);
            builder.append(row);
            builder.append(" ");
            builder.append(action);
            out.println(builder.toString());

            board.state[i][j] = board.play;
            if (action.equals("Raid")){
                char other = (board.play == 'X')?'O':'X';
                if (j < n-1 && board.state[i][j+1] == other)
                    board.state[i][j+1] = board.play;
                if (j > 0 && board.state[i][j-1] == other)
                    board.state[i][j-1] = board.play;
                if (i < n-1 && board.state[i+1][j] == other)
                    board.state[i+1][j] = board.play;
                if (i > 0 && board.state[i-1][j] == other)
                    board.state[i-1][j] = board.play;
            }
            for (int k = 0; k < n; k++) {
                for (int l = 0; l < n; l++)
                    out.print(board.state[k][l]);
                out.println();
            }
            out.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
    private void raidScore(int value, char curt, char other){
        board.score.put(other, board.score.get(other) - value);
        board.score.put(curt, board.score.get(curt) + value);
    }
    private void deRaidScore(int value, char curt, char other){
        board.score.put(curt, board.score.get(curt) - value);
        board.score.put(other, board.score.get(other) + value);
    }
    private boolean maybeRaid(int i, int j, int n, char curt){
        return  (j < n-1 && board.state[i][j+1] == curt) ||
                (j > 0 && board.state[i][j-1] == curt) ||
                (i < n-1 && board.state[i+1][j] == curt) ||
                (i > 0 && board.state[i-1][j] == curt);
    }
    private int raidValue(int i, int j, ArrayList<Pair> changed, char curt){
        int n = board.width;

        if (!maybeRaid(i, j, n, curt))
            return 0;

        char other = (curt == 'X')?'O':'X';
        int value = 0;

        if (j < n-1 && board.state[i][j+1] == other)
            value += conquer(i, j+1, curt, changed);
        if (j > 0 && board.state[i][j-1] == other)
            value += conquer(i, j-1, curt, changed);
        if (i < n-1 && board.state[i+1][j] == other)
            value += conquer(i+1, j, curt, changed);
        if (i > 0 && board.state[i-1][j] == other)
            value += conquer(i-1, j, curt, changed);

        return value;
    }
    private int conquer(int i, int j, char curt, ArrayList<Pair> changed){
        changed.add(new Pair(i, j));
        board.state[i][j] = curt;
        return board.values[i][j];
    }
    private Pair putChess(int p, char curt){
        Pair pos = board.unoccupied.get(p);
        board.state[pos.i][pos.j] = curt;
        board.used[p] = true;
        board.available--;
        board.score.put(curt, board.score.get(curt) + board.values[pos.i][pos.j]);
        return pos;
    }
    private void takeChess(int p, char curt){
        Pair pos = board.unoccupied.get(p);
        board.used[p] = false;
        board.available++;
        board.state[pos.i][pos.j] = '.';
        board.score.put(curt, board.score.get(curt) - board.values[pos.i][pos.j]);
    }
}
