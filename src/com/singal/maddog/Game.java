package com.singal.maddog;

import javax.swing.JFrame; // Base Container or the main window for the GUI
import java.awt.*;

public class Game extends Canvas implements Runnable {
    private static final long serialVersionUID = 1L;

    public static int width = 300;
    public static int height = width / 16*9 ;
    public static int scale = 3;


    private Thread thread;
    private JFrame frame;
    private boolean running = false;

    public Game() {
        Dimension size = new Dimension(width*scale , height*scale); // actual size is scale times the rendering size
        setPreferredSize(size);

        frame = new JFrame();
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this , "Display");
        thread.start();
    }
    public synchronized void stop() {
        running = false;
        try{
            thread.join();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }
    public void run(){
        // Game Loop
        while(running){
            System.out.println("Running.....");
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.frame.setResizable(false); // causes several graphical errors otherwise , first cmd most times
        game.frame.setTitle("MadDawg");
        game.frame.add(game);
        game.frame.pack();
        game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close button hit will close the window and terminate the program
        game.frame.setLocationRelativeTo(null); // center our window
        game.frame.setVisible(true);

        game.start();

    }


}
