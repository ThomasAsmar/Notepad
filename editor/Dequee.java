package editor;
public interface Dequee<Item>{
	public void addFirst(Item item);
	public void addLast(Item item);
	public Item removeLast();
	public Item removeFirst();
	public int size();
	public void printDeque();
	public Item get(int index);
	public boolean isEmpty();
}