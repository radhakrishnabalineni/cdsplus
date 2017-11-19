package com.hp.cdsplus.web.util;

import java.util.Comparator;
import java.util.Date;

import com.hp.cdsplus.bindings.output.schema.subscription.Ref;

public class LastModifiedComparator implements Comparator<Ref> {
	   
	   public int compare(Ref ref1,Ref ref2) {
		   Date date1 = new Date(Long.valueOf(ref1.getLastModified()));
		   Date date2 = new Date(Long.valueOf(ref2.getLastModified()));
		   return date2.compareTo(date1);
	   }
}
