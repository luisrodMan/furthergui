package com.ngeneration.furthergui.drag;

public class ObjectTransferable implements Transferable {

	private Object value;

	public ObjectTransferable(Object value) {
		this.value = value;
	}

	@Override
	public boolean isFlavorSupported(Class<? extends Flavor> flavor) {
		return flavor == Flavor.ObjectFlavor.class;
	}

	@Override
	public <T extends Flavor> T getFlavor(Class<T> flavor) {
		return flavor.cast(new Flavor.ObjectFlavor(value));
	}

}
