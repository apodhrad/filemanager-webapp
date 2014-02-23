package org.apodhrad.filemanager.webapp;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FileInfo {

	private String fileUrl;

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

}
