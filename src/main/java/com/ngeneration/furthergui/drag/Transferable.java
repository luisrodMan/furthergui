package com.ngeneration.furthergui.drag;

public interface Transferable {

	boolean isFlavorSupported(Class<? extends Flavor> flavor);

	<T extends Flavor> T getFlavor(Class<T> flavor);

	public static class SingleFlavorTransferable implements Transferable {

		private Flavor flavor;

		public SingleFlavorTransferable(Flavor flavor) {
			this.flavor = flavor;
		}

		@Override
		public boolean isFlavorSupported(Class<? extends Flavor> flavor) {
			return flavor == this.flavor.getClass();
		}

		@Override
		public <T extends Flavor> T getFlavor(Class<T> flavor) {
			return flavor.cast(this.flavor);
		}

	}

}
