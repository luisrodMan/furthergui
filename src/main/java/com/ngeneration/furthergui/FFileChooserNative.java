package com.ngeneration.furthergui;

import static org.lwjgl.util.nfd.NativeFileDialog.NFD_CANCEL;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_GetError;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_OKAY;

import java.io.File;

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;

public class FFileChooserNative {

	public static File openDialog() {
//		byte[] exts = "txt".getBytes();
//		ByteBuffer outBuffer = ByteBuffer.allocate(exts.length);
//		outBuffer.put(exts);
//		outBuffer.flip();
//		NFDFilterItem.Buffer buf = new NFDFilterItem.Buffer(outBuffer);
		PointerBuffer outPointer = PointerBuffer.allocateDirect(1);
		int result = NativeFileDialog.NFD_OpenDialog(outPointer.clear(), null, (CharSequence) null);

//		buf.free();
//		System.out.println("res: " + result);
		if (result == NFD_OKAY) {
//			System.out.println("Success!");
			String selectedPath = outPointer.getStringUTF8();
//			outPointer.free();
//			System.out.println("selected: " + selectedPath);
			return new File(selectedPath);
		} else if (result == NFD_CANCEL) {
//			System.out.println("User pressed cancel.");
		} else {
			System.err.printf("Error: %s" + System.lineSeparator(), NFD_GetError());
		}

		return null;
	}

}
