package com.kirkman_enterprises.MWJAPI;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class WikiPagesTest {

	@Test
	public void testGetPageContent() {
		String expectedText = "Wikipedia:Bots/Status";
		WikiPages contentsTest = new WikiPages("Wikipedia:Bots/Status");
		String pageSrc = null;
		pageSrc = contentsTest.getPageContent();
		// System.out.println(pageSrc);
		Assert.assertTrue(pageSrc.contains(expectedText));
	}
	
	@Test
	public void testGetEditPage() {
		WikiPages getEditTest = new WikiPages("Wikipedia:Sandbox");
		EditParameters params = getEditTest.getEditPage();
		Assert.assertEquals("Wikipedia:Sandbox", params.getArticleTitle());
		Assert.assertNotNull(params.getEditToken());
		Assert.assertNotNull(params.getContent());
		Assert.assertNotNull(params.getBaseTimeStamp());
		Assert.assertNotNull(params.getStartTimeStamp());
		Assert.assertNull(params.getEditSummary());
	}
	
	@Test
	public void testSaveEditPage() {
		WikiPages saveEditPage = new WikiPages("Wikipedia:Sandbox");
		EditParameters params = saveEditPage.getEditPage();
		boolean success = saveEditPage.savePageEdit(params);
		Assert.assertTrue(success);
	}

}
