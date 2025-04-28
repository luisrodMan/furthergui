package com.ngeneration.furthergui.drag;

public interface Flavor {

	public Object getValue();

	public class ObjectFlavor implements Flavor {

		private Class<?> type;
		private Object value;

		public ObjectFlavor(Object value) {
			this(value.getClass(), value);
		}

		public ObjectFlavor(Class<?> type) {
			this(type, null);
		}

		public ObjectFlavor(Class<?> type, Object value) {
			this.type = type;
			this.value = value;
		}

		@Override
		public Object getValue() {
			return value;
		}

		public Class<?> getType() {
			return type;
		}

	}

}
