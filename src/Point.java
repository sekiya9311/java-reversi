
public class Point {
	private char h;
	private char w;

	public Point(char h, char w) {
		this.h = h;
		this.w = w;
	}

	public char getWidth() {
		return this.w;
	}

	public char getHeight() {
		return this.h;
	}

	public void setWidth(char w) {
		this.w = w;
	}

	public void setHeight(char h) {
		this.h = h;
	}

}
