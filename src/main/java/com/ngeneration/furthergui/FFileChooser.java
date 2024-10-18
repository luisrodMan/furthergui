package com.ngeneration.furthergui;

import java.io.File;
import java.util.Arrays;

import javax.swing.filechooser.FileFilter;

import com.ngeneration.furthergui.event.MouseAdapter;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.math.Dimension;

public class FFileChooser extends FDialog {

	public static final String FILES_AND_DIRECTORIES = "files_and_dirs";
	public static final int APPROVE_OPTION = 1;
	private static final int CANCEL_OPTION = 2;

	private FList<File> list = new FList<>();
	private FPanel contentPanel = new FPanel(new BorderLayout());
	private File initialDirectory;
	private FButton acceptBtn = new FButton("Accept");
	private FButton cancelBtn = new FButton("Cancel");
	private FPanel bottomPanel = new FPanel(new FlowLayout());
	private int selectedOption;
	private String dialogType;
	private File currentDirectory;

	public FFileChooser() {
		add(contentPanel);
		list.setCellRenderer(new DefaultListCellRenderer<File>() {
			@Override
			public FComponent getRendererComponent(FList<File> list, File value, boolean isSelected,
					boolean cellHasFocus, int index) {
				var v = super.getRendererComponent(list, value, isSelected, cellHasFocus);
				File vv = (File) value;
				if ((index == 0 && vv.getName().equals("..")) || !vv.isDirectory())
					setText(vv.getName());
				else
					setText("[]" + vv.getName());
				return v;
			}
		});
		list.addMouseListener(new MouseAdapter() {

		});

		contentPanel.add(list);
		contentPanel.add(bottomPanel, BorderLayout.SOUTH);
	}

	public void setFileFilter(FileFilter fileFilter) {

	}

	public int showOpenDialog(FComponent parent) {
		acceptBtn.addActionListener(l -> {
			selectedOption = APPROVE_OPTION;
			dispose();
		});
		cancelBtn.addActionListener(l -> {
			selectedOption = CANCEL_OPTION;
			dispose();
		});

		bottomPanel.add(acceptBtn);
		bottomPanel.add(cancelBtn);

		var initFile = initialDirectory != null ? initialDirectory : new File(".");

		setList(initFile);

		setPrefferedSize(new Dimension(500, 500));
		setDimension(new Dimension(500, 500));

		setVisible(true);

		return selectedOption;
	}

	private void setList(File initFile) {
		list.clearList();
		currentDirectory = initFile;
		initFile = new File(initFile.getAbsolutePath());
		System.out.println("parent: " + initFile.getParentFile());
		if (initFile.getParentFile() != null)
			list.addItem(new File(".."));
		var files = initFile.listFiles();
		Arrays.sort(files,
				(f1, f2) -> ((f1.isFile() && f2.isFile()) || (f1.isDirectory() && f2.isDirectory())
						? f1.getName().compareTo(f2.getName())
						: (f1.isDirectory() ? -1 : 1)));
		for (File file : files) {
			list.addItem(file);
		}
	}

	public File getSelectedFile() {
		return list.getSelectedItem();
	}

	public void setDialogType(String dialogType) {
		this.dialogType = dialogType;
	}

	public void setCurrentDirectory(File file) {
		if (!file.isDirectory())
			throw new RuntimeException("file is not a directory: " + file.getAbsolutePath());
		initialDirectory = file;
	}
}