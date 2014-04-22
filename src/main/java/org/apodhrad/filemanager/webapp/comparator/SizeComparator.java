package org.apodhrad.filemanager.webapp.comparator;

import java.util.Comparator;

import org.apodhrad.filemanager.webapp.FileInfo;

/**
 * 
 * @author apodhrad
 *
 */
public class SizeComparator implements Comparator<FileInfo> {

	public int compare(FileInfo o1, FileInfo o2) {
		if (o1.getSize() > o2.getSize()) {
			return 1;
		} else if (o1.getSize() < o2.getSize()) {
			return -1;
		} else {
			return 0;
		}
	}

}