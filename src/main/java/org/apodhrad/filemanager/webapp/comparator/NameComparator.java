package org.apodhrad.filemanager.webapp.comparator;

import java.util.Comparator;

import org.apodhrad.filemanager.webapp.FileInfo;


/**
 * 
 * @author apodhrad
 * 
 */
public class NameComparator implements Comparator<FileInfo> {

	private boolean ascendent;

	public NameComparator() {
		this(true);
	}

	public NameComparator(boolean ascendent) {
		this.ascendent = ascendent;
	}

	public int compare(FileInfo o1, FileInfo o2) {
		int result = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
		return (ascendent ? 1 : -1) * result;
	}

}