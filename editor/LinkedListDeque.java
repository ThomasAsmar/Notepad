/*
 * Thomas Asmar. Represent a Doubly linked list
*/
package editor;
import javafx.application.Application;
import java.util.ArrayList;
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
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import java.util.Stack;
import java.io.*;

public class LinkedListDeque<Item> implements Dequee<Item>{
	private LinkNode sentinal;
	private int size;
	private String fileName;
	private LinkNode cursor;
	private Rectangle actualCursor;
	private ArrayList<LinkNode> yCord;
	private final Group thisGroup;
	private ScrollBar scrollBar;
	private int windowWidth, windowHeight;
	private Stack<UndoNode> undos;
	private Stack<UndoNode> redos;
	private int fontSize;
	private String fontName;

	private class LinkNode{
		public Item item;
		public int line;
		public int x;
		public int y;
		public LinkNode prev, next;
		public Text text;
		public boolean isEnter = false;
		public boolean isCursor;
		public boolean stay;

		public LinkNode(Item obj, LinkNode prev, LinkNode next){
			item = obj;
			this.prev = prev;
			this.next = next;
			text = new Text((String) obj);
			text.setTextOrigin(VPos.TOP);
        	text.setFont(Font.font (fontName, fontSize));
			thisGroup.getChildren().add(text);
			isCursor = false;

		}
		public LinkNode(LinkNode prev, LinkNode next, boolean cursorAction){
			item = (Item) "";
			text = new Text("");
			thisGroup.getChildren().add(text);
			this.prev = prev;
			this.next = next;
			isCursor = true;
			stay = true;

		}
		public LinkNode(){
			line = -1;
			item = null;
			prev = this;
			next = this;
		}
		public Text getText() {
			return text;
		}
		
	}

	
	private class UndoNode {
		public LinkNode node;
		public String action, deletedChar;

		public UndoNode(LinkNode node, String action) {
			this.node = node;
			this.action = action;
		}
		public UndoNode(LinkNode node, String action, String deletedChar) {
			this.node = node;
			this.action = action;
			this.deletedChar = deletedChar;
		}

	} 

	public void add(LinkNode node, String action) {
		if (undos.size() == 100) {
			undos.pop();
		}
		undos.add(new UndoNode(node, action));
	}

	public void add(LinkNode node, String action, String deletedChar) {
		if (undos.size() == 100) {
			undos.pop();
		}
		undos.add(new UndoNode(node, action, deletedChar));

	}

	public void pop() {
		undos.pop();
	}

	//When rerendering make sure to update hella information!!!!! You got this Thomas :D:D
	public void reRender(int windowHeight, int windowWidth) {
		this.windowHeight = windowHeight;
		this.windowWidth = windowWidth;
		scrollBar.setPrefHeight(windowHeight);

		LinkNode curNode = sentinal.next;
		int curX = 5;
		int curY = 0;
		int curHeight = windowHeight;
		int lineNumber = 0;
		yCord.clear();
		LinkNode lastSpace = sentinal;
		int fontHeight = getFontHeight();
        actualCursor.setHeight(fontHeight);
        scrollBar.setMax(0);
        int margin = 5 + (int) scrollBar.getLayoutBounds().getWidth();
        scrollBar.setLayoutX(windowWidth - (int) scrollBar.getLayoutBounds().getWidth());

		while (curNode != sentinal) {
			if(curX == 5 && yCord.size() <= lineNumber) {
				yCord.add(lineNumber, curNode);
				curNode.line = lineNumber;	
			}
			if (curNode.isCursor) {
				if (curX > windowWidth-margin) {
					addCursor(windowWidth-margin, curY);
				}
				else {
					addCursor(curX, curY);
				}
				curNode.line = lineNumber;
			}
			else if (curNode.item.equals("\r") || curNode.item.equals("\n") || curNode.item.equals("\r\n")) {
				curNode.text.setX(curX);
				curNode.text.setY(curY);
				curNode.isEnter = true;
				curX = 5;
				curY += fontHeight;
				curNode.line = lineNumber;
				lineNumber++;
				
			}
			else {

				Text curText = curNode.getText();
				curText.setFont(Font.font(fontName, fontSize));
				if (curText.getText().equals(" ")) {
					lastSpace = curNode;
					curNode.line = lineNumber;

					curText.setX(curX);
					curText.setY(curY);
					curX += Math.round(curText.getLayoutBounds().getWidth());
				}
				else if (curX + curText.getLayoutBounds().getWidth() > windowWidth - margin) {
					if (lastSpace.line < lineNumber) {
						curY += fontHeight;
						curX = 5;
						lineNumber++;
						if (curNode.prev.isCursor && !curNode.prev.stay) {
							curNode.prev.line = lineNumber;
							yCord.add(lineNumber, curNode.prev);
							addCursor(curX, curY);
						} 
						else {
							yCord.add(lineNumber, curNode);
						}
						curNode.line = lineNumber;
						curText.setX(curX);
						curText.setY(curY);
						curX += Math.round(curText.getLayoutBounds().getWidth());
					} 
					else {
						LinkNode start = lastSpace.next;
						if (start.isCursor && !start.stay) {
							start = start.next;
						}
						curY += fontHeight;
						curX = 5;
						lineNumber++;
						yCord.add(lineNumber, start);
						while (start != curNode) {
							if (start.isCursor) {
								addCursor(curX, curY);
								start.line = lineNumber;
								start = start.next;
							}
							else {
								start.getText().setX(curX);
								start.getText().setY(curY);
								start.line = lineNumber;
								curX += Math.round(start.getText().getLayoutBounds().getWidth());
								start = start.next;
							}
							
						}
						curText.setX(curX);
						curText.setY(curY);
						curNode.line = lineNumber;
						curX += Math.round(curText.getLayoutBounds().getWidth()); 
					}
				} 
				else {
					curNode.line = lineNumber;
					curText.setX(curX);
					curText.setY(curY);
					curX += Math.round(curText.getLayoutBounds().getWidth());
				}
				
			}

			curNode = curNode.next;
			if (curY +  fontHeight > curHeight) {
				scrollBar.setMax(scrollBar.getMax() + fontHeight);
				curHeight += fontHeight;
			}

		}
	}

	private int yToLine(int y) {
		int fontHeight = getFontHeight();
        int toReturn = (int) (y / fontHeight);
        if (toReturn >= yCord.size()) {
        	return -1;
        }
		return toReturn; 
	}
	public void undo() {
		if (undos.size() != 0) {
			UndoNode node = undos.pop();
			if (node.action.equals("type")) {
				undoType(node);
			}
			else {
				undoDelete(node);
			}
			reRender(windowHeight, windowWidth);
			snappppppppa();
		}
	}

	public void redo() {
		if (redos.size() != 0) {
			UndoNode node = redos.pop();
			if (node.action.equals("type")) {
				redoType(node);
			}
			else {
				redoDelete(node);
			}
			reRender(windowHeight, windowWidth);
			snappppppppa();
		}
	}



	private void undoType(UndoNode nodee) {
		LinkNode node = nodee.node;
		insertFront(node.next);
		String charText = (String) node.item;
		redos.add(new UndoNode(backSpace(false), "delete", charText));

	}
	private void undoDelete(UndoNode nodee) {
		LinkNode node = nodee.node;
		insertFront(node);
		redos.add(new UndoNode(initType((String) nodee.deletedChar), "type"));
	}

	private void redoType(UndoNode nodee) {
		LinkNode node = nodee.node;
		insertFront(node.next);
		backSpace(true);

	}

	private void redoDelete(UndoNode nodee) {
		LinkNode node = nodee.node;
		insertFront(node);
		typeLetter(nodee.deletedChar);
	}

	private int getFontHeight() {
		Text characterText = new Text(0,0," ");
        characterText.setTextOrigin(VPos.TOP);
        characterText.setFont(Font.font (fontName, fontSize));
        int fontHeight = (int) characterText.getLayoutBounds().getHeight();
        return fontHeight;
	}
	public void cursorUp() {
		if (cursor.prev == sentinal) {
			return;
		}
		int x = (int) actualCursor.getX();
		int y = (int) actualCursor.getY();
		y -= (int) getFontHeight();
		cursor.stay = false;
		insertFront(findNode(x,y));
		
	}
	public void cursorDown() {
		if (cursor.next == sentinal) {
			return;
		}
		int x = (int) actualCursor.getX();
		int y = (int) actualCursor.getY();
		cursor.stay = true;
		y += (int) getFontHeight();
		insertFront(findNode(x,y));
	}
	private void insertFront(LinkNode insertInFront) {
		if (insertInFront.isCursor || insertInFront.prev.isCursor) {
			return;
		}
		LinkNode buffer = insertInFront.prev;
		insertInFront.prev.next = cursor;
		insertInFront.prev = cursor;
		cursor.prev.next = cursor.next;
		cursor.next.prev = cursor.prev;
		cursor.next = insertInFront;
		cursor.prev = buffer;
	}
	//Given x, y coordinate, find node at location 
	private LinkNode findNode(int x, int y) {
		if (y < 0) {
			return sentinal.next;
		}
		int maybe = yToLine(y);
		if (maybe == -1) {
			return sentinal;
		}
		LinkNode start = yCord.get(maybe);
		LinkNode next = start.next;
		if (start.isEnter) {
			return start;
		}
		if (x <= 5) {
			cursor.stay = true;
			return start;
		}
		
		while(next.text.getX() <= x) {
			if (next.line > maybe) {
				cursor.stay = false;
				return next;
			}
			if (next.isEnter) {
				return next;
			}
			start = next;
			next = start.next;
			if (next == sentinal) {
				if (Math.abs(start.text.getX() - x) > Math.abs((start.text.getX() + (int) start.text.getLayoutBounds().getWidth()) - x)) {
					return sentinal;
				}
				return start;
			} 
		}
		if( Math.abs(next.text.getX() - x) <= Math.abs(start.text.getX() - x)) {
			return next;
		}
		cursor.stay = true;
		return start;
	}

	public void printCursor() {
		System.out.println((int) actualCursor.getX() + ", " + (int) actualCursor.getY());
	}

	private void addCursor(int x, int y) {
		actualCursor.setX(x);
		actualCursor.setY(y);
		cursor.text.setX(x);
		cursor.text.setY(y);

	}

	public void cursorRight() {
		if (cursor.next == sentinal) {
			return;
		}
		if (cursor.next.line == cursor.line) {
			cursor.stay = true;
		}
		else {
			cursor.stay = false;
		}
		LinkNode buffer2 = cursor.next.next;
		LinkNode buffer = cursor.next;
		cursor.prev.next = cursor.next;
		cursor.next.prev = cursor.prev;
		cursor.next.next = cursor;
		cursor.next = buffer2;
		cursor.prev = buffer;
	}

	public void mouseClick(int x, int y) {
		cursor.stay = false;
		insertFront(findNode(x,y + (int) scrollBar.getValue()));
	}
	public void cursorLeft() {
		if (cursor.prev == sentinal) {
			return;
		}
		if (cursor.prev.line == cursor.line) {
			cursor.stay = true;
		}
		else {
			cursor.stay = false;
		}
		LinkNode buffer2 = cursor.prev.prev;
		cursor.prev.prev.next = cursor;
		cursor.prev.prev = cursor;
		cursor.prev.next = cursor.next;
		cursor.next.prev = cursor.prev;
		LinkNode buffer = cursor.prev;
		cursor.prev = buffer2;
		cursor.next = buffer;
	}
	public LinkedListDeque(){
		sentinal = new LinkNode();
		cursor = new LinkNode((Item)"-1", sentinal, sentinal);
		sentinal.prev = cursor;
		sentinal.next = cursor;
		size = 1;
		thisGroup = null;
	}

	//Populate a linkedList based on the contents of this file
	public LinkedListDeque(String fileName, Group root, ScrollBar scrollBar) {
		undos = new Stack<UndoNode>();
		redos = new Stack<UndoNode>();
		fontSize = 12;
		this.scrollBar = scrollBar;
		fontName = "Verdana";
		thisGroup = new Group();
		root.getChildren().add(thisGroup);
		sentinal = new LinkNode();
		this.fileName = fileName;
		cursor = new LinkNode(sentinal, sentinal, true);
		actualCursor = new Rectangle(0,0,1,1);
		size = 1;
		thisGroup.getChildren().add(actualCursor);
		makeCursorBlink();
		yCord = new ArrayList<LinkNode>();
		sentinal.prev = cursor;
		sentinal.next = cursor;

		int intRead = -1;
		try {
			File inputFile = new File(fileName);
			if (!inputFile.exists()) {
				System.out.println("Unable to copy because file with name " + fileName + " does not exist");
                return;
			}

			FileReader reader = new FileReader(inputFile);
			BufferedReader bufferedReader = new BufferedReader(reader);
			while ((intRead = reader.read()) != -1) {
                // The integer read can be cast to a char, because we're assuming ASCII.
                char charRead = (char) intRead;
                if (!Character.toString(charRead).equals("\r")) {
                	initType(Character.toString(charRead));
                }
                
            }
            insertFront(sentinal.next);
            reader.close();
        } 
	        catch (FileNotFoundException fileNotFoundException) {
	            System.out.println("File not found! Exception was: " + fileNotFoundException);
	        }
	        catch (IOException ioException) {
	            System.out.println("Error when copying; exception was: " + ioException);
	        }
	        catch (NullPointerException nullpoint) {
	        	System.out.println("lol something did no go right :p");
	        }

		}
	

	public void decreaseFont() {
		fontSize = Math.max(4, fontSize - 4);
	}

	public void increaseFont() {
		fontSize += 4;
	}
	//letter typed
	private LinkNode initType(String character) {
		LinkNode charNode = new LinkNode((Item) character, cursor.prev, cursor);
		cursor.prev.next = charNode;
		cursor.prev = charNode;
		size++;
		return charNode;

	}
	public void typeLetter(String character) {
		add(initType(character), "type");
		snappppppppa();
		
	}

	public void snappppppppa() {
		int slide = (int) scrollBar.getValue();
		int y = (int) actualCursor.getY();
		if (y < slide) {
			scrollBar.setValue(y);
		}
		else if (y > (windowHeight - getFontHeight()+slide)) {
			int newValue = y - (windowHeight-getFontHeight() + slide);
			scrollBar.setValue(slide + newValue);
		}
	}

	public void copy() {
		try {
           FileWriter writer = new FileWriter(fileName);
           LinkNode curNode = sentinal.next;
           while (curNode != sentinal) {
           	if (curNode.isCursor != true) {
           		if (curNode.item.equals("\r") || curNode.item.equals("\n") || curNode.item.equals("\r\n")) {
           			writer.write("\n");
           		}
           		else {
		           	char j = ( (String) curNode.item).charAt(0);
		           	writer.write(j);
		           }
	           }		           	
	           curNode = curNode.next;

           }
           writer.close();
        }
        catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
	}


	public LinkNode backSpace(boolean add) {
		if (cursor.prev == sentinal) {
			return sentinal;
		}
		else {
			LinkNode toDelete = cursor.prev;
			String deletedChar = (String) toDelete.item;
			thisGroup.getChildren().remove(toDelete.getText());
			toDelete = cursor.next;
			cursor.prev.prev.next = cursor;
			cursor.prev = cursor.prev.prev;
			size--;
			snappppppppa();
			if (add) {
				add(toDelete, "delete", deletedChar);
			}
			return toDelete;
		}
	}
	@Override
	public void addFirst(Item obj){
		LinkNode newNode = new LinkNode(obj, sentinal, sentinal.next);
		sentinal.next.prev = newNode;
		sentinal.next = newNode;
		size += 1;

	}

	@Override
	public void addLast(Item obj){
		LinkNode newNode = new LinkNode(obj, sentinal.prev, sentinal);
		sentinal.prev.next = newNode;
		sentinal.prev = newNode;
		size += 1;
	}

	@Override
	public boolean isEmpty(){
		if (sentinal.next == sentinal)
			return true;
		return false;
	}

	@Override
	public int size(){
		return size;
	}

	@Override
	public void printDeque(){
		if(isEmpty())
			return;
		LinkNode currentNode = sentinal.next;
		while(currentNode != sentinal){
			if (currentNode.isCursor) {
				System.out.print("|||");
			}
			else {
				System.out.print(currentNode.item + "");

			}
		currentNode = currentNode.next;
		}
	}

	@Override
	public Item removeFirst(){
		if(isEmpty())
			return null;

		Item item = sentinal.next.item;
		sentinal.next.next.prev = sentinal;
		sentinal.next = sentinal.next.next;
		size-=1;
		return item;
	}

	@Override
	public Item removeLast(){
		if(isEmpty())
			return null;
		Item item = sentinal.prev.item;
		sentinal.prev.prev.next = sentinal;
		sentinal.prev = sentinal.prev.prev;
		size-=1;
		return item;
	}

	@Override
	public Item get(int index){
		if(index >= size)
			return null;
		if(index == 0)
			return sentinal.next.item;
		else if(index == size - 1)
			return sentinal.prev.item;
		else{
			LinkNode currentNode = sentinal.next;
			while(index > 0){
				currentNode = currentNode.next;
				index -= 1;
			}
			return currentNode.item;
		}

	}
	private Item getHelper(LinkNode currentNode, int curIndex){
				if(curIndex == 0)
					return currentNode.item;
				return getHelper(currentNode.next, curIndex - 1);
			}

	public Item getRecursive(int index){
		if (index >= size)
			return null;
		return getHelper(sentinal.next, index);
	}
	
	public void makeCursorBlink() {
        final Timeline timeline = new Timeline();

        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkHandler cursorChange = new CursorBlinkHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();

    }

    private class CursorBlinkHandler implements EventHandler<ActionEvent> {

        private int currentColor = 0;
        private Color[] colors = {Color.BLACK, Color.WHITE};

        CursorBlinkHandler() {
            blink();
        }

        private void blink() {
            actualCursor.setFill(colors[currentColor]);
            currentColor = 1 - currentColor;
            actualCursor.toFront();
        }

        @Override
        public void handle(ActionEvent event) {
            blink();
        }

    }

}