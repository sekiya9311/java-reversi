public enum Color {
	BLACK, WHITE, NONE;
	public static Color revColor(Color now) {
		return now == BLACK ? WHITE : BLACK;
	}
}