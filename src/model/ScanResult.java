package model;

public class ScanResult {

	public final String fileName;
	public final String filePath;

	public final Integer lineNum;
	public final String lineContent;

	public ScanResult(String fileName, String filePath, Integer lineNum, String lineContent) {
		this.fileName = fileName;
		this.filePath = filePath;
		this.lineNum = lineNum;
		this.lineContent = lineContent;
	}

}
