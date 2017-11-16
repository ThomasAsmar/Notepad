
package editor;

import java.io.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;



public class Editor extends Application {

    private final Group root;
    private int index;
    private LinkedListDeque<String> page;
    private static String inputFile;
    private int windowWidth, windowHeight;
    public Editor() {
        root = new Group();
        inputFile = inputFile;
        index = 0;
    }

    public Group getRoot() {
        return root;
    }
	/** An EventHandler to handle keys that get pressed. */
    private class KeyEventHandler implements EventHandler<KeyEvent> {
        
        int textCenterX;
        int textCenterY;
        /** The Text to display on the screen. */
        public Text displayText = new Text(250, 250, "");
        public int fontSize = 12;
        private boolean multiple = false;

        private String fontName = "Verdana";

        private int curIndex = 0;
        private int curX;
        private int curY;

        public KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            textCenterX = windowWidth / 2;
            textCenterY = windowHeight / 2;
            index = 0;
            // Initialize some empty text and add it to root so that it will be displayed.
            curX = 0;
            curY = 0;

            // Always set the text origin to be VPos.TOP! Setting the origin to be VPos.TOP means
            // that when the text is assigned a y-position, that position corresponds to the
            // highest position across all letters (for example, the top of a letter like "I", as
            // opposed to the top of a letter like "e"), which makes calculating positions much
            // simpler!
            
        }


        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED && !keyEvent.isShortcutDown()) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    // Ignore control keys, which have non-zero length, as well as the backspace key, which is
                    // represented as a character of value = 8 on Windows.
                    page.typeLetter(characterTyped);
                    keyEvent.consume();

                } 

                else if (characterTyped.charAt(0) == 8){

                	page.backSpace(true);
                	keyEvent.consume();

                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.RIGHT) {
                    page.cursorRight();
                } else if (code == KeyCode.LEFT) {
                    page.cursorLeft();
                } else if (code == KeyCode.UP) {
                    page.cursorUp();
                } else if (code == KeyCode.DOWN) {
                    page.cursorDown();
                } else if(keyEvent.isShortcutDown()) {
                    if (code == KeyCode.S) {
                        page.copy();
                    }
                    else if (code == KeyCode.Z) {
                        page.undo();
                    }
                    else if(code == KeyCode.P) {
                        page.printCursor();
                    }
                    else if (code == KeyCode.Y) {
                        page.redo();
                    }
                    else if (code == KeyCode.MINUS) {
                        page.decreaseFont();
                    }
                    else if (code == KeyCode.PLUS || code == KeyCode.EQUALS) {
                        page.increaseFont();
                    }
                
                }
            }
            keyEvent.consume();

            page.reRender(windowHeight, windowWidth); 
        }

        private void centerText() {
            // Figure out the size of the current text.
            double textHeight = displayText.getLayoutBounds().getHeight();
            double textWidth = displayText.getLayoutBounds().getWidth();

            // Calculate the position so that the text will be centered on the screen.
            double textTop = textCenterY - textHeight / 2;
            double textLeft = textCenterX - textWidth / 2;

            // Re-position the text.
            displayText.setX(textLeft);
            displayText.setY(textTop);

            // Make sure the text appears in front of any other objects you might add.
            displayText.toFront();
        }
    }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent mouseEvent) {
            int xCor = (int) mouseEvent.getX();
            int yCor = (int) mouseEvent.getY();
            page.mouseClick(xCor, yCor);
            page.reRender(windowHeight, windowWidth);
        }
    }

    @Override
    public void start(Stage primaryStage) {
	// Create a Node that will be the parent of all things displayed on the screen.
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        windowWidth = 500;
        windowHeight = 500;
        Scene scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(windowHeight);
        scrollBar.setMin(0);
        scrollBar.setMax(0);
        root.getChildren().add(scrollBar);
        Group textRoot = new Group();
        root.getChildren().add(textRoot);
        page = new LinkedListDeque<String>(inputFile, textRoot, scrollBar);
        page.reRender(windowHeight, windowWidth);
        
        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, windowWidth, windowHeight); 
        MouseClickEventHandler mouseEventHandler = new MouseClickEventHandler();  
        
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(mouseEventHandler);
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                // Re-compute window's width.
                windowWidth = newScreenWidth.intValue();
                page.reRender(windowHeight, windowWidth);
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                windowHeight = newScreenHeight.intValue();
                page.reRender(windowHeight, windowWidth);
            }
        });
        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                    textRoot.setLayoutY(-newValue.intValue());
                

            }
        });


        primaryStage.setTitle("Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("No file given :( gimme a file");
            System.exit(1);
        }
        inputFile = args[0];
        launch(args);

    }
}