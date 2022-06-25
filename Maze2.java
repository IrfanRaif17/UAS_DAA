/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package maze2;

/**
 *
 * @author irfan
 */

import java.awt.*;
import javax.swing.*;


public class Maze2 extends JPanel implements Runnable {
    
    // a main routine makes it possible to run this class as a program
    public static void main(String[] args) {
        JFrame window = new JFrame("Maze Solver");
        window.setContentPane(new Maze2());
        window.pack();
        window.setLocation(120, 80);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
    

    int[][] maze;   
    final static int backgroundCode = 0;
    final static int wallCode = 1;
    final static int pathCode = 2;
    final static int emptyCode = 3;
    final static int visitedCode = 4;


    Color[] color;          
    int rows = 29;          
    int columns = 31;   
    int border = 0;      
    int sleepTime = 5000;  
    int speedSleep = 30; 
    int blockSize = 12;     

    int width = -1; 
    int height = -1;

    int totalWidth;
    int totalHeight;
    int left;
    int top;

    boolean mazeExists = false;

    public Maze2() {
        color = new Color[] {
            new Color(200,0,0),
            new Color(200,0,0),
            new Color(128,128,255),
            Color.WHITE,
            new Color(200,200,200)
        };
        setBackground(color[backgroundCode]);
        setPreferredSize(new Dimension(blockSize*columns, blockSize*rows));
        new Thread(this).start();
    }

    void checkSize() {
        if (getWidth() != width || getHeight() != height) {
            width  = getWidth();
            height = getHeight();
            int w = (width - 2*border) / columns;
            int h = (height - 2*border) / rows;
            left = (width - w*columns) / 2;
            top = (height - h*rows) / 2;
            totalWidth = w*columns;
            totalHeight = h*rows; 
        }
    }

    synchronized protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        checkSize();
        redrawMaze(g);
    }

    void redrawMaze(Graphics g) {
        if (mazeExists) {
            int w = totalWidth / columns;
            int h = totalHeight / rows;
            for (int j=0; j<columns; j++)
                for (int i=0; i<rows; i++) {
                    if (maze[i][j] < 0)
                        g.setColor(color[emptyCode]);
                    else
                        g.setColor(color[maze[i][j]]);
                    g.fillRect( (j * w) + left, (i * h) + top, w, h );
                }
        }
    }

    public void run() {
          
        try { Thread.sleep(1000); }
        catch (InterruptedException e) { }
        while (true) {
            makeMaze();
            solveMaze(1,1);
            synchronized(this) {
                try { wait(sleepTime); }
                catch (InterruptedException e) { }
            }
            mazeExists = false;
            repaint();
        }
    }

    void makeMaze() {
        if (maze == null)
            maze = new int[rows][columns];
        int i,j;
        int emptyCt = 0;
        int wallCt = 0;
        int[] wallrow = new int[(rows*columns)/2];
        int[] wallcol = new int[(rows*columns)/2];
        for (i = 0; i<rows; i++)
            for (j = 0; j < columns; j++)
                maze[i][j] = wallCode;
        for (i = 1; i<rows-1; i += 2)
            for (j = 1; j<columns-1; j += 2) {
                emptyCt++;
                maze[i][j] = -emptyCt;
                if (i < rows-2) {
                    wallrow[wallCt] = i+1;
                    wallcol[wallCt] = j;
                    wallCt++;
                }
                if (j < columns-2) {
                    wallrow[wallCt] = i;
                    wallcol[wallCt] = j+1;
                    wallCt++;
                }
            }
        mazeExists = true;
        repaint();
        int r;
        for (i=wallCt-1; i>0; i--) {
            r = (int)(Math.random() * i);
            tearDown(wallrow[r],wallcol[r]);
            wallrow[r] = wallrow[i];
            wallcol[r] = wallcol[i];
        }
        for (i=1; i<rows-1; i++)
            for (j=1; j<columns-1; j++)
                if (maze[i][j] < 0)
                    maze[i][j] = emptyCode;
    }

    synchronized void tearDown(int row, int col) {
        if (row % 2 == 1 && maze[row][col-1] != maze[row][col+1]) {
            fill(row, col-1, maze[row][col-1], maze[row][col+1]);
            maze[row][col] = maze[row][col+1];
            repaint();
            try { wait(speedSleep); }
            catch (InterruptedException e) { }
        }
        else if (row % 2 == 0 && maze[row-1][col] != maze[row+1][col]) {
            fill(row-1, col, maze[row-1][col], maze[row+1][col]);
            maze[row][col] = maze[row+1][col];
            repaint();
            try { wait(speedSleep); }
            catch (InterruptedException e) { }
        }
    }

    void fill(int row, int col, int replace, int replaceWith) {
        if (maze[row][col] == replace) {
            maze[row][col] = replaceWith;
            fill(row+1,col,replace,replaceWith);
            fill(row-1,col,replace,replaceWith);
            fill(row,col+1,replace,replaceWith);
            fill(row,col-1,replace,replaceWith);
        }
    }

    boolean solveMaze(int row, int col) {
        if (maze[row][col] == emptyCode) {
            maze[row][col] = pathCode; 
            repaint();
            if (row == rows-2 && col == columns-2)
                return true;
            try { Thread.sleep(speedSleep); }
            catch (InterruptedException e) { }
            if ( solveMaze(row-1,col)  || 
                    solveMaze(row,col-1)  ||
                    solveMaze(row+1,col)  ||
                    solveMaze(row,col+1) )
                return true;
            maze[row][col] = visitedCode;
            repaint();
            synchronized(this) {
                try { wait(speedSleep); }
                catch (InterruptedException e) { }
            }
        }
        return false;
    }
}