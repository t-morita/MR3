/*
 * Created on 2003/07/06
 *
 */
package mr3.util;

import java.util.*;

import javax.swing.*;

import mr3.data.*;

/**
 * @author takeshi morita
 */
public class PrefixNSUtil {

	private static Set prefixNSInfoSet;
	private static RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	
	public static void setPrefixNSInfoSet(Set set) {
		prefixNSInfoSet = set;
	}

	public static String getBaseURIPrefix(String baseURI) {
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			if (info.getNameSpace().equals(baseURI)) {
				return info.getPrefix();
			}
		}
		return null;
	}

	private static Set getPropNSSet(List propList) {
		Set propNSSet = new HashSet();
		for (Iterator i = propList.iterator(); i.hasNext();) {
			Object cell = i.next();
			RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
			propNSSet.add(info.getNameSpace());	
		}
		return propNSSet;
	}

	public static Set getPropPrefixes(List propList) {
		Set prefixSet = new TreeSet();
		Set propNSSet = getPropNSSet(propList);
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			if (propNSSet.contains(info.getNameSpace())) {
				prefixSet.add(info.getPrefix());
			}
		}
		return prefixSet;
	}

	public static Set getPrefixes() {
		Set prefixes = new TreeSet();
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			prefixes.add(info.getPrefix());
		}
		return prefixes;
	}

	private static String getNameSpace(String prefix) {
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			if (info.getPrefix().equals(prefix)) {
				return info.getNameSpace();
			}
		}
		return "#";
	}

	public static void setNSLabel(JLabel nsLabel, String str) {
		nsLabel.setText(str);
		nsLabel.setToolTipText(str);
	}

	/* nsLabelをprefixに対応する名前空間に置き換える */
	public static void replacePrefix(String prefix, JLabel nsLabel) {
		//		Resource resource = new ResourceImpl(field.getText());
		//		if (!resource.getNameSpace().equals("http://")) {
		//			String localName = resource.getLocalName();
		//			setNSLabel(field, getNameSpace(prefix) + localName);
		setNSLabel(nsLabel, getNameSpace(prefix));
		//		}
	}

}
