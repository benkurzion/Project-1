import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Main {
    //state is a puzzle configuration
    //puzzle is adjacent to all the possible configurations that can be formed from the current configuration
    //A star will calculate the f(n) based on heuristic + distance from original state.
    //That way, we minimize the number of moves that are needed to reach the correct state.

    private static State currentState;
    private static State finalState; //stores correct coordinates for each tile --> finalState[tileNumber] = [x,y]
    private static int maxNumNodes;

    public static void main(String[] args){
        maxNumNodes = Integer.MAX_VALUE;
        finalState = new State(new int[][] {{0,0}, {0,1}, {0,2}, {1,0}, {1,1}, {1,2}, {2,0}, {2,1}, {2,2}});
        currentState = new State(new int[][] {{0,1,2}, {3,4,5}, {6,7,8}});
        String fileName = "";
        if(args != null){
            fileName = args[0];
        }
        try{
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String line = in.readLine();
            while(line != null && line.length() > 0){
                if(line.substring(0,4).equals("setS")){
                    setState(line.substring(9,line.length()));
                }
                if(line.substring(0,4).equals("prin")){
                    printState();
                }
                if(line.substring(0,4).equals("move")){
                    move(line.substring(5, line.length()));
                }
                if(line.substring(0,4).equals("rand")){
                    randomizeState(line.substring(15, line.length()));
                }
                if(line.substring(0,7).equals("solve A")){
                    aStarSearch(line.substring(13, line.length()));
                }
                if(line.substring(0,7).equals("solve b")){
                    beamSearch(line.substring(11, line.length()));
                }
                if(line.substring(0,4).equals("maxN")){
                    maxNodes(line.substring(9, line.length()));
                }
                line = in.readLine();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void setState(String state){
        int col = 0;
        int row = 0;
        int cursor = 0;
        while(cursor < state.length()){
            if(cursor == 3 || cursor == 7){
                row++;
                col = 0;
            }else{
                if(Character.isLetter(state.charAt(cursor))){
                    currentState.getConfiguration()[row][col] = 0;
                    currentState.setBlankCol(col);
                    currentState.setBlankRow(row);
                }else{
                    currentState.getConfiguration()[row][col] = Integer.parseInt("" + state.charAt(cursor));
                }
                col++;
            }
            cursor++;
        }
        if(!checkValidState(currentState)){
            throw new NumberFormatException("This is not a solvable state. Please only set the puzzle to a solvable state.");
        }
    }

    private static boolean checkValidState(State s){
        int[] arr = new int[9];
        int temp = 0;
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                arr[temp] = s.getConfiguration()[i][j];
                temp++;
            }
        }
        int inversions = 0;
        for(int i = 0; i < 9; i++){
            for(int j = i + 1; j < 9; j++){
                if(arr[i] != 0 && arr[j] != 0 && arr[i] > arr[j]){
                    inversions++;
                }
            }
        }
        if(inversions % 2 == 0)
            return true;
        return false;
    }

    private static void printState(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                if(currentState.getConfiguration()[i][j] == 0){
                    sb.append("b");
                }else{
                    sb.append((currentState.getConfiguration()[i][j]));
                }
            }
            sb.append(" ");
        }
        System.out.println(sb.toString());
    }

    private static void move(String direction){
        int row = currentState.getBlankRow();
        int col = currentState.getBlankCol();
        if(direction.equals("up") && row > 0){
            int temp = currentState.getConfiguration()[row - 1][col];
            currentState.getConfiguration()[row - 1][col] = currentState.getConfiguration()[row][col];
            currentState.getConfiguration()[row][col] = temp;
            currentState.setBlankRow(row - 1);
        }else if(direction.equals("down") && row < 2){
            int temp = currentState.getConfiguration()[row + 1][col];
            currentState.getConfiguration()[row + 1][col] = currentState.getConfiguration()[row][col];
            currentState.getConfiguration()[row][col] = temp;
            currentState.setBlankRow(row + 1);
        }else if(direction.equals("left") && col > 0){
            int temp = currentState.getConfiguration()[row][col - 1];
            currentState.getConfiguration()[row][col - 1] = currentState.getConfiguration()[row][col];
            currentState.getConfiguration()[row][col] = temp;
            currentState.setBlankCol(col - 1);
        }else if(direction.equals("right") && col < 2){
            int temp = currentState.getConfiguration()[row][col + 1];
            currentState.getConfiguration()[row][col + 1] = currentState.getConfiguration()[row][col];
            currentState.getConfiguration()[row][col] = temp;
            currentState.setBlankCol(col + 1);
        }
    }

    private static void randomizeState(String n){
        boolean flag;
        int rand;
        for(int i = 0; i < Integer.parseInt(n); i++){
            flag = false;
            while(!flag){
                rand = 1 + (int)(Math.random() * 4);
                if(rand == 1 && currentState.getBlankRow() > 0){
                    flag = true;
                    move("up");
                }
                if(rand == 2 && currentState.getBlankRow() < 2){
                    flag = true;
                    move("down");
                }
                if(rand == 3 && currentState.getBlankCol() > 0){
                    flag = true;
                    move("left");
                }
                if(rand == 4 && currentState.getBlankCol() < 2){
                    flag = true;
                    move("right");
                }
            }

        }
    }

    private static void aStarSearch(String heuristic){
        State goalState = new State();
        int generatedNodes = 0;
        Hashtable<String, State> reached = new Hashtable<>();
        PriorityQueue<State> frontier = new PriorityQueue<>(new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return o1.getCost() - o2.getCost();
            }
        });
        frontier.add(currentState);
        generatedNodes++;
        outerloop:
        while(!frontier.isEmpty() && generatedNodes < maxNumNodes){
            State top = frontier.poll();
            State[] nextStates = generateNextStates(top, heuristic);
            for(int i = 0; i < nextStates.length; i++){
                if(nextStates[i] != null){//generateNextStates can return null elements in the array if a move cannot be made
                    generatedNodes++;
                    if(generatedNodes >= maxNumNodes)
                        break outerloop;
                    if(nextStates[i].getCost() - nextStates[i].getMovesFromStart() == 0){//f(n) - g(n) = h(n)
                        //found finalState. Done Searching
                        goalState = nextStates[i];
                        break outerloop;
                    }
                    //checking for duplicates in reached set
                    State duplicate = reached.get(nextStates[i].stringRepresentation());
                    if (duplicate == null){
                        //no duplicate found
                        frontier.add(nextStates[i]);
                        reached.put(nextStates[i].stringRepresentation(), nextStates[i]);
                    }else if (duplicate.getCost() > nextStates[i].getCost()){
                        //duplicate found but it has a higher cost than the new version
                        frontier.add(nextStates[i]);
                        reached.put(nextStates[i].stringRepresentation(), nextStates[i]);
                    }
                }
            }
            reached.put(top.stringRepresentation(), top);
        }
        if(generatedNodes >= maxNumNodes){
            System.out.println("Error: Max Nodes is too small for given problem");
        }else{
            correctPath(goalState);
        }
    }

    private static void correctPath(State state){
        Stack<String> stack = new Stack<>();
        while(state.parent != null){
            stack.push(state.getParentDirection());
            state = state.getParent();
        }
        //System.out.println("Starting state = ");
        //printInput(state);
        System.out.println("Moves = " + stack.size());
        StringBuilder sb = new StringBuilder();
        while(!stack.isEmpty()){
            sb.append(stack.pop());
            sb.append(" ");
        }
        System.out.println(sb.toString());
    }

    /*
    In the first iteration, you will generate <=4 nodes and explore all of them. In the second iteration, lets say you
    now have 16 nodes and k = 8. You will choose the best k and continue.
    Generate and explore all children until you have more than k children where k is the parameter.
    Start with the currentState as inputted in the text file.
     */
    private static void beamSearch(String k){
        //check if the given state is the goal state
        if(manhattanDistance(currentState) == 0){
            System.out.println("Moves = 0");
            return;
        }
        int generatedNodes = 0;
        Hashtable<String, State> reached = new Hashtable<>();
        PriorityQueue<State> frontier = new PriorityQueue<>(new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return manhattanDistance(o1) - manhattanDistance(o2);
            }
        });
        //used to store each depth of the tree.
        Queue<State> currentDepth = new LinkedList<>();
        currentDepth.add(currentState);
        reached.put(currentState.stringRepresentation(), currentState);
        generatedNodes++;
        State goalState = null; //must be null because this algorithm is not complete.
        outerloop:
        while(generatedNodes < maxNumNodes){
            //add k or more states to the frontier depth by depth.
            while(frontier.size() < Integer.parseInt(k)){
                int numNodes = currentDepth.size();
                //run through every element in this depth and add its successors to the frontier and the reached set.
                for(int i = 0; i < numNodes; i++){
                    State parent = currentDepth.poll();
                    State[] successors = generateNextStates(parent, "h2");
                    for(int j = 0; j < successors.length; j++){
                        //successors must be non-null and have a better cost than their parent
                        if(successors[j] != null && manhattanDistance(parent) > manhattanDistance(successors[j])){
                            generatedNodes++;
                            if(generatedNodes >= maxNumNodes)
                                break outerloop;
                            if(manhattanDistance(successors[j]) == 0){
                                goalState = successors[j];
                                break outerloop;
                            }
                            State duplicate = reached.get(successors[j].stringRepresentation());
                            //add this successor only if it is a new permutation or if the original has a higher cost
                            if(duplicate == null || manhattanDistance(duplicate) > manhattanDistance(successors[j])){
                                frontier.add(successors[j]);
                                reached.put(successors[j].stringRepresentation(), successors[j]);
                                currentDepth.add(successors[j]);
                            }
                        }
                    }
                }
                if(currentDepth.isEmpty()){
                    System.out.println("No solution - local minima found.");
                    return;
                }
            }
            currentDepth.clear();
            //Now, the frontier has at least k States.
            //Reached all the new states
            //currentDepth is empty
            //need to take the k best states from the frontier and make them the current depth. Discard the rest.
            for(int i = 0; i < Integer.parseInt(k); i++){
                State temp = frontier.poll();
                currentDepth.add(temp);
            }
            //discard non-optimal States from the frontier.
            frontier.clear();
        }
        if(goalState != null && generatedNodes < maxNumNodes){
            correctPath(goalState);
        }else if (generatedNodes >= maxNumNodes){
            System.out.println("Error: Max Nodes is too small for given problem");
        }else{
            System.out.println("Beam Search did not find a solution");
        }
    }

    private static void maxNodes(String n){
        maxNumNodes = Integer.parseInt(n);
    }

    private static class State{
        private int[][] configuration;
        private State parent;
        private String parentDirection; //direction to move from parent state to this state.
        private int blankRow;
        private int blankCol;
        private int movesFromStart; //g(n)
        private int cost; //f(n) = g(n) + h(n)

        private State (int[][] c){
            configuration = c;
            movesFromStart = 0;
        }

        private State (){
            configuration = new int[][] {{0,0,0}, {0,0,0}, {0,0,0}};
            movesFromStart = 0;
        }

        private String stringRepresentation(){
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < 3; i++){
                for(int j = 0; j < 3; j++){
                    if(getConfiguration()[i][j] == 0){
                        sb.append("b");
                    }else{
                        sb.append((getConfiguration()[i][j]));
                    }
                }
                sb.append(" ");
            }
            return sb.toString();
        }

        private int[][] getConfiguration() {
            return configuration;
        }

        private void setConfiguration(int[][] configuration) {
            this.configuration = configuration;
        }

        private int getBlankRow() {
            return blankRow;
        }

        private void setBlankRow(int blankRow) {
            this.blankRow = blankRow;
        }

        private int getBlankCol() {
            return blankCol;
        }

        private void setBlankCol(int blankCol) {
            this.blankCol = blankCol;
        }

        private State getParent() {
            return parent;
        }

        private void setParent(State parent) {
            this.parent = parent;
        }

        private int getMovesFromStart() {
            return movesFromStart;
        }

        private void setMovesFromStart(int movesFromStart) {
            this.movesFromStart = movesFromStart;
        }

        private int getCost() {
            return cost;
        }

        private void setCost(int cost) {
            this.cost = cost;
        }

        private String getParentDirection() {
            return parentDirection;
        }

        private void setParentDirection(String parentDirection) {
            this.parentDirection = parentDirection;
        }
    }


    //Heuristic 1 for A* Search.
    private static int numWrongTiles(State state){
        int result = 0;
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                if(state.getConfiguration()[i][j] != 0 && (finalState.getConfiguration()[state.getConfiguration()[i][j]][0] != i ||
                        finalState.getConfiguration()[state.getConfiguration()[i][j]][1] != j)){
                    result++;
                }
            }
        }
        return result;
    }

    //Heuristic 2 for A* Search
    private static int manhattanDistance(State state){
        int result = 0;
        for(int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(state.getConfiguration()[i][j] != 0){
                    result += Math.abs(finalState.getConfiguration()[state.getConfiguration()[i][j]][0] - i);
                    result += Math.abs(finalState.getConfiguration()[state.getConfiguration()[i][j]][1] - j);
                }
            }
        }
        return result;
    }

    private static State[] generateNextStates(State state, String heuristic){
        //System.out.println("Generating nextStates");
        State[] nextStates = new State[4];
        int depth = state.getMovesFromStart() + 1;
        int row = state.getBlankRow();
        int col = state.getBlankCol();
        /*For each permutation:
            Swap the value of 'blank' with the tile in the corresponding direction
            Change the field storing the coordinates for 'blank'
            Set the parent field to whichever direction this permutation came from
            Set the g(n) for each permutation to 'depth' which is the g(n) from parent + 1
            Compute f(n) for each permutation based on which heuristic the function wants
            Add this new permutation to the array of 'nextStates'
            Set permutation back to the parent state so another permutation of the parent can be computed
         */
        if(row > 0){ //permute up
            //cloning state array
            int[][] one = new int[3][];
            for(int i = 0; i < 3; i++){
                one[i] = state.getConfiguration()[i].clone();
            }
            State up = new State(one);
            //moving blank up one place
            int temp = up.getConfiguration()[row - 1][col];
            up.getConfiguration()[row - 1][col] = up.getConfiguration()[row][col];
            up.getConfiguration()[row][col] = temp;
            //moving blank index
            up.setBlankRow(row - 1);
            up.setBlankCol(col);
            //setting g(n)
            up.setMovesFromStart(depth);
            //setting parent
            up.setParentDirection("up");
            up.setParent(state);
            if(heuristic.equals("h1")){
                up.setCost(up.getMovesFromStart() + numWrongTiles(up));
            }else{
                up.setCost(up.getMovesFromStart() + manhattanDistance(up));
            }
            nextStates[0] = up;
        }
        if(row < 2){ //permute down
            //cloning the array
            int[][] two = new int[3][];
            for(int i = 0; i < 3; i++){
                two[i] = state.getConfiguration()[i].clone();
            }
            State down = new State(two);
            //moving blank down one place
            int temp = down.getConfiguration()[row + 1][col];
            down.getConfiguration()[row + 1][col] = down.getConfiguration()[row][col];
            down.getConfiguration()[row][col] = temp;
            //moving blank index
            down.setBlankRow(row + 1);
            down.setBlankCol(col);
            //setting g(n)
            down.setMovesFromStart(depth);
            //setting parent
            down.setParentDirection("down");
            down.setParent(state);
            if(heuristic.equals("h1")){
                down.setCost(down.getMovesFromStart() + numWrongTiles(down));
            }else{
                down.setCost(down.getMovesFromStart() + manhattanDistance(down));
            }
            nextStates[1] = down;
        }
        if(col > 0){ //permute left
            //cloning the array
            int[][] three = new int[3][];
            for(int i = 0; i < 3; i++){
                three[i] = state.getConfiguration()[i].clone();
            }
            State left = new State(three);
            //moving blank left one place
            int temp = left.getConfiguration()[row][col - 1];
            left.getConfiguration()[row][col - 1] = left.getConfiguration()[row][col];
            left.getConfiguration()[row][col] = temp;
            //moving blank index
            left.setBlankRow(row);
            left.setBlankCol(col - 1);
            //setting g(n)
            left.setMovesFromStart(depth);
            //setting parent
            left.setParentDirection("left");
            left.setParent(state);
            if(heuristic.equals("h1")){
                left.setCost(left.getMovesFromStart() + numWrongTiles(left));
            }else{
                left.setCost(left.getMovesFromStart() + manhattanDistance(left));
            }
            nextStates[2] = left;
        }
        if(col < 2){ //permute right
            //cloning the array
            int[][] four = new int[3][];
            for(int i = 0; i < 3; i++){
                four[i] = state.getConfiguration()[i].clone();
            }
            State right = new State(four);
            //moving blank right one place
            int temp = right.getConfiguration()[row][col + 1];
            right.getConfiguration()[row][col + 1] = right.getConfiguration()[row][col];
            right.getConfiguration()[row][col] = temp;
            //moving blank index
            right.setBlankRow(row);
            right.setBlankCol(col + 1);
            //setting g(n)
            right.setMovesFromStart(depth);
            //setting parent
            right.setParentDirection("right");
            right.setParent(state);
            if(heuristic.equals("h1")){
                right.setCost(right.getMovesFromStart() + numWrongTiles(right));
            }else{
                right.setCost(right.getMovesFromStart() + manhattanDistance(right));
            }
            nextStates[3] = right;
        }
        return nextStates;
    }

    private static void printInput(State state){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                if(state.getConfiguration()[i][j] == 0){
                    sb.append("b");
                }else{
                    sb.append((state.getConfiguration()[i][j]));
                }
            }
            sb.append(" ");
        }
        System.out.println(sb.toString());
    }
}
