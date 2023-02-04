import java.io.*;
import java.util.*;
import java.lang.Math;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.lang.Math;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.*;
public class Logic {
    private Requests requests;
    private HashMap<String, Integer> map;
    private String selfSymbol;
    private String oppoSymbol;
    private int gameid;
    private int boardSize;
    private String[][] board;
    private int target;
    private int TEAM_ID = 1296;
    private int turn;
    private boolean is_player_win;
    private boolean is_ai_win;
    static int ai_move_row;
    static int ai_move_colmn;
    public Logic(int gameId, int boardSize, int target, String selfSymbol, String oppoSymbol) throws Exception {
        this.requests = new Requests();
        this.gameid = gameId;
        this.target = target;
        this.boardSize = boardSize;
        this.selfSymbol = selfSymbol;
        this.oppoSymbol = oppoSymbol;
        this.board = new String[boardSize][boardSize];
        initBoard();

//        int n = 2;
//        int m = 2;
//        //初始化数组
//        String[][] Tic_Tac_Toe = new String[n][n];
//        for (int i = 0; i < n; i++){
//            for (int j = 0; j < n; j++){
//                Tic_Tac_Toe[i][j] = "";
//            }
//        }

//        boolean is_player = false;
//        boolean is_ai = true;
//        String player = "O";
//        String ai = "X";
        this.is_player_win = false;
        this.is_ai_win = false;
        this.map = new HashMap<String, Integer>();
        map.put("ai_win", 10);
        map.put("player_win", -10);
        map.put("tie", 0);
        ai_move_row = 0;
        ai_move_colmn = 0;
        start();
//        while (!check(Tic_Tac_Toe, n, m) && !check_full(Tic_Tac_Toe, n)){
//            if (is_ai == true){
//                best_move(Tic_Tac_Toe, n, m, map);
////                ai_move_row = i;
////                ai_move_colmn = j;
//                System.out.println(ai_move_row);
//                System.out.println(ai_move_colmn);
//
//
//
//                is_ai = false;
//                is_player = true;
//                //需要把选择的位置传出
//            }else{
//                human(Tic_Tac_Toe, n);
//                is_player = false;
//                is_ai = true;
//            }
//        }

//        if (is_player_win == true){
//            System.out.println("player win");
//        }
//        else if (is_ai_win == true){
//            System.out.println("ai win");
//        }else{
//            System.out.println("tie");
//        }

    }

    /**
     * Initialize the board and boardSum with the given game
     */
    private void initBoard() throws IOException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", "boardMap");
        params.put("gameId", Integer.toString(this.gameid));
        String result = this.requests.get(params);

        JSONObject parsedResult = JSON.parseObject(result);
        String outputString = parsedResult.getString("output");
        if (outputString == null) {
            for (int i = 0; i < this.boardSize; i++){
                for (int j = 0; j < this.boardSize; j++){
                    this.board[i][j] = "";
                }
            }
            this.turn = 1;
            return;
        }
        for (int i = 0; i < this.boardSize; i++){
            for (int j = 0; j < this.boardSize; j++){
                this.board[i][j] = "";
            }
        }
        JSONObject output = JSON.parseObject(outputString);
        int count = 1;
        for (Entry<String, Object> e : output.entrySet()) {
            String key = e.getKey(), value = (String) e.getValue();
            String[] xy = key.split(",");
            int x = Integer.parseInt(xy[0]), y = Integer.parseInt(xy[1]);
            this.updateBoard(x, y, value);
            count++;
        }
        this.turn = count;
    }

    public void human(String[][] Tic_Tac_Toe, int n){
            for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                if (Tic_Tac_Toe[i][j] == ""){
                    Tic_Tac_Toe[i][j] = this.oppoSymbol;
                    return;
                }
            }
        }
    }
    public void best_move(String[][] Tic_Tac_Toe, int n, int m, HashMap<String, Integer> map) throws Exception {
        int max_score = Integer.MIN_VALUE;

        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                if (Tic_Tac_Toe[i][j] == ""){
                    Tic_Tac_Toe[i][j] = this.selfSymbol;
                    int alpha = Integer.MIN_VALUE;
                    int beta = Integer.MAX_VALUE;
                    int score = Alpha_beta_search(Tic_Tac_Toe, n, m, map, false, alpha, beta);
                    Tic_Tac_Toe[i][j] = "";
                    if (score > max_score){
                        max_score = score;
                        ai_move_row = i;
                        ai_move_colmn = j;
                    }
                }
            }
        }
        confirmMove(ai_move_row, ai_move_colmn);
    }

    private void updateBoard(int x, int y, String symbol) {
        this.board[x][y] = symbol;
    }

    public String getLastMove() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", "moves");
        params.put("gameId", Integer.toString(this.gameid));
        params.put("count", Integer.toString(1));
        String result = this.requests.get(params);

        JSONObject parsedResult = JSON.parseObject(result);
        if (!parsedResult.getString("code").equals("OK")) {
            if (parsedResult.getString("message").equals("No moves")) {
                return "";
            } else {
                throw new Exception("ERROR: getting last move: " + result);
            }
        } else {
            JSONObject temp = JSON.parseObject(parsedResult.get("moves").toString().replace("[", "").replace("]", ""));
            return temp.get("symbol") + "," + temp.get("moveX") + "," + temp.get("moveY");
        }
    }

    public void start() throws Exception {
        // If someone's win, exit
        while (!check(this.board, this.boardSize, this.target) && !check_full(this.board, this.boardSize)) {
            if ((this.turn % 2 == 0 && selfSymbol.equals("O")) || (this.turn % 2 == 1 && selfSymbol.equals("X"))) {
                // If it's opponent's turn, keep querying every second until they make a move
                String move;
                do {
                    TimeUnit.SECONDS.sleep(1);
                    move = getLastMove();
                } while (move.startsWith(selfSymbol) || move.equals(""));
                String[] temp = move.split(",");
                System.out.println("INFO: Turn " + Integer.toString(this.turn));
                System.out.println("Opponent move = " + "(" + temp[1] + ", " + temp[2] + ")");
                this.updateBoard(Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), temp[0]);
                //printBoard();
            } else {
                // If it's our turn, think and make move
                System.out.println("make a move");
                best_move(this.board, this.boardSize, this.target, map);
            }
            this.turn += 1;
        }
        if (this.is_player_win == true){
            System.out.println("player win");
        }
        else if (this.is_ai_win == true){
            System.out.println("ai win");
        }else{
            System.out.println("tie");
        }
    }

    private void confirmMove(int x, int y) throws Exception {
        String move = Integer.toString(x) + "," + Integer.toString(y);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", "move");
        params.put("teamId", Integer.toString(this.TEAM_ID));
        params.put("gameId", Integer.toString(this.gameid));
        params.put("move", move);
        String result = this.requests.post(params);
        System.out.println("Make move: " + x + " " + y);

        JSONObject parsedResult = JSON.parseObject(result);
        if (!parsedResult.getString("code").contains("OK")) {
            throw new Exception("ERROR: making move: " + result);
        } else {
            updateBoard(x, y, this.selfSymbol);
        }
    }

    public int Alpha_beta_search(String[][] Tic_Tac_Toe, int n, int m, HashMap<String, Integer> map, boolean is_max, int alpha, int beta){
        String str = result(Tic_Tac_Toe, n, m);
        if (str != ""){
            return map.get(str);
        }
        if (is_max){
            int max = Integer.MIN_VALUE;
            for (int i = 0; i < n; i++){
                for (int j = 0; j < n; j++){
                    if (Tic_Tac_Toe[i][j] == ""){
                        Tic_Tac_Toe[i][j] = this.selfSymbol;
                        int score = Alpha_beta_search(Tic_Tac_Toe, n, m, map, false, alpha, beta);
                        Tic_Tac_Toe[i][j] = "";
                        max = Math.max(max, score);
                        if (max >= beta){
                            return max;
                        }
                        alpha = Math.max(alpha, max);
                    }
                }
            }
            return max;
        }else{
            int max = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++){
                for (int j = 0; j < n; j++){
                    if (Tic_Tac_Toe[i][j] == ""){
                        Tic_Tac_Toe[i][j] = "O";
                        int score = Alpha_beta_search(Tic_Tac_Toe, n, m, map, true, alpha, beta);
                        Tic_Tac_Toe[i][j] = "";
                        max = Math.min(max, score);
                        if (max <= alpha){
                            return max;
                        }
                        beta = Math.min(beta, max);
                    }
                }
            }
            return max;
        }
    }
    public boolean check_full(String[][] Tic_Tac_Toe, int n){
        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                if (Tic_Tac_Toe[i][j] == ""){
                    return false;
                }
            }
        }
        return true;
    }
    public String result(String[][] Tic_Tac_Toe, int n, int m){

        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                if (Tic_Tac_Toe[i][j].equals(this.selfSymbol) && check_X_result(Tic_Tac_Toe, i, j, n, m)){
                    return "ai_win";
                }
                else if (Tic_Tac_Toe[i][j].equals(this.oppoSymbol) && check_O_result(Tic_Tac_Toe, i, j, n, m)){
                    return "player_win";
                }
            }
        }
        if (check_full(Tic_Tac_Toe, n)){
            return "tie";
        }
        return "";
    }
    public boolean check_X_result(String[][] Tic_Tac_Toe, int i, int j, int n, int m){
        //check row
        int front = j;
        int behind = j;
        int front_num = 0;
        int behind_num = 0;
        for (int c = j; c < n; c++){
            if(Tic_Tac_Toe[i][c].equals(this.selfSymbol)){
                front_num++;
            }else{
                break;
            }
        }
        for (int c = j; c >= 0; c--){
            if(Tic_Tac_Toe[i][c].equals(this.oppoSymbol)){
                behind_num++;
            }else{
                break;
            }
        }
        if (front_num == 0 || behind_num == 0){
            if (front_num >= m || behind_num >= m){

                return true;
            }
        }else{
            if ((front_num + behind_num - 1) >= m){

                return true;
            }
        }

        //check column
        int up = i;
        int down = i;
        int up_num = 0;
        int down_num = 0;
        for (int r = i; r < n; r++){
            if(Tic_Tac_Toe[r][j].equals(this.selfSymbol)){
                down_num++;
            }else{
                break;
            }
        }
        for (int r = i; r >= 0; r--){
            if(Tic_Tac_Toe[r][j].equals(this.oppoSymbol)){
                up_num++;
            }else{
                break;
            }
        }
        if (up_num == 0 || down_num == 0){
            if (up_num >= m || down_num >= m){

                return true;
            }
        }else{
            if ((up_num + down_num - 1) >= m){
                return true;
            }
        }

        //check diagonal
        int up_diagonal = i;
        int down_diagonal = i;
        int up_diagonal_num = 0;
        int down_diagonal_num = 0;
        int r = i;
        int c = j;
        while (r < n && c < n){
            if (Tic_Tac_Toe[r][c].equals(this.selfSymbol)){
                down_diagonal_num++;
            }else{
                break;
            }
            r++;
            c++;
        }
        r = i;
        c = j;
        while (r >= 0 && c >= 0){
            if (Tic_Tac_Toe[r][c].equals(this.oppoSymbol)){
                up_diagonal_num++;
            }else{
                break;
            }
            r--;
            c--;
        }
        if (up_diagonal_num == 0 || down_diagonal_num == 0){
            if (up_diagonal_num >= m || down_diagonal_num >= m){
                return true;
            }
        }else{
            if ((up_diagonal_num + down_diagonal_num - 1) >= m){
                return true;
            }
        }

        return false;
    }
    public boolean check_O_result(String[][] Tic_Tac_Toe, int i, int j, int n, int m){
        //check row
        int front = j;
        int behind = j;
        int front_num = 0;
        int behind_num = 0;
        for (int c = j; c < n; c++){
            if(Tic_Tac_Toe[i][c].equals(this.oppoSymbol)){
                front_num++;
            }else{
                break;
            }
        }
        for (int c = j; c >= 0; c--){
            if(Tic_Tac_Toe[i][c].equals(this.oppoSymbol)){
                behind_num++;
            }else{
                break;
            }
        }
        if (front_num == 0 || behind_num == 0){
            if (front_num >= m || behind_num >= m){
                return true;
            }
        }else{
            if ((front_num + behind_num - 1) >= m){
                return true;
            }
        }

        //check column
        int up = i;
        int down = i;
        int up_num = 0;
        int down_num = 0;
        for (int r = i; r < n; r++){
            if(Tic_Tac_Toe[r][j].equals(this.oppoSymbol)){
                down_num++;
            }else{
                break;
            }
        }
        for (int r = i; r >= 0; r--){
            if(Tic_Tac_Toe[r][j].equals(this.oppoSymbol)){
                up_num++;
            }else{
                break;
            }
        }
        if (up_num == 0 || down_num == 0){
            if (up_num >= m || down_num >= m){
                return true;
            }
        }else{
            if ((up_num + down_num - 1) >= m){
                return true;
            }
        }

        //check diagonal
        int up_diagonal = i;
        int down_diagonal = i;
        int up_diagonal_num = 0;
        int down_diagonal_num = 0;
        int r = i;
        int c = j;
        while (r < n && c < n){
            if (Tic_Tac_Toe[r][c].equals(this.oppoSymbol)){
                down_diagonal_num++;
            }else{
                break;
            }
            r++;
            c++;
        }
        r = i;
        c = j;
        while (r >= 0 && c >= 0){
            if (Tic_Tac_Toe[r][c].equals(this.oppoSymbol)){
                up_diagonal_num++;
            }else{
                break;
            }
            r--;
            c--;
        }
        if (up_diagonal_num == 0 || down_diagonal_num == 0){
            if (up_diagonal_num >= m || down_diagonal_num >= m){
                return true;
            }
        }else{
            if ((up_diagonal_num + down_diagonal_num - 1) >= m){
                return true;
            }
        }

        return false;
    }

    public boolean check(String[][] Tic_Tac_Toe, int n, int m){

        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                if (Tic_Tac_Toe[i][j].equals(this.selfSymbol) && check_X(Tic_Tac_Toe, i, j, n, m)){
                    return true;
                }
                else if (Tic_Tac_Toe[i][j].equals(this.oppoSymbol) && check_O(Tic_Tac_Toe, i, j, n, m)){
                    return true;
                }
            }
        }
        return false;
    }
    public boolean check_X(String[][] Tic_Tac_Toe, int i, int j, int n, int m){
        //check row
        int front = j;
        int behind = j;
        int front_num = 0;
        int behind_num = 0;
        for (int c = j; c < n; c++){
            if(Tic_Tac_Toe[i][c].equals(this.selfSymbol)){
                front_num++;
            }else{
                break;
            }
        }
        for (int c = j; c >= 0; c--){
            if(Tic_Tac_Toe[i][c].equals(this.selfSymbol)){
                behind_num++;
            }else{
                break;
            }
        }
        if (front_num == 0 || behind_num == 0){
            if (front_num >= m || behind_num >= m){
                is_ai_win = true;
                return true;
            }
        }else{
            if ((front_num + behind_num - 1) >= m){
                is_ai_win = true;
                return true;
            }
        }

        //check column
        int up = i;
        int down = i;
        int up_num = 0;
        int down_num = 0;
        for (int r = i; r < n; r++){
            if(Tic_Tac_Toe[r][j].equals(this.selfSymbol)){
                down_num++;
            }else{
                break;
            }
        }
        for (int r = i; r >= 0; r--){
            if(Tic_Tac_Toe[r][j].equals(this.selfSymbol)){
                up_num++;
            }else{
                break;
            }
        }
        if (up_num == 0 || down_num == 0){
            if (up_num >= m || down_num >= m){
                is_ai_win = true;
                return true;
            }
        }else{
            if ((up_num + down_num - 1) >= m){
                is_ai_win = true;
                return true;
            }
        }

        //check diagonal
        int up_diagonal = i;
        int down_diagonal = i;
        int up_diagonal_num = 0;
        int down_diagonal_num = 0;
        int r = i;
        int c = j;
        while (r < n && c < n){
            if (Tic_Tac_Toe[r][c].equals(this.selfSymbol)){
                down_diagonal_num++;
            }else{
                break;
            }
            r++;
            c++;
        }
        r = i;
        c = j;
        while (r >= 0 && c >= 0){
            if (Tic_Tac_Toe[r][c].equals(this.selfSymbol)){
                up_diagonal_num++;
            }else{
                break;
            }
            r--;
            c--;
        }
        if (up_diagonal_num == 0 || down_diagonal_num == 0){
            if (up_diagonal_num >= m || down_diagonal_num >= m){
                is_ai_win = true;
                return true;
            }
        }else{
            if ((up_diagonal_num + down_diagonal_num - 1) >= m){
                is_ai_win = true;
                return true;
            }
        }

        return false;
    }
    public boolean check_O(String[][] Tic_Tac_Toe, int i, int j, int n, int m){
        //check row
        int front = j;
        int behind = j;
        int front_num = 0;
        int behind_num = 0;
        for (int c = j; c < n; c++){
            if(Tic_Tac_Toe[i][c].equals(this.oppoSymbol)){
                front_num++;
            }else{
                break;
            }
        }
        for (int c = j; c >= 0; c--){
            if(Tic_Tac_Toe[i][c].equals(this.oppoSymbol)){
                behind_num++;
            }else{
                break;
            }
        }
        if (front_num == 0 || behind_num == 0){
            if (front_num >= m || behind_num >= m){
                is_player_win = true;
                return true;
            }
        }else{
            if ((front_num + behind_num - 1) >= m){
                is_player_win = true;
                return true;
            }
        }

        //check column
        int up = i;
        int down = i;
        int up_num = 0;
        int down_num = 0;
        for (int r = i; r < n; r++){
            if(Tic_Tac_Toe[r][j].equals(this.oppoSymbol)){
                down_num++;
            }else{
                break;
            }
        }
        for (int r = i; r >= 0; r--){
            if(Tic_Tac_Toe[r][j].equals(this.oppoSymbol)){
                up_num++;
            }else{
                break;
            }
        }
        if (up_num == 0 || down_num == 0){
            if (up_num >= m || down_num >= m){
                is_player_win = true;
                return true;
            }
        }else{
            if ((up_num + down_num - 1) >= m){
                is_player_win = true;
                return true;
            }
        }

        //check diagonal
        int up_diagonal = i;
        int down_diagonal = i;
        int up_diagonal_num = 0;
        int down_diagonal_num = 0;
        int r = i;
        int c = j;
        while (r < n && c < n){
            if (Tic_Tac_Toe[r][c].equals(this.oppoSymbol)){
                down_diagonal_num++;
            }else{
                break;
            }
            r++;
            c++;
        }
        r = i;
        c = j;
        while (r >= 0 && c >= 0){
            if (Tic_Tac_Toe[r][c].equals(this.oppoSymbol)){
                up_diagonal_num++;
            }else{
                break;
            }
            r--;
            c--;
        }
        if (up_diagonal_num == 0 || down_diagonal_num == 0){
            if (up_diagonal_num >= m || down_diagonal_num >= m){
                is_player_win = true;
                return true;
            }
        }else{
            if ((up_diagonal_num + down_diagonal_num - 1) >= m){
                is_player_win = true;
                return true;
            }
        }

        return false;
    }

}
