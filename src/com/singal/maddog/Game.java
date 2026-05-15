package com.singal.maddog;

import com.singal.maddog.graphics.Screen;

import javax.swing.JFrame; // Base Container or the main window for the GUI
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Game extends Canvas implements Runnable {
    private static final long serialVersionUID = 1L;

    public static int width = 300;
    public static int height = width / 16*9 ;
    public static int scale = 3;


    private Thread thread;
    private JFrame frame;
    private boolean running = false;

    private Screen screen;

    private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // creating an image
    private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData(); // converting the image object into an array of integers , accessing the image

    public Game() {
        Dimension size = new Dimension(width*scale , height*scale); // actual size is scale times the rendering size
        setPreferredSize(size);

        screen = new Screen(width, height);

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

        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0;
        double delta = 0;


        while(running){

            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while(delta >= 1){
                update(); // also called tick, limited to around 60 times per sec
                delta--;
            }
            render(); // can run infinitely
        }
        stop();
    }

    public void update(){

    }

    public void render(){
        BufferStrategy bs = getBufferStrategy();
        if (bs == null){
            createBufferStrategy(3); // we don't wanna create buffer everytime, thus the check . 3 for multiple buffering - speed improvement
            return;
        }

        screen.render();

        for(int i = 0; i < pixels.length; i++){
            pixels[i] = screen.pixels[i];
        }

        screen.clear();

        Graphics g = bs.getDrawGraphics();
        /*
        We don't need the below 2 lines anymore as we are now rendering using the Screen class
        g.setColor(Color.black);
        g.fillRect(0,0,getWidth(),getHeight()); // filling should be after you set the color
        */
        g.drawImage(image,0,0,getWidth(),getHeight(),null);
        g.dispose();
        bs.show();


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
