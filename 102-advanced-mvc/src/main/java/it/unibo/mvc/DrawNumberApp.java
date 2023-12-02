package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    
    private static final String DIRECTORY_RES =  "config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) throws IOException{
        /*
         * Side-effect proof
         */
        private final Configuration.Builder build_config = new Builder();
        private final Configuration config
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        this.model = new DrawNumberImpl();
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException, IOException{
        new DrawNumberApp(new DrawNumberViewImpl());
    }

    private final void readSettings() throws IOException{
        System.out.println(DIRECTORY_RES);
        try(
            
            final InputStreamReader file_stream = new InputStreamReader(ClassLoader.getSystemResourceAsStream(DIRECTORY_RES));
            final BufferedReader buff_r = new BufferedReader(file_stream);
        ) {
            setMax(Integer.valueOf(readFromStream(buff_r)));
            setAttempts(Integer.valueOf(readFromStream(buff_r)));
            System.out.println(this.min + " " + this.max + " " + this.attempts);
        }
    }

    private final String readFromStream(BufferedReader input) throws IOException {
        String line;
        line = input.readLine();
        StringTokenizer setting = new StringTokenizer(line, ": ");
        setting.nextToken();
        return setting.nextToken();
    }

}
