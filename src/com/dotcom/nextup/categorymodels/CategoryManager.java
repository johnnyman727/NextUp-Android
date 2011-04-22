package com.dotcom.nextup.categorymodels;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class CategoryManager {
	
	public static String catToString(Category cat) throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ObjectOutput oo = new ObjectOutputStream(baos);
	oo.writeObject(cat);
	return baos.toString();
	}
	
	public static Category stringToCat(String input) throws IOException, ClassNotFoundException {
		byte[] bytes = input.getBytes();
		ByteArrayInputStream bais = new  ByteArrayInputStream(bytes);
		ObjectInput oi = new ObjectInputStream(bais);
		Category cat = (Category) oi.readObject();
		return cat;
		}
}
