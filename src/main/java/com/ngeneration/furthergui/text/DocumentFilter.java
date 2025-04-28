package com.ngeneration.furthergui.text;

public interface DocumentFilter {

	void remove(Bypass filter, int offset, int length);

	void insertString(Bypass filter, int offset, String string);

	void replace(Bypass filter, int offset, int length, String text);

	public static class Bypass {

		private FTextComponent textComponent;

		public Bypass(FTextComponent fTextComponent) {
			this.textComponent = fTextComponent;
		}

		public void remove(int offset, int length) {
			textComponent.remove(offset, length, true);
		}

		public void insertString(int offset, String string) {
			textComponent.insertString(offset, string, null, true);
		}

		public void replace(int offset, int length, String string) {
			textComponent.replaceStringInternal(offset, length, string);
		}

		public FTextComponent getComponent() {
			return textComponent;
		}

	}

}
