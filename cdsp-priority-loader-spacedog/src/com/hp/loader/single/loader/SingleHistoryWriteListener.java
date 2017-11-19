package com.hp.loader.single.loader;

import java.io.IOException;

public interface SingleHistoryWriteListener {

	public void writeHistory(SingleHistoryHandler historyHandler) throws IOException, SingleHistoryException;
	
}
