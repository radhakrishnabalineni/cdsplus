package com.hp.loader.history;

import java.io.IOException;

public interface HistoryWriteListener {

	public void writeHistory(HistoryHandler historyHandler) throws IOException, HistoryException;
	
}
