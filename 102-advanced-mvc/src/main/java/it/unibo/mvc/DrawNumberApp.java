package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import it.unibo.mvc.Configuration.Builder;

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
        final Configuration.Builder build_config = new Builder();
        readSettings(build_config);
        final Configuration config  = build_config.build();
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        this.model = new DrawNumberImpl(config.getMin(), config.getMax(), config.getAttempts());
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
        new DrawNumberApp(new DrawNumberViewImpl(),
                new DrawNumberViewImpl(), 
                new PrintStreamView(System.out),
                new PrintStreamView("output.log"));
    }

    private final void readSettings(Builder b) throws IOException{
        System.out.println(DIRECTORY_RES);
        try(
            final InputStreamReader file_stream = new InputStreamReader(ClassLoader.getSystemResourceAsStream(DIRECTORY_RES));
            final BufferedReader buff_r = new BufferedReader(file_stream);
        ) {
            b.setMin(Integer.valueOf(readFromStream(buff_r)));
            b.setMax(Integer.valueOf(readFromStream(buff_r)));
            b.setAttempts(Integer.valueOf(readFromStream(buff_r)));
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
