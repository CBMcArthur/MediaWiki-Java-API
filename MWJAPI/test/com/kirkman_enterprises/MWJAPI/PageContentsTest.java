package com.kirkman_enterprises.MWJAPI;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class PageContentsTest {

	@Test
	public void test() {
		String expectedText = "Wikipedia:Bots/Status";
		PageContents contentsTest = new PageContents("Wikipedia:Bots/Status");
		String pageSrc = null;
		pageSrc = contentsTest.getPageContent();
		// System.out.println(pageSrc);
		Assert.assertTrue(pageSrc.contains(expectedText));
	}

}
