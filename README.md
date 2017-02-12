MediaWiki-Java-API
==================

This is a basic, partial implementation of MediaWiki.org's API in Java. It is an almost complete rewrite of the code of this project created 5+ years ago.

### Functionality
  * WikiPages.java
    * getPageContent: Retrieve a specified article's content, including wiki-markup.
    * getEditPage: Retrieve article content and other data needed for editing a page.
    * saveEditPage: Save a page, using data from getEditPage() and modified page contents.
  * WikiCategories
    * In progress

### Configuration
By default this API is configured to interact with Wikipedia with an user agent specific to this API.  The following variables can be changed to alter this default configuration:
  * WikiPages.baseUrl: This specifies the Wiki site to interact with (Default: https://en.wikipedia.com/w)
  * WikiPages.userAgent: This specifies the user agent HTTP requests uses. This should be set to contain information about your bot and not a copy/paste of a web browser's user agent.

### Dependencies
  * Apache HttpClient (http://hc.apache.org/httpcomponents-client-ga/)
    * httpclient-4.5.3
    * httpcore-4.4.6
    * commons-loggin-1.2
